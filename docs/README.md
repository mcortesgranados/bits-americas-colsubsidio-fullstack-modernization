# Documentación del proyecto

Repositorio: `bits-americas-colsubsidio-fullstack-modernization`
Versión de la línea base documental: **1.0** — 2026-10-20

## Aviso importante

Este repositorio es una **prueba técnica independiente**. No reproduce la arquitectura interna de
Colsubsidio, no se conecta a sus sistemas, datos o APIs, y no utiliza información personal real.
Todos los datos son sintéticos y todas las reglas de negocio están marcadas como **reglas de
demostración**.

## Orden de lectura

| # | Documento | Contenido |
|---|-----------|-----------|
| 01 | [Contexto y alcance](01-contexto-y-alcance.md) | Problema, objetivo, personas, alcance dentro/fuera, supuestos, glosario |
| 02 | [Requisitos funcionales](02-requisitos-funcionales.md) | Requisitos por portal y por flujo, prioridad y criterios de aceptación |
| 03 | [Requisitos no funcionales](03-requisitos-no-funcionales.md) | Seguridad, rendimiento, confiabilidad, accesibilidad, observabilidad, pruebas |
| 04 | [Arquitectura baseline](04-arquitectura-baseline.md) | Contexto, contenedores, módulos, decisiones, contratos de API, modelo de datos |
| 05 | [Matriz de trazabilidad](05-trazabilidad.md) | Requisito → módulo → endpoint → prueba → pantalla → entregable |
| 06 | [Plan de entrega, riesgos y puertas de calidad](06-plan-entrega.md) | Fases, entregables, criterios de salida y riesgos |
| 07 | [Esqueleto de código existente](07-inventario-codigo-preliminar.md) | Inventario y estado del código ya generado, sin compromiso de diseño final |
| 08 | [Casos de uso](08-casos-de-uso.md) | Catálogo UC-01…UC-13 con actores, flujos principal y alternos, postcondiciones y requisitos |

Documentos previstos para fases posteriores (aún no creados):

- `docs/adr/ADR-0001..0008.md` — decisiones arquitectónicas.
- `docs/modernizacion/` — plan de migración Java 8 / Boot 2.3 → Java 17 / Boot 3.x, matriz de
  compatibilidad y recetas OpenRewrite.
- `docs/api/openapi.yaml` — contrato OpenAPI versionado.
- `docs/demo/guion-demo.md` — guion de demostración y escenarios de prueba.
- `docs/seguridad/modelo-de-seguridad.md` — modelo de amenazas y controles.

## Estado del repositorio

El código actual es un **esqueleto preliminar** escrito antes de cerrar esta línea base. Se conserva
íntegro porque acelera la Fase 2, pero **no se considera aprobado** hasta contrastarlo contra los
requisitos 01–06.

| Área | Estado | Observación |
|------|--------|-------------|
| Documentación de requisitos | Completada (v1.0) | Este conjunto de documentos |
| Backend: seguridad, dominio, repositorios, mensajería, almacenamiento | Esqueleto | No compilado todavía; faltan servicios y controladores de solicitudes/empresas |
| Backend: migraciones MySQL, pruebas, OpenAPI | Pendiente | Fase 2–3 |
| Frontend Angular + Module Federation | Pendiente | Fase 4 |
| Azure Functions, Docker, pipelines, Postman | Pendiente | Fase 5–6 |
| ADR, modernización, guion de demo | Pendiente | Fase 7 |

## Control de cambios

| Versión | Fecha | Cambio |
|---------|-------|--------|
| 1.0 | 2026-10-20 | Línea base de requisitos, arquitectura y plan antes de continuar con la implementación |
