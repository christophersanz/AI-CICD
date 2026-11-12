#!/usr/bin/env bash
# Comprueba que un proyecto tiene lo que necesitan los prompts del kit (ver docs/prompts/README.md §6).
#   Uso: docs/prompts/kit/verificar.sh [proyecto]   (por defecto, la raíz del repositorio actual)
# Errores (exit 1): falta un documento, una clave DP que usa algún prompt, un comando o su prompt,
#                   o hay dos archivos para el mismo prompt.
# Avisos: valores DP sin completar ([...]) y secciones numeradas que citan los prompts y no existen.
set -uo pipefail

root="${1:-$(cd "$(dirname "$0")/../../.." && pwd)}"
cd "$root" || exit 1
errors=0 warnings=0
err()  { echo "  ERROR: $*"; errors=$((errors + 1)); }
warn() { echo "  AVISO: $*"; warnings=$((warnings + 1)); }

echo "Verificando el kit de prompts en $root"

echo "1. Documentos"
for f in CLAUDE.md docs/architecture.md docs/business-rules.md docs/prompts/README.md docs/sessions/README.md; do
  [ -f "$f" ] || err "falta $f"
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

echo "3. Secciones que citan los prompts"
check_sections() {             # check_sections <archivo> <secciones...>
  local file="$1"; shift
  [ -f "$file" ] || return
  for s in "$@"; do grep -qE "^#{2,3} $s[. ]" "$file" || warn "$file no tiene la sección $s"; done
}
check_sections docs/architecture.md 2 3 5 6 7 9
check_sections docs/business-rules.md 1 2 3 4 4.1 5 6 7 8 9

echo "4. Prompts y comandos"
dups=$(ls docs/prompts/ 2>/dev/null | grep -E '^[0-9]{2}-' | sed -E 's/(-[0-9]{2})?\.md$//' | sort | uniq -d)
[ -z "$dups" ] || err "hay más de un archivo para: $dups"
ls docs/prompts/ 2>/dev/null | grep -qE '^[0-9]{2}-.*-[0-9]{2}\.md$' && err "hay prompts con versión en el nombre (un prompt = un archivo NN-nombre.md)"
for p in docs/prompts/[0-9][0-9]-*.md; do
  [ -e "$p" ] || { err "no hay prompts en docs/prompts/"; break; }
  name=$(basename "$p" .md)
  [ "$name" = "09-reglas-revision" ] && continue
  [ -f ".claude/commands/$name.md" ] || err "falta el comando .claude/commands/$name.md"
done
for c in .claude/commands/[0-9][0-9]-*.md; do
  [ -e "$c" ] || break
  for ref in $(grep -o 'docs/prompts/[0-9a-z-]*\.md' "$c" | sort -u); do
    [ -f "$ref" ] || err "$c apunta a $ref, que no existe"
  done
done

echo "Resultado: $errors errores, $warnings avisos"
[ "$errors" -eq 0 ]
