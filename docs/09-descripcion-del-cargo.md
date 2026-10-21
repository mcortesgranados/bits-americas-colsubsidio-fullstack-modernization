# 09 · Descripción del cargo (JD)

Texto de referencia de la vacante a la que responde esta prueba técnica. Se reproduce **sin
alteraciones** y se usa como entrada para verificar cobertura: qué exige el rol, qué demuestra hoy el
repositorio y qué queda pendiente.

> Este documento no implica vinculación laboral ni reproduce documentación interna del cliente. El JD
> es el texto de la convocatoria.

## 9.1 Texto original

<div class="jd-document">
<p class="jd-confidential">TEXTO ORIGINAL DE LA VACANTE · USO DE REFERENCIA</p>
<h3>AI CONTEXT DESARROLLADOR FULL STACK SENIOR JAVA (SPRING BOOT / WEBFLUX) + ANGULAR, CON AZURE Y MICROFRONTENDS</h3>
<p class="jd-meta"><span>Remoto</span><span>2,5 meses</span><span>160 h/mes</span><span>1 recurso</span><span>8:00–17:00</span></p>

<h4>Objetivo del rol</h4>
<p>Modernizar y extender la plataforma del cliente de punta a punta: ejecutar el upgrade progresivo de Java y Spring Boot, construir APIs REST y reactivas integradas con servicios serverless y de mensajería en Azure, y estructurar la base de dos portales con arquitectura de microfrontends en Angular, dejando estándares que el equipo del cliente pueda seguir.</p>

<h4>Responsabilidades principales</h4>
<ul class="jd-list">
<li>Planear y ejecutar el upgrade progresivo desde Spring Boot 2.3.x y Java 8 hacia versiones recientes, resolviendo incompatibilidades (javax a jakarta, Spring Security, dependencias) sin afectar la operación.</li>
<li>Desarrollar APIs RESTful con Spring (MVC y WebFlux), documentadas con Swagger/OpenAPI.</li>
<li>Implementar Azure Functions e integración por colas y eventos con Azure Service Bus y Event Grid; usar Azure Storage (Blob, Table) según el caso de uso, y MySQL como base relacional.</li>
<li>Diseñar y construir la base de los dos portales de microfrontends: shell/host, módulos remotos con Module Federation, dependencias compartidas y comunicación entre microfrontends.</li>
<li>Desarrollar aplicaciones y web components en Angular (15 o superior) con RxJS, maquetación responsive mobile first (Flexbox, SASS/LESS/CSS) y criterios de accesibilidad.</li>
<li>Desplegar los portales en Azure Static Web Apps y los servicios con Docker y pipelines de Azure DevOps.</li>
<li>Escribir pruebas unitarias con JUnit (backend) y Jest (frontend).</li>
<li>Definir y documentar lineamientos técnicos (patrones de diseño, SOLID, estructura de proyectos) y diagramas en Mermaid.</li>
<li>Revisar código y acompañar técnicamente al equipo del cliente.</li>
</ul>

<h4 class="jd-required">Requisitos técnicos obligatorios</h4>
<ul class="jd-list">
<li><strong>Lenguajes:</strong> Java (8, 11 y 17, con experiencia real migrando entre versiones), TypeScript y JavaScript moderno (APIs recientes del navegador y de Node.js).</li>
<li><strong>Backend:</strong> Spring Boot (2.x y 3.x), Spring Web, Spring WebFlux (Project Reactor), APIs RESTful, Swagger/OpenAPI.</li>
<li><strong>Frontend:</strong> Angular 15 o superior, RxJS, HTML5, CSS3, SASS, Flexbox, diseño responsive mobile first.</li>
<li><strong>Microfrontends:</strong> Module Federation y web components.</li>
<li><strong>Base de datos:</strong> MySQL.</li>
<li><strong>Cloud:</strong> Azure Functions, Azure Service Bus, Azure Event Grid, Azure Storage Account (Blob, Table), Azure Static Web Apps.</li>
<li><strong>DevOps / CI/CD:</strong> Docker, Azure DevOps (Repos, Pipelines).</li>
<li><strong>Testing:</strong> JUnit, Jest.</li>
<li><strong>Herramientas:</strong> Git (ramas, merge, rebase, resolución de conflictos), VS Code, Postman o Insomnia.</li>
<li><strong>Prácticas:</strong> patrones de diseño, principios SOLID, programación funcional (lambdas) y reactiva.</li>
</ul>

<h4 class="jd-preferred">Requisitos técnicos deseables</h4>
<ul class="jd-list">
<li>Java 25 y Spring Boot 4.</li>
<li>React y Stencil para microfrontends y web components.</li>
<li>Progressive Web Apps (PWA) y WebSockets.</li>
<li>LESS.</li>
<li>Accesibilidad web formal (WCAG 2.x).</li>
<li>Diagramación con Mermaid.</li>
<li>Herramientas de migración automatizada (por ejemplo, OpenRewrite).</li>
<li>Certificación Azure (AZ-204 o AZ-900).</li>
<li>Uso de asistentes de IA para refactorización, pruebas y documentación, con control humano.</li>
</ul>

<h4>Requisitos blandos</h4>
<ul class="jd-list">
<li>Autonomía alta: debe producir desde la primera semana por la corta duración del servicio.</li>
<li>Criterio técnico para tomar y sustentar decisiones de arquitectura ante el cliente.</li>
<li>Capacidad de priorizar entre frentes (migración, Azure y portales) con un solo recurso.</li>
<li>Comunicación clara y por escrito, propia de un trabajo remoto.</li>
<li>Documentación técnica de las decisiones y estándares definidos.</li>
</ul>

<h4>Experiencia mínima</h4>
<ul class="jd-list">
<li>Mínimo exigido: 5 años como desarrollador full stack.</li>
<li>Al menos 4 años en Java/Spring, incluida al menos una migración de versión de Spring Boot o Java.</li>
<li>Al menos 3 años en Angular.</li>
<li>Al menos un proyecto con microfrontends (Module Federation) y experiencia comprobable en Azure (Functions y Service Bus).</li>
</ul>

<h4>Formación, modalidad y condiciones</h4>
<p><strong>Formación:</strong> profesional, tecnólogo o estudiante de últimos semestres con experiencia sólida en ingeniería de sistemas o afines.</p>
<p><strong>Modalidad:</strong> remoto. Horario de 8:00 a 17:00.</p>
<p><strong>Duración:</strong> 2,5 meses. <strong>Dedicación:</strong> 160 horas/mes (tiempo completo). <strong>Cantidad de recursos:</strong> 1.</p>
</div>

### 9.1.1 Comparativa del bloque Cloud

Cuadro de análisis, **no forma parte del texto original**. Contrasta las cinco tecnologías citadas con lo
que la cuenta gratuita de Azure incluye hoy, y con el estado real en este repositorio.

| Tecnología del JD | ¿Free tier? | Capacidad incluida | Qué implica en la práctica | Estado en el prototipo |
|-------------------|-------------|--------------------|----------------------------|------------------------|
| **Azure Functions** (plan Consumption) | <span class="ft ft--yes">✅ Sí · permanente</span> | 1 000 000 de ejecuciones y 400 000 GB-s al mes | Unas 33 000 ejecuciones al día sin coste | Adaptador de notificación previsto; no hay código de Function |
| **Azure Functions** (plan Flex Consumption) | <span class="ft ft--yes">✅ Sí · permanente</span> | 250 000 ejecuciones y 100 000 GB-s al mes | Menos ejecuciones que Consumption; útil si se quiere escala a cero por instancia | Alternativa de plan, no usada |
| **Azure Service Bus** | <span class="ft ft--no">❌ No</span> | Sin cuota gratuita: se factura por operación | El flujo asíncrono cuesta desde el primer mensaje | Adaptador `azure` escrito y compilado, sin ejecutar |
| **Azure Event Grid** | <span class="ft ft--yes">✅ Sí · permanente</span> | 100 000 operaciones al mes | Unas 3 300 operaciones al día | Previsto para UC-04 y para el bus de auditoría del §10.14; no implementado |
| **Azure Storage Account · Blob** | <span class="ft ft--no">❌ No</span> | Sin cuota en la lista de servicios gratuitos consultada: se paga por GB y por operaciones | Las evidencias cuestan desde el primer archivo | Adaptador `azure` escrito, sin ejecutar |
| **Azure Storage Account · Table** | <span class="ft ft--no">❌ No</span> | Igual que Blob: sin cuota, pago por operación | — | Descartado en el documento 10 |
| **Azure Static Web Apps** (plan Free) | <span class="ft ft--yes">✅ Sí · permanente</span> | 100 GB de banda al mes **por suscripción**, 0,5 GB por app y 2 dominios personalizados | Suficiente para tres portales estáticos; sin SLA | Shell y remoto listos para publicarse |

**Dos costes que la columna «free tier» no revela:** Functions necesita una **cuenta de almacenamiento**
para su estado, que se factura aparte; y el crédito de 200 USD de la cuenta gratuita caduca a los
**30 días**, no a los 12 meses. Los servicios marcados «permanente» no vencen, pero tampoco cubren a sus
dependencias.

La tabla es el resumen; el detalle por caso de uso está en [8 · §8.6](08-casos-de-uso.md) y el inventario
completo de servicios en [10 · §10.3](10-tecnologias-azure.md).

## 9.2 Lectura estructurada

| Dimensión | Exigencia del JD | Implicación para esta prueba |
|-----------|------------------|------------------------------|
| Alcance | Modernización + APIs + Azure + dos portales | El repositorio debe mostrar los cuatro frentes, no solo backend |
| Autonomía | Producir desde la primera semana | Decisiones documentadas y ejecutables sin apoyo del cliente |
| Prioridad | Un solo recurso para tres frentes | Orden explícito: flujo vertical → portales → infraestructura |
| Riesgo | Upgrade sin afectar operación | Estrategia progresiva, rollback y pruebas de regresión |
| Comunicación | Sustentar decisiones ante el cliente | ADR y matriz de compatibilidad, no solo código |

## 9.3 Cobertura actual del JD

Estados: **Cubierto** (existe y es verificable), **Parcial** (hay base, falta completar o ejecutar),
**Pendiente** (planificado y aún no construido), **Fuera del repositorio** (no es un artefacto de
código).

| Requisito del JD | Evidencia en el repositorio | Estado |
|------------------|-----------------------------|--------|
| Upgrade Java 8 / Spring Boot 2.3.x → 17 / 3.x | Estrategia y reglas en `03` (NFR-ARQ-06, NFR-ARQ-07) y fase 7 del plan | Parcial |
| `javax.*` → `jakarta.*`, Spring Security, Hibernate | Decisiones D7 y matriz de compatibilidad en `04` | Parcial |
| OpenRewrite | Receta prevista en el plan de modernización | Pendiente |
| APIs REST con Spring MVC | 60 clases backend, controladores de afiliados y seguridad | Parcial |
| WebFlux / Project Reactor | SSE con `Flux` y `Sinks`, agregación con `Mono`, reintentos con `Retry.backoff` | Parcial |
| Swagger / OpenAPI | `springdoc` declarado en el `pom.xml` y contrato previsto | Pendiente |
| Azure Functions | Fase 5 del plan; sin código aún | Pendiente |
| Azure Service Bus | Adaptadores publicador y consumidor escritos | Parcial |
| Azure Event Grid | Fase 5 del plan | Pendiente |
| Blob Storage | `AzureBlobDocumentStorage` + `LocalFileSystemDocumentStorage` | Parcial |
| Table Storage | Evaluado y descartado para el caso de uso (documentado) | Pendiente |
| MySQL | Modelo JPA completo; migraciones Flyway pendientes | Parcial |
| Microfrontends / Module Federation | Decisión D5 y diseño §4.8; sin código | Pendiente |
| Angular 15+ con RxJS y SASS | Workspace Angular 18.2: shell, remoto de afiliados, perfil (UC-01), beneficiarios (UC-02) y asistente de solicitud (UC-03) | Parcial |
| Web components | `cs-status-badge` diseñado, no implementado | Pendiente |
| Accesibilidad WCAG 2.x | Checklist y criterios en `03` (NFR-UX) | Parcial |
| Azure Static Web Apps | Fase 6 del plan | Pendiente |
| Docker | Fase 6 del plan | Pendiente |
| Azure DevOps Pipelines | Fase 6 del plan | Pendiente |
| JUnit / Mockito | 37 pruebas unitarias en verde (auth, perfil, beneficiarios, solicitudes, radicado, documentos y consultas) | Parcial |
| Jest | 27 pruebas en verde (auth, login, perfil, beneficiarios, asistente, documentos, detalle y contratos) | Parcial |
| Mermaid | Diagramas en `01`, `02`, `04`, `06` y `08` | Cubierto |
| SOLID y patrones | Módulos por funcionalidad, puertos y adaptadores, DTOs | Parcial |
| Documentación de decisiones | Documentos 01–08 y decisiones D1–D10 | Cubierto |
| Git, Postman, VS Code | Colección Postman prevista en fase 6 | Pendiente |
| Java 25 / Spring Boot 4 (deseable) | No usado: se fija Java 17 y Boot 3.5.x por compatibilidad | Fuera del alcance |
| React / Stencil (deseable) | No aplica: la prueba usa Angular | Fuera del alcance |
| PWA / WebSockets (deseable) | Se usa SSE en lugar de WebSocket | Fuera del alcance |
| Certificación AZ-204 / AZ-900 | No es un artefacto de repositorio | Fuera del repositorio |
| Experiencia mínima (5 años, 4 en Java, 3 en Angular) | Se sustenta en la hoja de vida, no en el código | Fuera del repositorio |

## 9.4 Brechas declaradas

1. **La migración no se ejecutó sobre una aplicación real.** Hay estrategia, reglas y compatibilidad documentadas; el laboratorio de código heredado y la receta de OpenRewrite están pendientes.
2. **El backend no compila todavía.** Es un esqueleto avanzado; faltan servicios, migraciones y pruebas (ver documento 07).
3. **No hay frontend.** Los dos portales, el shell y los remotos son diseño, no implementación.
4. **No hay despliegue en Azure.** Existen adaptadores y decisiones, no recursos provisionados ni pipeline ejecutado.
5. **La cobertura se limita a pruebas unitarias.** Hay 37 pruebas de backend y 27 de frontend en verde, pero no hay pruebas de integración con MySQL ni E2E automatizado.
6. **Table Storage** se evaluó y se descartó porque el caso de uso no lo requiere; queda como decisión razonada, no como omisión.

## 9.5 Contexto para asistentes de IA

Bloque compacto para reutilizar al pedir ayuda a un asistente sobre este repositorio:

```text
Proyecto: prueba técnica Bits Americas para Colsubsidio. Prototipo independiente, datos sintéticos.
Estado del repositorio: 2026-10-21. UC-01 a UC-05 operativos de punta a punta.
Objetivo: modernización Java 8/Boot 2.3 -> Java 17/Boot 3.x, APIs REST + Reactor,
          Azure (Functions, Service Bus, Event Grid, Blob) y dos portales Angular con Module Federation.

Restricciones duras:
  - No afirmar integración con sistemas reales de Colsubsidio ni usar PII real.
  - Las reglas de negocio son de demostración; ninguna aprobación es oficial.
  - Ejecutable en local sin suscripción de Azure (adaptadores locales).
  - No reportar pruebas como exitosas sin ejecutarlas.

Fuente de verdad: docs/01..12 y docs/adr/ADR-0009.
  01 alcance · 02 FR · 03 NFR · 04 arquitectura · 05 trazabilidad · 06 plan · 07 inventario
  08 casos de uso · 09 JD y cobertura · 10 Azure · 11 diccionario de datos · 12 despliegue

Backend (implementado): co.com.bits.colsubsidio, monolito modular por funcionalidad,
  MVC + JPA para CRUD, Reactor acotado a SSE, dashboard y salidas HTTP.
  Auth: POST /api/v1/auth/demo-token emite JWT HS256 con claims roles/affiliateId/companyId.
  DemoUserInitializer crea las cuentas desde app.security.demo-users al arrancar (BCrypt).
  UC-01: GET /api/v1/affiliates/me y GET /api/v1/affiliates/me/subsidy-requests.
  UC-02: GET /api/v1/affiliates/me/beneficiaries y /{id}/beneficiaries, ordenados por nombre,
    documento enmascarado en el DTO, pertenencia validada por AccessGuard y auditoría
    PII_ACCESS / PII_ACCESS_DENIED.
  UC-03: POST /api/v1/subsidy-requests (rol afiliado) aplica RD-01…RD-05, genera un radicado
    SOL-<año>-<secuencia>, escribe solicitud, documentos, primer historial y evento outbox en una
    transacción y deja que el consumidor la clasifique en PENDING_DOCUMENTS o IN_REVIEW.
  UC-04: POST y GET /api/v1/subsidy-requests/{id}/documents; valida pertenencia, catálogo, MIME y 5 MB,
    guarda con SHA-256, audita, publica subsidy.document.uploaded y el consumidor mueve la solicitud a
    IN_REVIEW cuando no faltan evidencias.
  UC-05: GET /{id} y /{id}/history para detalle y línea de tiempo; GET /{id}/events (SSE) con replay por
    Last-Event-ID, heartbeat cada 15 s y cierre en estado terminal; el portal consume el stream con fetch
    porque EventSource no envía el token.
  Mensajería: outbox transaccional, idempotencia (message_id, consumer), dead-letter local.
  Adaptadores Azure escritos para Service Bus y Blob; no ejecutados.

Base de datos (implementada): MySQL 8.0.46 local, base colsubsidio_demo.
  Flyway: V1 esquema (13 tablas), V2 índices, V3 datos sintéticos. Baseline marcado en 3.
  app_user se siembra en runtime, no en SQL, para no inventar hashes BCrypt.

Frontend (implementado): workspace Angular 18.2 en frontend/.
  shell (4200) + mfe-affiliates (4201) + shared-core (contratos). Module Federation con webpack.
  Pantallas: login (con cuentas de prueba), perfil (UC-01), beneficiarios (UC-02), asistente de
  solicitud (UC-03), documentos de la solicitud (UC-04) y detalle con historial en vivo (UC-05); la
  navegación vive en el remoto.
  Manifiesto en runtime (assets/mf.manifest.json) y contractVersion validada antes de montar.
  remoto se carga con loadRemoteModule type:'module' porque remoteEntry.js es ESM.
  URL de API en assets/config.json, inyectada por interceptor; token en sessionStorage.
  Estilos tomados de colsubsidio.com: Poppins, #333333, #212529, #F6F7FC, botones píldora 80px/44px.
  Logotipo oficial sin modificar (assets/logo-colsubsidio.svg, tomado de colsubsidio.com);
  el portal se declara demostración en cabecera y pie.

Azure:
  Topología vigente: frontend en 3 Static Web Apps (Free), backend en EC2 t3.small con Docker
    Compose (ADR-0009). App Service con slots queda como alternativa documentada.
  Cobertura por diseño: los trece casos emiten audit.* a Event Grid y pasan por una Function
    (FR-AZR-09). Cinco funciones: auditDispatcher, subsidyClassifier, documentScanner,
    notificationFanout, reportBuilder. Participación asíncrona vía outbox: no bloquea el caso.
  Free tier verificado: Functions 1 M ejecuciones + 400 000 GB-s/mes; Event Grid 100 000
    operaciones/mes; Static Web Apps 100 GB banda/mes por suscripción. Service Bus, Blob y
    Key Vault sin cuota. MySQL gratis 12 meses. Crédito de 200 USD válido 30 días.
  Nada de Event Grid ni de Functions está implementado todavía: es diseño.

Pruebas ejecutadas: 37 backend (JUnit 5 + Mockito) y 27 frontend (Jest). Ambas suites en verde.
  Sin prueba de integración con MySQL ni E2E automatizado.

Entorno local, trampas conocidas:
  - SPRING_DATASOURCE_* globales de otro proyecto sobrescriben application.yml: exportarlas.
  - ng serve del remoto inyecta su cliente de recarga y recarga el shell: servir dist con CORS.
  - webpack duplicado (raíz vs build-angular) rompe el plugin de federación: overrides en package.json.
  - PowerShell 5.1 escribe BOM con Set-Content -Encoding UTF8 y rompe javac y los .java.

Comandos: tools/apply_local_db.ps1 (base), tools/start-demo.ps1 (API + remoto + shell),
  mvn -f backend/pom.xml test, npm test, npm run build:all, tools/build_docs_html.py (docs HTML).

Pendiente: remoto de empresas, implementación de las cinco funciones y del publicador de
  Event Grid, E2E, integración con MySQL real, y despliegue de los tres artefactos estáticos.

Salida esperada: código compilable, pruebas ejecutadas, decisiones justificadas y documentación
en español, sin maquillar lo que falta.
```

## 9.6 Uso en la entrevista

- **Minuto 0–2:** el JD pide tres frentes con un solo recurso; el plan de `06` explica el orden y por qué.
- **Minuto 2–5:** el flujo vertical de `08` (UC-03 → UC-13) demuestra integración, no piezas aisladas.
- **Minuto 5–8:** `04` justifica decisiones incómodas: MVC con Reactor acotado, monolito modular, Module Federation con webpack.
- **Cierre:** `9.3` y `9.4` muestran lo que falta sin maquillarlo; la honestidad sobre el estado es parte de la evaluación.
