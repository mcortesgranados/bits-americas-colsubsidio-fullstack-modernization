# 07 · Inventario del código preliminar

## 7.1 Estado

El repositorio contiene un **esqueleto de backend** generado antes de cerrar la línea base de
requisitos. Se conserva completo porque adelanta trabajo de la Fase 2, pero:

- **no ha sido compilado todavía**;
- **no se declara funcional**;
- **se ajustará** contra los documentos 01–06 antes de considerarlo parte de la entrega.

Ningún archivo se elimina sin dejar constancia aquí de la razón y del reemplazo.

## 7.2 Archivos generados

| Paquete / recurso | Contenido | Estado | Observación |
|-------------------|-----------|--------|-------------|
| `ColsubsidioPlatformApplication` | Arranque, `@EnableScheduling`, `@ConfigurationPropertiesScan` | Completo | — |
| `backend/pom.xml` | Boot 3.5.16, Java 17 release, web + webflux, JPA, security, oauth2-resource-server, Flyway, MySQL, H2, springdoc, Azure SDK | Completo | Validar versiones al compilar |
| `application.yml` | Datasource, JPA, Flyway, seguridad, mensajería, almacenamiento, notificaciones, CORS | Completo | Sin secretos reales; valores por defecto sólo para demo |
| `common/api`, `common/error` | `PageResponse`, excepciones, `GlobalExceptionHandler` (RFC 7807) | Completo | Revisar mapeo de `AccessDeniedException` |
| `common/correlation` | Filtro y utilidades de correlación | Completo | Propaga a MDC y cabecera |
| `common/privacy` | `MaskingUtils` | Completo | Documento, correo, teléfono y NIT |
| `security` | `SecurityConfig`, `JwtConfig`, `CurrentUser`, `CurrentUserProvider`, `AccessGuard` | Completo | Falta prueba de extremo a extremo |
| `auth/domain` | `AppUser`, `Role`, repositorio | Completo | Falta emisión de token y controlador |
| `affiliate` | Entidades, repositorios, especificaciones, DTOs, servicio, controlador | Parcial | El controlador referencia `SubsidyRequestService` (aún no existe) |
| `company/domain` | `Company`, `CompanyWorker`, enums, repositorios | Completo | Faltan servicio, DTOs y controlador |
| `subsidy/domain` | Agregado, documentos, historial, repositorios, especificaciones, estados | Completo | Base sólida para Fase 2 |
| `subsidy/application` | `SubsidyDemoRules`, `SubsidyStatusStreamRegistry` | Parcial | Faltan servicios de solicitud y documentos |
| `messaging` | Outbox, envelope, publicador, consumidor, idempotencia, fallidos, adaptadores local y Azure | Completo | Requiere pruebas de idempotencia y reintentos |
| `notification` | Entidad, repositorio, servicio, gateways local y Azure | Completo | Falta controlador propio (se expone en afiliados) |
| `storage` | Puerto, DTO y adaptadores local/Azure | Completo | Falta prueba de path traversal y checksum |
| `audit` | Entidad, repositorio, servicio y catálogo de acciones | Completo | Transacción independiente documentada |
| `config` | Propiedades tipadas y `WebClient` | Completo | Revisar validación de propiedades obligatorias |
| `db/migration` | `V1` esquema, `V2` índices, `V3` datos sintéticos | **Completo** | Historial de Flyway marcado en la versión 3 |
| Pruebas | 9 pruebas de backend (JUnit/Mockito) y 7 de frontend (Jest) | **Parcial** | Faltan integración con MySQL real y E2E |
| Frontend | Workspace Angular 18 con `shell`, `mfe-affiliates` y `shared-core`; login y perfil implementados | **Parcial** | Faltan el remoto de empresas, los demás UC y el pipeline |

## 7.3 Brechas para que el backend compile

| # | Brecha | Acción en Fase 2 |
|---|--------|------------------|
| B1 | `SubsidyRequestService` referenciado por `AffiliateController` y por el manejador de eventos | **Resuelto en parte**: `SubsidyRequestService.listForAffiliate` y `SubsidyRequestSummaryResponse`; las operaciones de escritura siguen pendientes |
| B2 | Servicio, DTOs y controlador de empresas | Implementar `CompanyService`, `CompanyController` y DTOs |
| B3 | Emisión de JWT, controlador de autenticación e inicializador de usuarios demo | **Resuelto**: `AuthController` con `POST /api/v1/auth/demo-token`, `AuthService` y `DemoUserInitializer` con BCrypt |
| B4 | DTOs de solicitud (`SubsidyRequestSummaryResponse`, detalle, documentos, historial, revisión) | **Parcial**: el resumen existe; faltan detalle, documentos, historial y revisión |
| B5 | Tablero reactivo del afiliado | Crear módulo `dashboard` con `Mono.zip` |
| B6 | Controlador de operación (outbox y fallidos) | Crear `ops` |
| B7 | Migraciones Flyway | **Resuelto**: `V1__esquema_inicial.sql`, `V2__indices_de_consulta.sql` y `V3__datos_sinteticos.sql` |

## 7.4 Ajustes detectados al contrastar con los requisitos

| # | Hallazgo | Requisito afectado | Resolución propuesta |
|---|----------|--------------------|----------------------|
| A1 | `AffiliateController.myBeneficiaries()` obtiene el id llamando a `ownProfile()` (consulta redundante) | NFR-PER-01 | Exponer `currentAffiliateId()` en el servicio y usarlo |
| A2 | `updateContact` devuelve `companyName` nulo | FR-AFI-01 | Resolver el nombre de empresa en la misma consulta |
| A3 | Import sin uso (`SimpleAuthorityMapper`) en `SecurityConfig` | Calidad | Eliminar y activar comprobación de imports en revisión |
| A4 | Formato de radicado `SOL-<año>-<secuencia>` aún no implementado | FR-SOL-02 | Usar tabla de secuencia (`document_sequence`) para garantizar unicidad sin colisiones |
| A5 | No existe prueba de que el evento SSE llegue tras el commit | FR-SOL-12 | Prueba de integración con `StepVerifier` y Awaitility |
| A6 | El outbox usa sondeo; el intervalo por defecto es 3 s | NFR-PER-03 | Documentar la latencia esperada y permitir ajuste por configuración |
| A7 | El consumidor registra auditoría en transacción independiente | NFR-REL-01 | Verificar que un fallo del handler no deje auditoría de una transición no confirmada |

## 7.5 Regla de conservación

1. Todo archivo existente se **refactoriza**, no se reescribe desde cero, salvo que el documento 04
   contradiga su diseño.
2. Si un archivo debe desaparecer, se anota en este documento con la razón y el reemplazo.
3. El historial de Git conserva la evolución; el repositorio entregado no incluye código muerto.
