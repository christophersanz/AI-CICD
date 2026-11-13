#!/usr/bin/env bash
# Comprueba que un proyecto tiene lo que necesitan los prompts del kit (ver docs/prompts/README.md §6).
#   Uso: docs/prompts/kit/verificar.sh [proyecto]   (por defecto, la raíz del repositorio actual)
# Errores (exit 1): falta un documento, una clave DP que usa algún prompt, un comando o su prompt, un prompt
#                   cita un comando inexistente o nombra su archivo de sesión con otro número,
#                   o hay dos archivos para el mismo prompt.
#                   Un documento de DP.doc-* no existe o sigue siendo la plantilla del kit sin completar,
#                   o DP.build usa un wrapper (./mvnw, ./gradlew) que no existe.
# Avisos: valores DP sin completar ([...]), secciones numeradas que faltan (solo si DP.secciones es «Estándar
#         del kit») y plantillas sobrantes en docs/ cuando los documentos reales están en otra ruta.
set -uo pipefail

root="${1:-$(cd "$(dirname "$0")/../../.." && pwd)}"
cd "$root" || exit 1
errors=0 warnings=0
err()  { echo "  ERROR: $*"; errors=$((errors + 1)); }
warn() { echo "  AVISO: $*"; warnings=$((warnings + 1)); }

echo "Verificando el kit de prompts en $root"

# dp_value <clave>: valor de la clave en CLAUDE.md (última columna de su fila; vacío si no está).
dp_value() {
  [ -f CLAUDE.md ] || return 0
  grep -E "^\| \`$1\` \|" CLAUDE.md | head -1 | awk -F'|' '{print $(NF-1)}' | sed 's/^ *//; s/ *$//'
}
# doc_path <clave> <ruta por defecto>: ruta del documento ("-" si es «No aplica»).
doc_path() {
  local value path
  value=$(dp_value "$1")
  [ -n "$value" ] || { echo "$2"; return; }
  if echo "$value" | grep -qiE '^no aplica'; then echo "-"; return; fi
  path=$(echo "$value" | grep -oE '`[^`]+`' | head -1 | tr -d '`[]')
  echo "${path:-$2}"
}
kit_dir=docs/prompts/kit
doc_arq=$(doc_path doc-arquitectura docs/architecture.md)
doc_reglas=$(doc_path doc-reglas docs/business-rules.md)
doc_tec=$(doc_path doc-tecnico docs/technical.md)

echo "1. Documentos"
for f in CLAUDE.md docs/prompts/README.md docs/sessions/README.md; do
  [ -f "$f" ] || err "falta $f"
done
check_doc() {                  # check_doc <clave> <ruta> <plantilla del kit> <obligatorio: si|no>
  local key="$1" path="$2" template="$3" required="$4"
  [ "$path" = "-" ] && { echo "  DP.$key: No aplica"; return; }
  if [ ! -f "$path" ]; then
    if [ "$required" = si ]; then err "DP.$key apunta a $path, que no existe"
    else warn "DP.$key apunta a $path, que no existe (si el proyecto no lo tiene, escribe «No aplica»)"; fi
    return
  fi
  if [ -n "$template" ] && [ -f "$kit_dir/$template" ] && cmp -s "$path" "$kit_dir/$template"; then
    err "$path es la plantilla del kit sin completar (DP.$key); complétala o apunta DP.$key al documento real"
  fi
}
check_doc doc-arquitectura "$doc_arq" architecture.plantilla.md si
check_doc doc-reglas "$doc_reglas" business-rules.plantilla.md si
check_doc doc-tecnico "$doc_tec" "" no
# Plantillas sobrantes: el instalador las crea si no encuentra los documentos; si DP apunta a otro archivo, sobran.
for pair in "architecture.plantilla.md:docs/architecture.md:$doc_arq" "business-rules.plantilla.md:docs/business-rules.md:$doc_reglas"; do
  IFS=: read -r template default current <<< "$pair"
  if [ "$current" != "$default" ] && [ -f "$default" ] && cmp -s "$default" "$kit_dir/$template"; then
    warn "$default es una plantilla sin usar (DP apunta a $current): bórrala para no confundir a los prompts"
  fi
done

echo "2. Datos para los prompts (CLAUDE.md)"
if [ -f CLAUDE.md ]; then
  grep -q "Datos para los prompts" CLAUDE.md || err "CLAUDE.md no tiene la sección \"Datos para los prompts\" (plantilla en docs/prompts/kit/CLAUDE-datos-prompts.md)"
  for key in $(grep -ho 'DP\.[a-z][a-z-]*[a-z]' docs/prompts/*.md .claude/commands/*.md 2>/dev/null | sed 's/^DP\.//' | sort -u); do
    row=$(grep -E "^\| \`$key\` \|" CLAUDE.md | head -1)
    if [ -z "$row" ]; then err "falta la clave DP.$key"
    elif echo "$row" | grep -q '\[[^]]*\]' && ! echo "$row" | grep -q '](' ; then warn "DP.$key sin completar: $(echo "$row" | cut -c1-90)"
    fi
  done
fi

# DP.build: si usa el wrapper (./mvnw, ./gradlew), el wrapper debe existir en el proyecto.
build_cmd=$(dp_value build | grep -oE '`[^`]+`' | head -1 | tr -d '`')
wrapper=$(echo "$build_cmd" | grep -oE '^\./(mvnw|gradlew)' || true)
if [ -n "$wrapper" ] && [ ! -f "$wrapper" ]; then
  err "DP.build usa $wrapper, que no existe en el proyecto (usa mvn/gradle o agrega el wrapper)"
fi

echo "3. Secciones que citan los prompts"
check_sections() {             # check_sections <archivo> <secciones...>
  local file="$1"; shift
  [ -f "$file" ] || return
  for s in "$@"; do grep -qE "^#{2,3} $s[. ]" "$file" || warn "$file no tiene la sección $s"; done
}
secciones=$(dp_value secciones)
if [ -z "$secciones" ] || echo "$secciones" | grep -qi "estándar del kit"; then
  check_sections "$doc_arq" 2 3 5 6 7 9
  check_sections "$doc_reglas" 1 2 3 4 4.1 5 6 7 8 9
else
  echo "  DP.secciones tiene un mapa propio: no se comprueba la numeración del kit"
fi

echo "4. Prompts y comandos"
dups=$(ls docs/prompts/ 2>/dev/null | grep -E '^[0-9]{2}-' | sed -E 's/(-[0-9]{2})?\.md$//' | sort | uniq -d)
[ -z "$dups" ] || err "hay más de un archivo para: $dups"
ls docs/prompts/ 2>/dev/null | grep -qE '^[0-9]{2}-.*-[0-9]{2}\.md$' && err "hay prompts con versión en el nombre (un prompt = un archivo NN-nombre.md)"
for p in docs/prompts/[0-9][0-9]-*.md; do
  [ -e "$p" ] || { err "no hay prompts en docs/prompts/"; break; }
  name=$(basename "$p" .md)
  [ -f ".claude/commands/$name.md" ] || err "falta el comando .claude/commands/$name.md"
done
for c in .claude/commands/[0-9][0-9]-*.md; do
  [ -e "$c" ] || break
  for ref in $(grep -o 'docs/prompts/[0-9a-z-]*\.md' "$c" | sort -u); do
    [ -f "$ref" ] || err "$c apunta a $ref, que no existe"
  done
done

echo "5. Referencias internas de los prompts"
# El archivo de sesión de cada prompt debe llevar su propio número y nombre (se rompe al renumerar).
for p in docs/prompts/[0-9][0-9]-*.md; do
  [ -e "$p" ] || break
  name=$(basename "$p" .md)
  for s in $(grep -oE 'AAAA-MM-DD-[0-9]{2}-[a-z]+(-[a-z]+)*' "$p" | sort -u); do
    case "${s#AAAA-MM-DD-}" in
      "$name"*) ;;
      *) err "$p nombra su archivo de sesión como $s (debería empezar por AAAA-MM-DD-$name)" ;;
    esac
  done
done
# Todo comando /NN-nombre citado en los prompts, los comandos y los índices debe existir.
for ref in $(cat docs/prompts/*.md .claude/commands/*.md CLAUDE.md README.md 2>/dev/null \
    | grep -oE '(^|[^a-zA-Z0-9_./-])/[0-9]{2}-[a-z]+(-[a-z]+)*' | grep -oE '/[0-9]{2}-[a-z]+(-[a-z]+)*' | sort -u); do
  [ -f ".claude/commands/${ref#/}.md" ] || err "se cita el comando $ref, que no existe en .claude/commands/"
done

echo "Resultado: $errors errores, $warnings avisos"
[ "$errors" -eq 0 ]
