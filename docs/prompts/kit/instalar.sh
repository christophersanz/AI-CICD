#!/usr/bin/env bash
# Instala el kit de prompts en otro proyecto (ver docs/prompts/README.md §6).
#   Uso: docs/prompts/kit/instalar.sh <proyecto-destino> [--actualizar]
#
# Copia lo que es del kit (genérico):
#   docs/prompts/NN-*.md y README.md, docs/prompts/kit/, .claude/commands/NN-*.md, docs/sessions/README.md
# Documentos del proyecto (nunca los sobrescribe):
#   Busca architecture.md, business-rules.md y technical.md en docs/ y en el resto del proyecto. Si existen, deja
#   sus rutas en DP.doc-*; si no, crea docs/architecture.md y docs/business-rules.md desde las plantillas.
#   CLAUDE.md: si no existe, lo crea (plantilla + "Datos para los prompts"); si existe sin esa sección, se la agrega.
# Sin --actualizar no reemplaza un prompt o comando que ya exista con otro contenido (lo avisa).
set -euo pipefail

dest="${1:?Uso: $0 <proyecto-destino> [--actualizar]}"
mode="${2:-}"
[ -d "$dest" ] || { echo "No existe el directorio destino: $dest" >&2; exit 1; }
dest="$(cd "$dest" && pwd)"
kit="$(cd "$(dirname "$0")" && pwd)"
src="$(cd "$kit/../../.." && pwd)"
[ "$src" != "$dest" ] || { echo "El destino es el mismo proyecto de origen." >&2; exit 1; }
# El kit no viaja solo: los prompts y los comandos están fuera de kit/, en el repositorio que lo contiene.
for need in docs/prompts/01-definir.md docs/prompts/README.md .claude/commands docs/sessions/README.md; do
  if [ ! -e "$src/$need" ]; then
    echo "No se encuentra $need en $src." >&2
    echo "instalar.sh se ejecuta desde un repositorio que tenga el kit completo (docs/prompts/, .claude/commands/ y" >&2
    echo "docs/sessions/README.md): no basta con copiar la carpeta kit/. Clona el repositorio de origen y ejecútalo desde allí." >&2
    exit 1
  fi
done

copied=0 skipped=0 created=0
copy_kit_file() {               # copy_kit_file <origen> <destino>
  local from="$1" to="$2"
  mkdir -p "$(dirname "$to")"
  if [ -e "$to" ] && ! cmp -s "$from" "$to"; then
    if [ "$mode" != "--actualizar" ]; then
      echo "  omitido (ya existe y es distinto; usa --actualizar): ${to#"$dest"/}"
      skipped=$((skipped + 1)); return
    fi
  fi
  cp "$from" "$to"; copied=$((copied + 1))
}
create_if_missing() {           # create_if_missing <plantilla> <destino>
  if [ -e "$2" ]; then echo "  se conserva: ${2#"$dest"/}"; return; fi
  mkdir -p "$(dirname "$2")"; cp "$1" "$2"; created=$((created + 1))
  echo "  creado desde la plantilla: ${2#"$dest"/}"
}

echo "Kit de prompts: $src → $dest"
echo "1. Prompts, comandos y kit"
for f in "$src"/docs/prompts/[0-9][0-9]-*.md "$src/docs/prompts/README.md"; do
  copy_kit_file "$f" "$dest/docs/prompts/$(basename "$f")"
done
for f in "$kit"/*; do copy_kit_file "$f" "$dest/docs/prompts/kit/$(basename "$f")"; done
chmod +x "$dest"/docs/prompts/kit/*.sh
for f in "$src"/.claude/commands/[0-9][0-9]-*.md; do
  copy_kit_file "$f" "$dest/.claude/commands/$(basename "$f")"
done
copy_kit_file "$src/docs/sessions/README.md" "$dest/docs/sessions/README.md"
mkdir -p "$dest/docs/reviews"

echo "2. Documentos del proyecto"
find_doc() {                    # find_doc <nombre>: ruta relativa del documento si ya existe, o vacío
  if [ -f "$dest/docs/$1" ]; then echo "docs/$1"; return; fi
  (cd "$dest" && find . -maxdepth 3 -name "$1" -not -path "./docs/prompts/*" -not -path "*/target/*" \
      -not -path "*/node_modules/*" -not -path "./.git/*" | sort | head -1 | sed 's|^\./||')
}
doc_arq=$(find_doc architecture.md); doc_reglas=$(find_doc business-rules.md); doc_tec=$(find_doc technical.md)
own_docs=no
for d in "$doc_arq" "$doc_reglas"; do
  case "$d" in ""|docs/architecture.md|docs/business-rules.md) ;; *) own_docs=yes ;; esac
done
# dp_section: la plantilla de "Datos para los prompts" con las rutas de los documentos encontrados.
# Reemplazo literal con awk (index): no depende de cómo trate cada versión de bash los patrones de ${x//…}.
dp_section() {
  awk -v a="$doc_arq" -v r="$doc_reglas" -v t="$doc_tec" '
    function lit(s, old, val,   i) {
      if (val == "") return s
      i = index(s, old)
      return i ? substr(s, 1, i - 1) "`" val "`" substr(s, i + length(old)) : s
    }
    { $0 = lit($0, "`[docs/architecture.md]`", a)
      $0 = lit($0, "`[docs/business-rules.md]`", r)
      $0 = lit($0, "`[docs/technical.md]` / No aplica", t)
      print }' "$kit/CLAUDE-datos-prompts.md"
}
for pair in "arquitectura:$doc_arq" "reglas:$doc_reglas" "tecnico:$doc_tec"; do
  [ -n "${pair#*:}" ] && echo "  documento existente: ${pair#*:} → DP.doc-${pair%%:*}"
done
if [ -e "$dest/CLAUDE.md" ]; then
  if grep -q "Datos para los prompts" "$dest/CLAUDE.md"; then
    echo "  se conserva: CLAUDE.md (ya tiene \"Datos para los prompts\")"
  else
    { echo; dp_section; } >> "$dest/CLAUDE.md"
    echo "  CLAUDE.md: se agregó la sección \"Datos para los prompts\" al final (complétala)"
  fi
else
  { cat "$kit/CLAUDE.plantilla.md"; echo; dp_section; } > "$dest/CLAUDE.md"
  created=$((created + 1)); echo "  creado desde la plantilla: CLAUDE.md"
fi
[ -n "$doc_arq" ] || create_if_missing "$kit/architecture.plantilla.md" "$dest/docs/architecture.md"
[ -n "$doc_reglas" ] || create_if_missing "$kit/business-rules.plantilla.md" "$dest/docs/business-rules.md"
if [ "$own_docs" = yes ]; then
  echo "  AVISO: los documentos no siguen la estructura del kit. Completa DP.secciones con la sección real de cada"
  echo "         concepto (reglas, estados, casos de uso, catálogo de errores...) y el formato de los ID de las reglas."
fi

echo "Copiados: $copied · omitidos: $skipped · documentos creados: $created"
echo
echo "Siguientes pasos:"
echo "  1. Completa en CLAUDE.md la sección \"Datos para los prompts\" (cada [...])."
echo "  2. Completa los documentos de DP.doc-arquitectura y DP.doc-reglas (o usa /01-definir requisitos y /01-definir reglas)."
echo "  3. Comprueba la instalación: docs/prompts/kit/verificar.sh"
echo "  4. Abre Claude Code en la raíz del proyecto y escribe / para ver los comandos."
echo
"$dest/docs/prompts/kit/verificar.sh" "$dest" || true
