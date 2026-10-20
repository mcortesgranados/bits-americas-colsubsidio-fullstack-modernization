# 03 · Requisitos no funcionales

Cada requisito declara **cómo se verifica**. Los objetivos numéricos son metas de diseño del
prototipo; se reportarán con mediciones reales sólo cuando exista la prueba correspondiente.

## 3.1 Arquitectura y mantenibilidad

| ID | Requisito | Criterio verificable |
|----|-----------|----------------------|
| NFR-ARQ-01 | Java 17 y Spring Boot 3.x | `maven.compiler.release=17`; versión de Boot fijada en el `pom.xml` |
| NFR-ARQ-02 | Organización por funcionalidad con capas internas (`api`, `application`, `domain`) | Estructura de paquetes revisable; sin dependencias de `api` hacia `infrastructure` |
| NFR-ARQ-03 | Principios SOLID y responsabilidad única | Servicios pequeños, puertos (`DocumentStorage`, `DomainEventPublisher`, `NotificationGateway`) con adaptadores intercambiables |
| NFR-ARQ-04 | Configuración externalizada | Sin credenciales ni URLs de entorno en código; `application.yml` con variables de entorno |
| NFR-ARQ-05 | Decisiones arquitectónicas registradas | Un ADR por decisión relevante en `docs/adr/` |
| NFR-ARQ-06 | Contrato de API estable | OpenAPI versionado en `docs/api/openapi.yaml`, contrastado con los controladores |

## 3.2 Seguridad

| ID | Requisito | Criterio verificable | Estado en el prototipo |
|----|-----------|----------------------|------------------------|
| NFR-SEG-01 | Autenticación sin estado basada en JWT firmado (HS256) | Firma y expiración validadas por el resource server; secreto ≥ 32 bytes inyectado por entorno | Implementado |
| NFR-SEG-02 | Autorización por rol y por recurso | Pruebas que demuestran 401/403 para accesos indebidos (FR-AUT-04, FR-AUT-05) | Implementado |
| NFR-SEG-03 | Contraseñas con hash lento | BCrypt con factor 10; nunca se registran en logs | Implementado |
| NFR-SEG-04 | Validación de entrada en el borde | Bean Validation en DTOs; rechazo con 400 y detalle por campo | Implementado |
| NFR-SEG-05 | Errores sin filtración de detalles internos | `ProblemDetail` sin stacktrace ni SQL; `include-message: never` | Implementado |
| NFR-SEG-06 | Carga de archivos segura | Tipo MIME permitido, tamaño máximo, nombre saneado, clave generada por la aplicación (sin path traversal), checksum SHA-256 | Implementado |
| NFR-SEG-07 | CORS restringido a los orígenes de los portales | Lista explícita por configuración | Implementado |
| NFR-SEG-08 | Cabeceras de seguridad HTTP | `X-Content-Type-Options`, `Cache-Control` en respuestas sensibles, HSTS en el borde | Parcial: cabeceras de Spring Security; HSTS en el ingress |
| NFR-SEG-09 | Secretos fuera del repositorio | `.gitignore` excluye `.env`; Azure Key Vault en el pipeline | Implementado como configuración |
| NFR-SEG-10 | Protección frente a fuerza bruta | Rate limiting en el endpoint de token y bloqueo progresivo | **Sólo documentado** (se declara como pendiente de producción) |
| NFR-SEG-11 | Dependencias sin vulnerabilidades críticas | `mvn dependency-check` / `npm audit` en el pipeline | Previsto en CI |
| NFR-SEG-12 | Resistencia a inyección | Consultas derivadas y criterios JPA; sin SQL concatenado | Implementado |

Mapeo con OWASP API Security Top 10 (2023): API1 BOLA → `AccessGuard`; API2 autenticación rota →
JWT + BCrypt; API3 consumo de datos → DTOs con enmascaramiento; API4 consumo de recursos → paginación
con máximo y límite de tamaño de archivo; API5 autorización a nivel de función → `@PreAuthorize`;
API6 flujos sensibles → reglas de estado; API8 configuración → perfiles y secretos externos; API10
consumo inseguro de APIs → WebClient con timeout y reintentos acotados.

## 3.3 Privacidad y tratamiento de datos

| ID | Requisito | Criterio verificable |
|----|-----------|----------------------|
| NFR-PRI-01 | Datos exclusivamente sintéticos | Guion de demo y migraciones usan documentos, nombres y correos ficticios |
| NFR-PRI-02 | Minimización | La empresa no recibe nombre completo ni contacto del afiliado; sólo documento enmascarado y estado |
| NFR-PRI-03 | Enmascaramiento por construcción | Los DTOs no tienen campos para el dato sin enmascarar; helpers en `MaskingUtils` |
| NFR-PRI-04 | Auditoría de acceso a datos personales | Lectura de perfil por staff genera registro `PII_ACCESS` con actor y correlación |
| NFR-PRI-05 | Retención declarada | Se documenta política de retención y borrado; no se implementa job de purga en el prototipo |

## 3.4 Rendimiento y escalabilidad

| ID | Requisito | Objetivo de diseño | Verificación |
|----|-----------|--------------------|--------------|
| NFR-PER-01 | Latencia de lecturas paginadas | p95 < 300 ms con 50 registros y 100 usuarios concurrentes | Escenario k6 documentado |
| NFR-PER-02 | Latencia de registro de solicitud | p95 < 500 ms sin contar el procesamiento asíncrono | Prueba de carga |
| NFR-PER-03 | Latencia de propagación de estado | < 2 s entre commit y evento SSE | Prueba de integración con Awaitility |
| NFR-PER-04 | Cotas de memoria en streaming | Sinks con buffer acotado y heartbeat; sin acumulación por solicitud | Revisión de código y prueba de suscripción |
| NFR-PER-05 | Sin consultas N+1 en listados | Listados usan proyecciones o entidades sin colecciones ansiosas; `open-in-view=false` | Revisión de logs SQL en pruebas |
| NFR-PER-06 | Conexiones acotadas | Hikari con máximo configurable (10 por defecto) | Configuración |

## 3.5 Confiabilidad e integridad

| ID | Requisito | Criterio verificable |
|----|-----------|----------------------|
| NFR-REL-01 | Consistencia entre negocio y evento | El evento se escribe en la misma transacción que el cambio (outbox) |
| NFR-REL-02 | Entrega al menos una vez | Publicación antes de marcar como enviado; el consumidor tolera duplicados |
| NFR-REL-03 | Idempotencia demostrada | Restricción única `(message_id, consumer)` y prueba de reentrega |
| NFR-REL-04 | Aislamiento de mensajes fallidos | Reintentos con backoff exponencial y tabla `failed_message` con el error |
| NFR-REL-05 | Integridad del ciclo de vida | Transiciones inválidas rechazadas por el agregado, no por el controlador |
| NFR-REL-06 | Concurrencia optimista | `@Version` en solicitud y outbox; conflicto devuelve 409 |
| NFR-REL-07 | Migraciones reproducibles | Flyway con scripts versionados e idempotentes por versión |

## 3.6 Observabilidad

| ID | Requisito | Criterio verificable |
|----|-----------|----------------------|
| NFR-OBS-01 | Correlación de extremo a extremo | `X-Correlation-Id` aceptado o generado, devuelto en la respuesta, en logs, historial, auditoría y outbox |
| NFR-OBS-02 | Logs estructurados | Formato con marca de tiempo, nivel, logger, correlación y campos de negocio; sin datos personales |
| NFR-OBS-03 | Health checks | `/actuator/health` con probes de liveness/readiness |
| NFR-OBS-04 | Métricas mínimas | Uso de Actuator; métricas de negocio (% solicitudes pendientes, mensajes fallidos) documentadas como extensión |
| NFR-OBS-05 | Trazabilidad del trámite | Historial persistido con actor, motivo, fecha y correlación |

## 3.7 Accesibilidad y experiencia

| ID | Requisito | Criterio verificable |
|----|-----------|----------------------|
| NFR-UX-01 | Mobile first y responsive | Layout fluido desde 320 px; sin scroll horizontal; probado en 320/768/1280 |
| NFR-UX-02 | WCAG 2.1 AA | Contraste ≥ 4.5:1 en texto, foco visible, etiquetas asociadas, navegación completa por teclado, landmarks semánticos, `aria-live` en cambios de estado, enlace "saltar al contenido" |
| NFR-UX-03 | Estados de interfaz explícitos | Carga, vacío, error y reintento en cada vista de datos |
| NFR-UX-04 | Mensajes comprensibles | Errores de API traducidos a lenguaje de negocio, con código y correlación visibles para soporte |
| NFR-UX-05 | Preferencias del usuario | Respeto a `prefers-reduced-motion` y tamaño de fuente base escalable |

## 3.8 Compatibilidad

| Componente | Versión fijada | Justificación |
|------------|----------------|---------------|
| Java | 17 (`--release`) | Requisito de la prueba y LTS soportada por Spring Boot 3 |
| Spring Boot | 3.5.x | Última línea 3.x estable |
| Spring Framework | 6.2.x (gestionada por Boot) | Requiere Jakarta EE 9+ |
| MySQL | 8.0 | Soporte de Flyway, JSON y transacciones |
| H2 | 2.2.x (pruebas) | Modo MySQL para validar migraciones sin Docker |
| Angular | 18.2.x | Estable y compatible con Module Federation vía webpack |
| Node.js | 20 o 22 LTS | Requerido por Angular 18 |
| `@angular-architects/module-federation` | 18.0.x | Debe coincidir con la línea mayor de Angular |
| `@angular-builders/custom-webpack` | 18.0.x | Necesario para extender la build con ModuleFederationPlugin |
| webpack | 5.9x (el que resuelve Angular 18) | El plugin de federación no es compatible con el builder esbuild por defecto |
| Jest | 29 + `jest-preset-angular` 14.x | Pruebas de componentes y servicios |

**Regla explícita:** Angular, el plugin de federación y el builder de webpack se fijan a la **misma
línea mayor**. No se mezclan versiones ni se asume equivalencia entre Module Federation con webpack y
Native Federation; la decisión y su justificación se registran en un ADR.

## 3.9 Pruebas y calidad

| ID | Requisito | Criterio verificable |
|----|-----------|----------------------|
| NFR-TST-01 | Pruebas unitarias de dominio y reglas | Transiciones de estado, límites de monto, catálogo de documentos, enmascaramiento |
| NFR-TST-02 | Pruebas unitarias de servicios con Mockito | Casos felices y de error, verificación de interacciones |
| NFR-TST-03 | Pruebas de integración de API | Seguridad, validación, paginación, flujo completo, idempotencia |
| NFR-TST-04 | Pruebas de componentes y servicios Angular | Jest con `jest-preset-angular`; servicios HTTP con `HttpTestingController` |
| NFR-TST-05 | Resultados reales | Todo resultado de prueba reportado debe provenir de una ejecución; no se declaran pruebas verdes sin evidencia |
| NFR-TST-06 | Cobertura objetivo | ≥ 70 % de líneas en dominio y aplicación del backend; ≥ 60 % en servicios del frontend (se mide y se reporta) |

## 3.10 Operación y despliegue

| ID | Requisito | Criterio verificable |
|----|-----------|----------------------|
| NFR-OPS-01 | Ejecución local sin Azure | Perfil `local` con adaptadores en memoria/filesystem; `docker compose up` levanta MySQL y backend |
| NFR-OPS-02 | Separación real/simulado | Paquetes `transport`, `storage` y `notification` con implementaciones `local` y `azure` condicionadas por configuración |
| NFR-OPS-03 | Contenedores reproducibles | Multi-stage build, usuario no root, healthcheck, imagen ligera (JRE 17) |
| NFR-OPS-04 | Pipeline CI/CD | Build + pruebas + análisis + empaquetado + despliegue por etapas con aprobación manual a producción |
| NFR-OPS-05 | Configuración por entorno | Variables y Key Vault; ninguna credencial en el repositorio ni en la imagen |
| NFR-OPS-06 | Despliegue progresivo | Blue/green o slots con rollback documentado |

## 3.11 Límites conocidos del prototipo (declarados)

| # | Límite | Impacto | Mitigación documentada |
|---|--------|---------|------------------------|
| L1 | El registro SSE es en memoria | No funciona con varias instancias | Sustituir por Redis pub/sub o Service Bus topic; contrato sin cambios |
| L2 | El outbox se publica por sondeo cada pocos segundos | Latencia adicional de hasta el intervalo | Ajustar intervalo o usar Debezium/Change Feed en producción |
| L3 | La autenticación es de demostración | No apta para producción | Sustituir por Entra ID / B2C manteniendo resource server |
| L4 | Sin rate limiting | Riesgo de fuerza bruta | API Management o filtro dedicado |
| L5 | Sin cifrado de campos en reposo | Riesgo sobre datos personales | TDE + cifrado a nivel de columna con Key Vault |
| L6 | Las pruebas de integración usan H2 | Diferencias menores con MySQL | Perfil Testcontainers disponible cuando el daemon de Docker esté activo |
