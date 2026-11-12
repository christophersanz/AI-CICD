#!/usr/bin/env bash
# Instala el kit de prompts en otro proyecto (ver docs/prompts/README.md §6).
#   Uso: docs/prompts/kit/instalar.sh <proyecto-destino> [--actualizar]
#
# Copia lo que es del kit (genérico):
#   docs/prompts/NN-*.md y README.md, docs/prompts/kit/, .claude/commands/NN-*.md, docs/sessions/README.md
# Crea, solo si no existen, los documentos del proyecto (nunca los sobrescribe):
#   CLAUDE.md (plantilla + "Datos para los prompts"), docs/architecture.md, docs/business-rules.md
#   Si CLAUDE.md ya existe y no tiene "Datos para los prompts", le agrega esa sección al final.
# Sin --actualizar no reemplaza un prompt o comando que ya exista con otro contenido (lo avisa).
set -euo pipefail

dest="${1:?Uso: $0 <proyecto-destino> [--actualizar]}"
mode="${2:-}"
[ -d "$dest" ] || { echo "No existe el directorio destino: $dest" >&2; exit 1; }
dest="$(cd "$dest" && pwd)"
kit="$(cd "$(dirname "$0")" && pwd)"
src="$(cd "$kit/../../.." && pwd)"
[ "$src" != "$dest" ] || { echo "El destino es el mismo proyecto de origen." >&2; exit 1; }

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
if [ -e "$dest/CLAUDE.md" ]; then
  if grep -q "Datos para los prompts" "$dest/CLAUDE.md"; then
    echo "  se conserva: CLAUDE.md (ya tiene \"Datos para los prompts\")"
  else
    { echo; cat "$kit/CLAUDE-datos-prompts.md"; } >> "$dest/CLAUDE.md"
    echo "  CLAUDE.md: se agregó la sección \"Datos para los prompts\" al final (complétala)"
  fi
else
  { cat "$kit/CLAUDE.plantilla.md"; echo; cat "$kit/CLAUDE-datos-prompts.md"; } > "$dest/CLAUDE.md"
  created=$((created + 1)); echo "  creado desde la plantilla: CLAUDE.md"
fi
create_if_missing "$kit/architecture.plantilla.md" "$dest/docs/architecture.md"
create_if_missing "$kit/business-rules.plantilla.md" "$dest/docs/business-rules.md"

echo "Copiados: $copied · omitidos: $skipped · documentos creados: $created"
echo
echo "Siguientes pasos:"
echo "  1. Completa en CLAUDE.md la sección \"Datos para los prompts\" (cada [...])."
echo "  2. Completa docs/architecture.md y docs/business-rules.md (o usa /00-refinar-requisitos y /01-reglas-negocio)."
echo "  3. Comprueba la instalación: docs/prompts/kit/verificar.sh"
echo "  4. Abre Claude Code en la raíz del proyecto y escribe / para ver los comandos."
echo
"$dest/docs/prompts/kit/verificar.sh" "$dest" || true
