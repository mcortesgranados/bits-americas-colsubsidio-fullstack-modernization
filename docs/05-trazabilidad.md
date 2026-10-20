# 05 · Matriz de trazabilidad

La matriz conecta cada requisito con su implementación prevista, su prueba y su evidencia en los
portales. Se actualizará al cerrar cada fase; un requisito sin prueba y sin evidencia se considera
**no entregado**.

## 5.1 Requisitos funcionales

| Requisito | Módulo backend | Endpoint / componente | Prueba prevista | Pantalla / evidencia |
|-----------|----------------|-----------------------|-----------------|----------------------|
| FR-AUT-01 | `auth` | `POST /api/v1/auth/demo-token` | `DemoTokenServiceTest`, `AuthIT` | Login del shell |
| FR-AUT-02 | `auth`, `common` | mismo endpoint + `GlobalExceptionHandler` | `AuthIT.invalidCredentials` | Mensaje de error accesible |
| FR-AUT-03 | `security` | `SecurityFilterChain` | `SecurityIT.requiresToken` | Redirección a login |
| FR-AUT-04 | `security` | `@PreAuthorize` | `SecurityIT.forbiddenByRole` | Navegación condicionada por rol |
| FR-AUT-05 | `security` | `AccessGuard` | `SecurityIT.forbiddenByOwnership` | Mensaje 403 comprensible |
| FR-AUT-06 | `auth` | `GET /api/v1/auth/me` | `AuthIT.me` | Encabezado con usuario |
| FR-AFI-01 | `affiliate` | `GET /api/v1/affiliates/me` | `AffiliateServiceTest`, `AffiliateIT` | `mfe-affiliates` · Perfil |
| FR-AFI-02 | `affiliate` | `PATCH /api/v1/affiliates/me/contact` | `AffiliateContactIT` | Formulario de contacto |
| FR-AFI-03 | `affiliate` | `GET /api/v1/affiliates/me/beneficiaries` | `AffiliateIT.beneficiaries` | `mfe-affiliates` · Beneficiarios |
| FR-AFI-04 | `subsidy` | `GET /api/v1/affiliates/me/subsidy-requests` | `SubsidyRequestQueryIT` | `mfe-affiliates` · Mis solicitudes |
| FR-AFI-05 | `subsidy` | `GET /api/v1/subsidy-requests/{id}` | `SubsidyRequestQueryIT.detail` | Detalle con línea de tiempo |
| FR-AFI-06 | `subsidy` | detalle + `GET .../documents` | `DocumentCompletenessTest` | Lista de pendientes |
| FR-AFI-07 | `notification` | `GET/PATCH .../notifications` | `NotificationIT` | Campana y bandeja |
| FR-AFI-08 | `dashboard` | `GET /api/v1/affiliates/me/dashboard` | `DashboardServiceTest` (StepVerifier) | Tarjetas del inicio |
| FR-EMP-01 | `company` | `GET /api/v1/companies/me` | `CompanyIT.profile` | `mfe-companies` · Empresa |
| FR-EMP-02 | `company` | `GET /api/v1/companies/me/workers` | `CompanyWorkerIT.list` | Tabla de trabajadores |
| FR-EMP-03 | `company` | `POST /api/v1/companies/me/workers` | `CompanyWorkerIT.createAndConflict` | Formulario de alta |
| FR-EMP-04 | `company` | `PATCH /api/v1/companies/me/workers/{id}` | `CompanyWorkerIT.updateStatus` | Acción de estado |
| FR-EMP-05 | `company`, `subsidy` | `GET /api/v1/companies/me/subsidy-requests` | `CompanySubsidyIT.minimisedData` | Bandeja administrativa |
| FR-EMP-06 | `company` | `GET /api/v1/companies/me/reports/summary` | `CompanyReportIT` | Reporte con tarjetas |
| FR-EMP-07 | `security` | `AccessGuard.requireCompanyAccess` | `SecurityIT.companyIsolation` | 403 en UI |
| FR-SOL-01 | `subsidy` | `POST /api/v1/subsidy-requests` | `SubsidyRequestServiceTest`, `SubsidyFlowIT` | Asistente de nueva solicitud |
| FR-SOL-02 | `subsidy` | generador de radicado | `RequestNumberGeneratorTest` | Radicado visible |
| FR-SOL-03 | `subsidy`, `messaging` | `OutboxService.append` | `OutboxPublisherTest` | Evidencia en logs/BD |
| FR-SOL-04 | `subsidy` | `GET /api/v1/subsidy-requests` | `SubsidyRequestQueryIT.filters` | Filtros de bandeja |
| FR-SOL-05 | `subsidy`, `storage` | `POST .../{id}/documents` | `DocumentUploadIT`, `LocalStorageTest` | Carga de documentos |
| FR-SOL-06 | `subsidy`, `storage` | validaciones de archivo | `DocumentUploadIT.rejectsInvalid` | Mensaje de error |
| FR-SOL-07 | `subsidy`, `storage` | `GET .../documents/{type}/content` | `DocumentDownloadIT` | Enlace de descarga |
| FR-SOL-08 | `subsidy` | `GET .../{id}/history` | `SubsidyHistoryIT` | Línea de tiempo |
| FR-SOL-09 | `subsidy` | `POST .../{id}/cancel` | `SubsidyRequestServiceTest.cancel` | Acción de cancelación |
| FR-SOL-10 | `subsidy` | `POST .../{id}/review` | `ReviewIT` | Swagger / Postman |
| FR-SOL-11 | `subsidy` | agregado `SubsidyRequest` | `SubsidyRequestTest.transitions` | 422 en UI |
| FR-SOL-12 | `subsidy` | `GET .../{id}/events` (SSE) | `StatusStreamIT` | Actualización en vivo |
| FR-ASY-01 | `messaging` | `SubsidyMessageHandler` | `SubsidyFlowIT` | Estado tras registrar |
| FR-ASY-02 | `messaging` | `TransactionalMessageExecutor` | `IdempotencyIT` | Sin duplicados |
| FR-ASY-03 | `messaging` | `MessageProcessingService` | `RetryAndDlqTest` | `failed_message` |
| FR-ASY-04 | `messaging` | `onDocumentUploaded` | `SubsidyFlowIT` | Paso a `IN_REVIEW` |
| FR-ASY-05 | `notification` | `NotificationService` | `NotificationIT` | Bandeja |
| FR-ASY-06 | `messaging` | `GET /api/v1/ops/failed-messages` | `OpsIT` | Swagger / Postman |

## 5.2 Requisitos no funcionales

| Requisito | Evidencia prevista |
|-----------|--------------------|
| NFR-ARQ-01…06 | `pom.xml`, estructura de paquetes, ADR, OpenAPI |
| NFR-SEG-01…07, 09, 12 | `SecurityConfig`, `AccessGuard`, `MaskingUtils`, `SecurityIT`, `DocumentUploadIT` |
| NFR-SEG-08, 10, 11 | Configuración de pipeline, documento de seguridad y hallazgos de análisis |
| NFR-PRI-01…05 | Migraciones sintéticas, DTOs, `AuditService`, documento de privacidad |
| NFR-PER-01…06 | Escenario k6 documentado, configuración de Hikari, revisión de consultas |
| NFR-REL-01…07 | `OutboxPublisher`, `TransactionalMessageExecutor`, `@Version`, Flyway e `IdempotencyIT` |
| NFR-OBS-01…05 | `CorrelationIdFilter`, logs, Actuator, historial |
| NFR-UX-01…05 | Revisión de UI, pruebas de componentes, checklist WCAG en `docs/demo/` |
| NFR-TST-01…06 | Suites Maven y Jest, reporte de ejecución real |
| NFR-OPS-01…06 | `docker-compose.yml`, Dockerfiles, pipeline, documento de despliegue |

## 5.3 Trazabilidad inversa (entregable → requisitos)

| Entregable | Requisitos que cubre |
|------------|----------------------|
| `README.md` | O1–O8, NFR-ARQ-04, NFR-OPS-01 |
| Diagramas Mermaid (arquitectura y flujo) | O1, O5, O7 |
| Backend Spring Boot | FR-AUT, FR-AFI, FR-EMP, FR-SOL, FR-ASY |
| Portales Angular | FR-AFI, FR-EMP, FR-SOL-12, NFR-UX |
| Esquema y migraciones | NFR-REL-07, NFR-PRI-01 |
| Contrato OpenAPI | NFR-ARQ-06, FR-SOL-04 |
| Pruebas JUnit/Mockito/Jest | NFR-TST |
| Docker y compose | NFR-OPS-01, NFR-OPS-03 |
| Configuraciones Azure | O6, NFR-OPS-02, NFR-OPS-05 |
| Pipeline CI/CD | NFR-SEG-11, NFR-OPS-04, NFR-OPS-06 |
| ADR | NFR-ARQ-05 |
| Plan de modernización y matriz | O7 |
| Postman | FR-SOL-10, FR-ASY-06 |
| Guion de demo | Todos los `M` |

## 5.4 Casos de uso

| Caso | Actor | Requisitos | Prueba prevista | Evidencia |
|------|-------|------------|-----------------|-----------|
| UC-01 | Afiliado | FR-AFI-01 | `AffiliateIT.profile` | `mfe-affiliates` · Perfil |
| UC-02 | Afiliado | FR-AFI-03 | `AffiliateIT.beneficiaries` | `mfe-affiliates` · Beneficiarios |
| UC-03 | Afiliado | FR-SOL-01, FR-SOL-02, FR-SOL-03 | `SubsidyFlowIT.create` | Asistente de solicitud |
| UC-04 | Afiliado | FR-SOL-05, FR-SOL-06 | `DocumentUploadIT` | Carga de evidencias |
| UC-05 | Afiliado / empresa | FR-AFI-05, FR-SOL-08, FR-SOL-12 | `StatusStreamIT` | Detalle con línea de tiempo |
| UC-06 | Afiliado | FR-SOL-09 | `SubsidyRequestServiceTest.cancel` | Acción de cancelación |
| UC-07 | Afiliado | FR-AFI-07 | `NotificationIT` | Bandeja y contador |
| UC-08 | Empresa | FR-EMP-01 | `CompanyIT.profile` | `mfe-companies` · Empresa |
| UC-09 | Empresa | FR-EMP-02, FR-EMP-03, FR-EMP-04 | `CompanyWorkerIT` | Tabla y alta de trabajadores |
| UC-10 | Empresa | FR-EMP-05 | `CompanySubsidyIT.minimisedData` | Bandeja administrativa |
| UC-11 | Empresa / auditor | FR-EMP-06 | `CompanyReportIT` | Reporte agregado |
| UC-12 | Back-office | FR-SOL-10, FR-SOL-11 | `ReviewIT` | Swagger / Postman |
| UC-13 | Consumidor | FR-ASY-01 a FR-ASY-04 | `IdempotencyIT`, `RetryAndDlqTest` | Logs y `failed_message` |

El catálogo completo, con flujos alternos y postcondiciones, está en
[08 · Casos de uso](08-casos-de-uso.md).

## 5.5 Cobertura del JD

El documento [09 · Descripción del cargo](09-descripcion-del-cargo.md) mantiene la matriz que relaciona
cada requisito de la vacante con su evidencia en el repositorio y su estado real (cubierto, parcial,
pendiente o fuera del alcance). Esa matriz es la vista de negocio de esta trazabilidad: aquí se enlaza
requisito → código → prueba; allí se enlaza requisito del JD → entregable → estado.

## 5.6 Requisitos de integración con Azure

| Requisito | Componente | Prueba prevista | Evidencia |
|-----------|------------|-----------------|-----------|
| FR-AZR-01 | `DomainEventEnvelope`, `OutboxPublisher` | `EnvelopeContractTest` | `messageId` igual al id del outbox |
| FR-AZR-02 | `MessageProcessingService` | `RetryAndDlqTest` | `failed_message` tras agotar intentos |
| FR-AZR-03 | Adaptador Event Grid (pendiente) | `DocumentEventIT` | CloudEvent con `documentId` |
| FR-AZR-04 | `DocumentStorage`, `AccessGuard` | `DocumentAccessIT` | 403 al descargar documento ajeno |
| FR-AZR-05 | Function de notificaciones (pendiente) | `NotificationIdempotencyTest` | Un solo envío por `notificationId` |
| FR-AZR-06 | `DomainEventPublisher` y `DocumentStorage` | `ParityIT` (perfil local y doble Azure) | Mismo handler en ambos transportes |
| FR-AZR-07 | `application.yml`, propiedades | `AzureProfileStartupTest` | Falla al iniciar sin cadena de conexión |
| FR-AZR-08 | `staticwebapp.config.json` (pendiente) | `SwaRoutingTest` | Recarga profunda devuelve el shell |
