## Qué cambia

<!-- Resumen breve y motivo. Referencia las reglas (BR-XXX-NNN), ADRs o issues relacionados. -->

## Checklist

- [ ] La rama sale de `main` (`feature/*`, `fix/*` o `docs/*`) y el CI está en verde.
- [ ] Se respeta la regla de dependencia y las reglas de `docs/architecture.md` §3.
- [ ] Cada caso de uso nuevo o modificado tiene sus pruebas (`should<Resultado>When<Condición>`).
- [ ] Si cambia una regla de negocio, `docs/business-rules.md` está actualizado.
- [ ] Si hay una decisión arquitectónica, hay un ADR en `docs/adr/`.
- [ ] Sin secretos, contraseñas ni tokens en el código, los logs ni la configuración versionada.
