# 09-mejorar-prompt kit-reutilizable

- **Inicio:** 2026-11-13 · **Rama:** `feature/kit-documentos-configurables` · **Estado:** terminado

## Objetivo
Que el kit funcione en un proyecto Spring Boot con Clean Architecture que ya tiene sus propios documentos, con otra ruta y otra estructura, y que `verificar.sh` detecte cuándo los prompts leerían documentos equivocados.

## Plan aprobado
1. [x] Nota común de los 9 prompts: documentos por `DP.doc-*` y secciones por `DP.secciones`; sin rutas `docs/…` fijas.
2. [x] 7 líneas que asumían módulos (`domain/pom.xml`, `infrastructure/config`, `infrastructure/api`) pasan a `DP.modulos`.
3. [x] Claves nuevas `doc-arquitectura`, `doc-reglas`, `doc-tecnico` y `secciones` en `CLAUDE.md` y en la plantilla del kit.
4. [x] `instalar.sh` detecta los documentos existentes y no crea plantillas si ya existen.
5. [x] `verificar.sh`: documento inexistente o igual a la plantilla → error; plantilla sobrante → aviso; numeración solo con «Estándar del kit»; wrapper de `DP.build` inexistente → error.

## Decisiones
- Las 57 referencias `§N` de los prompts se conservan como numeración estándar del kit, y `DP.secciones` las traduce al proyecto. Reescribirlas una por una era un cambio grande y arriesgado para la calidad de los prompts.

## Registro
- 2026-11-13 — Prueba sobre una copia de `F4/01.-Modelos/use-case-ca-v1`, con el original intacto. Antes de la corrección: 0 errores, pero los prompts habrían leído plantillas vacías en `docs/` en lugar de los documentos reales de la raíz.
- 2026-11-13 — Fallos del propio script encontrados en la prueba: el bash 3.2 de macOS no admite comillas invertidas ni `[…]` en `${x//…}` (se cambió a `awk`), y `DP.doc-*` en el README se leía como una clave `DP.doc`.
- 2026-11-13 — Resultado: el modelo del profe, con `DP` completado, da 0 errores y 0 avisos; `contract-api` también. Los 4 casos negativos se detectan. `--actualizar` conserva el `DP` del proyecto destino.

## Pendiente al retomar
- No se ejecutó ningún prompt completo (por ejemplo, `/06-revisar dominio`) en el proyecto del profe: solo la instalación y la verificación.
