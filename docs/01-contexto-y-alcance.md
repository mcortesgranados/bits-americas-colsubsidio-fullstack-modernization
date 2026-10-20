# 01 · Contexto y alcance

## 1. Problema que se quiere resolver

Una caja de compensación familiar administra información de afiliados, de las empresas aportantes y
de las solicitudes de subsidio que los afiliados presentan. En plataformas heredadas esa información
suele estar fragmentada: el afiliado no tiene visibilidad de su trámite, la empresa no puede consultar
el estado de sus trabajadores sin pedir reportes manuales, y el back-office depende de procesos
batch poco observables.

La prueba técnica consiste en demostrar, con un prototipo ejecutable, cómo se moderniza ese tipo de
plataforma: APIs REST y reactivas sobre Java 17 / Spring Boot 3.x, dos portales construidos con
microfrontends Angular, mensajería asíncrona confiable y un camino claro de despliegue en Azure.

**Este prototipo es independiente.** No reproduce la arquitectura interna de Colsubsidio ni usa sus
sistemas, datos o APIs. Cualquier parecido funcional es intencionalmente genérico.

## 2. Objetivo general

Entregar un repositorio demostrable, ejecutable y documentado que permita evaluar criterio
arquitectónico, calidad de código, seguridad, pruebas y capacidad de modernización en un contexto
empresarial realista.

## 3. Objetivos evaluables

| # | Objetivo | Evidencia esperada |
|---|----------|--------------------|
| O1 | Separar responsabilidades y modelar el dominio con reglas explícitas | Módulos por funcionalidad, agregados con transiciones de estado, reglas de demostración aisladas |
| O2 | Exponer APIs REST consistentes y seguras | Contrato OpenAPI, paginación y filtros uniformes, errores RFC 7807, control de acceso por rol y por recurso |
| O3 | Usar reactividad donde aporta valor | SSE de cambios de estado, agregación paralela del tablero, cliente HTTP saliente con timeout y reintentos |
| O4 | Garantizar consistencia entre base de datos y mensajería | Patrón outbox transaccional, consumidor idempotente, reintentos con backoff y cola de fallidos |
| O5 | Construir portales desacoplados | Shell/host con Module Federation, contratos tipados, aislamiento y manejo de fallos de carga |
| O6 | Ejecutar sin suscripción de Azure | Adaptadores locales para mensajería, almacenamiento y notificaciones, con las integraciones reales separadas por perfil |
| O7 | Demostrar una ruta de modernización | Plan Java 8 → 17, javax → jakarta, matriz de compatibilidad, recetas OpenRewrite y estrategia de rollback |
| O8 | Tratar datos personales con criterio | Minimización, enmascaramiento en todas las respuestas, auditoría de accesos y datos sintéticos |

## 4. Personas y roles

| Rol técnico | Persona | Necesidad principal | Portal |
|-------------|---------|---------------------|--------|
| `ROLE_AFFILIATE` | Afiliado cotizante | Ver su información, beneficiarios, solicitudes, requisitos y notificaciones | Portal de afiliados |
| `ROLE_COMPANY` | Administrador de empresa aportante | Ver datos de la empresa, registrar trabajadores de demostración, consultar novedades y estado de trámites | Portal de empresas |
| `ROLE_BACKOFFICE` | Analista de subsidios | Consultar solicitudes, revisar documentos y registrar una decisión **de demostración** | API / Swagger (sin portal propio) |
| `ROLE_AUDITOR` | Auditor | Consultar información agregada y trazas sin modificar datos | API / Swagger |
| `system:subsidy-consumer` | Consumidor asíncrono | Clasificar solicitudes y actualizar estados de forma idempotente | Interno |

El prototipo **no** incluye un portal de back-office: la operación de revisión se expone por API y se
documenta en Swagger y en la colección Postman. Se declara como decisión de alcance, no como omisión.

## 5. Alcance incluido

### 5.1 Funcional

1. Autenticación de demostración con emisión de JWT y autorización por rol y por recurso.
2. Portal de afiliados: perfil, beneficiarios, solicitudes, documentos, historial y notificaciones.
3. Portal de empresas: perfil, registro de trabajadores de demostración, consulta de solicitudes y
   reporte administrativo agregado.
4. Registro de solicitud de subsidio con validaciones, persistencia y publicación de evento.
5. Procesamiento asíncrono idempotente con actualización de estado y trazabilidad.
6. Carga de documentos con validación de tipo y tamaño, checksum y almacenamiento desacoplado.
7. Consulta de estado en tiempo real mediante Server-Sent Events.
8. Auditoría de operaciones relevantes y acceso a datos personales.

### 5.2 Técnico

- Backend Java 17 + Spring Boot 3.x (MVC para REST, Reactor/WebFlux para streaming y agregación).
- MySQL 8 con migraciones versionadas (Flyway).
- Seguridad OAuth2 Resource Server con JWT (HS256) emitidos por un endpoint de demostración.
- OpenAPI 3 documentado y publicado en Swagger UI.
- Pruebas unitarias (JUnit + Mockito) y de integración (Spring Boot Test + MockMvc + H2 en modo MySQL).
- Dos portales Angular con shell/host y módulos remotos por Module Federation.
- Pruebas de componentes y servicios con Jest.
- Docker y `docker-compose` para ejecución local.
- Pipeline CI/CD de Azure DevOps, Functions, Service Bus, Event Grid, Blob Storage y Static Web Apps
  como configuración de ejemplo, no como dependencia de ejecución.

## 6. Fuera de alcance (explícito)

| Excluido | Motivo |
|----------|--------|
| Integración con sistemas reales de Colsubsidio | La prueba es un prototipo independiente |
| Decisiones reales de aprobación o desembolso | Sería irresponsable simular decisiones oficiales |
| Pasarela de pagos, facturación o dispersión de recursos | No aporta a los objetivos evaluables |
| Motor de reglas configurable (DMN) | Se implementan reglas de demostración en código aislado y testeable |
| Portal de back-office completo | Se cubre por API, Swagger y Postman; se declara la decisión |
| Aplicación móvil nativa | Los portales son responsive mobile first |
| Alta disponibilidad multi-región real | Se documenta el diseño, no se aprovisiona infraestructura |
| Autenticación con Entra ID real | Se documenta la ruta de sustitución del proveedor de identidad |
| Cifrado de campos a nivel de base de datos (TDE/KMS) | Se documenta como control de producción pendiente |

## 7. Supuestos y restricciones del entorno

| # | Supuesto / restricción | Impacto |
|---|------------------------|---------|
| S1 | JDK 17 como `--release` de compilación; el entorno local puede tener un JDK superior | El código no debe usar APIs posteriores a 17 |
| S2 | Node.js 20+ y Angular 18 para el frontend | Versión estable y compatible con Module Federation vía webpack |
| S3 | MySQL 8 en ejecución local vía Docker; H2 en modo MySQL para pruebas | Las migraciones deben ser SQL portable |
| S4 | No se requiere suscripción de Azure para ejecutar | Perfiles `local` por defecto; `azure` opt-in |
| S5 | El daemon de Docker puede no estar disponible en la máquina de evaluación | Se documenta ejecución con y sin Docker; las pruebas no dependen de Testcontainers |
| S6 | Los datos son sintéticos y se generan por migración o por inicializador idempotente | Prohibido usar información personal real |
| S7 | El prototipo se evalúa como código, no como servicio en producción | Se prioriza claridad, pruebas y trazabilidad |

## 8. Reglas de demostración (no son políticas de Colsubsidio)

| ID | Regla | Valor | Ubicación prevista |
|----|-------|-------|--------------------|
| RD-01 | Afiliado debe estar `ACTIVE` para registrar solicitud | — | `SubsidyRequestService` |
| RD-02 | Una sola solicitud abierta por tipo y afiliado | — | `SubsidyRequestService` |
| RD-03 | Monto máximo por tipo | Vivienda 4.500.000 · Educación 1.800.000 · Recreación 900.000 · Crédito 3.000.000 COP | `SubsidyDemoRules` |
| RD-04 | Crédito solo para categoría A o B | — | `SubsidyDemoRules` |
| RD-05 | Educación exige al menos un beneficiario registrado | — | `SubsidyRequestService` |
| RD-06 | Catálogo de documentos por tipo | Vivienda: identidad, ingresos, banco · Educación: identidad, ingresos, matrícula · Recreación: identidad · Crédito: identidad, ingresos, banco | `SubsidyDemoRules` |
| RD-07 | Clasificación automática solo a `PENDING_DOCUMENTS` o `IN_REVIEW` | Nunca aprueba | `SubsidyMessageHandler` |
| RD-08 | Decisión final | Solo un usuario `ROLE_BACKOFFICE`, marcada como `APPROVED_DEMO` / `REJECTED_DEMO` | `SubsidyRequestService.review` |

Toda regla debe aparecer en el código con la marca `DEMONSTRATION` o `DEMO` y repetirse en la
respuesta de la API cuando el cliente necesite explicar el resultado.

## 9. Glosario

| Término | Definición en este prototipo |
|---------|------------------------------|
| Afiliado | Persona registrada en la caja de compensación, con categoría y empresa asociada |
| Beneficiario | Persona a cargo del afiliado (hijo, cónyuge, etc.) |
| Empresa aportante | Persona jurídica que vincula trabajadores y reporta novedades |
| Trabajador | Registro de demostración que la empresa administra en su portal |
| Novedad | Cambio reportado por la empresa (ingreso, retiro); en el prototipo se limita al estado del trabajador |
| Solicitud de subsidio | Trámite con tipo, monto, documentos y ciclo de vida |
| Documento requerido | Evidencia exigida por el catálogo de demostración |
| Outbox | Tabla transaccional que garantiza publicación de eventos sin escritura dual |
| Idempotencia | Propiedad por la que un mensaje repetido no produce un segundo efecto de negocio |

## 10. Criterios de éxito de la entrega

1. El repositorio se clona, se levanta con MySQL (o Docker) y responde el flujo completo de la
   sección 5 sin pasos manuales no documentados.
2. Todas las pruebas declaradas se ejecutan y su resultado se reporta con la salida real.
3. El contrato OpenAPI coincide con los endpoints implementados.
4. Los portales funcionan de forma independiente y el fallo de un remoto no bloquea al shell.
5. La documentación de modernización permite ejecutar la migración sin ambigüedad.
6. Ninguna respuesta HTTP expone datos personales sin enmascarar.

## 11. Decisiones que se validarán con el evaluador

| # | Pregunta | Supuesto actual |
|---|----------|-----------------|
| Q1 | ¿Se espera portal de back-office? | No; se cubre por API y Swagger |
| Q2 | ¿Se prefiere Native Federation sobre webpack Module Federation? | No; se usa Module Federation con webpack por compatibilidad declarada con Angular 18 |
| Q3 | ¿Se requiere autenticación contra un IdP real? | No; JWT de demostración con ruta documentada a Entra ID |
| Q4 | ¿Se exige cobertura mínima de pruebas? | Se propone 70 % en dominio/aplicación y pruebas de todos los endpoints críticos |
