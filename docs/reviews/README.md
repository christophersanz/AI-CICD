# Revisiones

Informes de `/06-revisar` y `/07-corregir` (`docs/prompts/`). Cada **ronda** es una revisión completa o parcial del proyecto, con su carpeta `AAAA-MM-DD-ronda-NNN/`: la fecha es la del inicio de la ronda. Su `README.md` resume el estado de cada paso. Las revisiones de un PR van en `pr/`.

| Ronda | Fecha | Alcance | Resultado |
|---|---|---|---|
| [001](2026-09-26-ronda-001/README.md) | 2026-09-26 | Revisión integral con el prompt del curso `001-review-01.md` | 20 hallazgos (1 alto, 6 medios, 13 bajos), corregidos en los PR siguientes |
| [002](2026-11-12-ronda-002/README.md) | 2026-11-12 | Los 9 pasos: 7 capas, correcciones y veredicto final | **READY**: 0 ERROR; 6 WARNING (3 corregidos y 3 diferidos) y 8 INFO |
| [003](2026-11-13-ronda-003/README.md) | 2026-11-13 | Pasos 1–3 (dominio, aplicación e infraestructura); **cerrada** el 2026-11-14 sin los pasos 4–9 | 0 ERROR, 0 WARNING y 8 INFO opcionales; sin veredicto final |

**Cómo leer una ronda:** primero su `README.md`; después `09-veredicto-final.md`, si existe, y el informe de la capa que interese. Los informes se nombran por lo que contienen: `01-revision-dominio.md` … `07-revision-integral.md`, `08-correcciones-aplicadas.md` y `09-veredicto-final.md`.
