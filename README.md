# Bits Americas · Prueba técnica Full Stack — Colsubsidio

Prototipo técnico de modernización de una plataforma de gestión de **afiliados, empresas y
solicitudes de subsidios**, construido con Java 17 / Spring Boot 3.x, Angular con microfrontends y un
camino de despliegue en Azure.

> **Aviso:** este repositorio es una prueba técnica independiente. No reproduce la arquitectura interna
> de Colsubsidio ni se conecta a sus sistemas, datos o APIs. Todos los datos son sintéticos y todas las
> reglas de negocio están marcadas como **reglas de demostración**.

## Estado

| Fase | Alcance | Estado |
|------|---------|--------|
| 1 | Requisitos, alcance y arquitectura | Completada (v1.0) |
| 2 | Backend, modelo de datos y APIs | En progreso: esquema, semilla y **UC-01 y UC-02 de punta a punta** |
| 3 | Seguridad, pruebas y OpenAPI | Parcial: JWT, autorización por token y 14 pruebas de backend |
| 4 | Portales Angular y Module Federation | Parcial: shell, remoto de afiliados, login, perfil y beneficiarios |
| 5 | Mensajería asíncrona y adaptadores Azure | Parcial (esqueleto) |
| 6 | Docker, CI/CD y Postman | Pendiente |
| 7 | ADR, modernización y guion de demo | Pendiente |

El backend compila y arranca contra MySQL. Los casos de uso **UC-01 (consultar el perfil del afiliado)** y
**UC-02 (consultar beneficiarios)** funcionan de punta a punta: login con JWT, perfil enmascarado, listado
de solicitudes y beneficiarios con documento enmascarado, auditoría de la consulta y 403 cuando el
afiliado pide los beneficiarios de otro. Lo que sigue pendiente está en
[07 · Inventario del código preliminar](docs/07-inventario-codigo-preliminar.md). No se declara ninguna
prueba como exitosa sin su ejecución real.

## Documentación

| Documento | Contenido |
|-----------|-----------|
| [docs/README.md](docs/README.md) | Índice y control de cambios |
| [01 · Contexto y alcance](docs/01-contexto-y-alcance.md) | Problema, objetivos, personas, alcance y reglas de demostración |
| [02 · Requisitos funcionales](docs/02-requisitos-funcionales.md) | Requisitos por portal, criterios de aceptación, flujo y estados |
| [03 · Requisitos no funcionales](docs/03-requisitos-no-funcionales.md) | Seguridad, privacidad, rendimiento, accesibilidad, pruebas y límites |
| [04 · Arquitectura baseline](docs/04-arquitectura-baseline.md) | Diagramas, módulos, decisiones, contratos, modelo de datos y estructura |
| [05 · Trazabilidad](docs/05-trazabilidad.md) | Requisito → módulo → endpoint → prueba → pantalla |
| [06 · Plan de entrega](docs/06-plan-entrega.md) | Fases, puertas de calidad, riesgos y convenciones |
| [07 · Inventario del código](docs/07-inventario-codigo-preliminar.md) | Qué existe, qué falta y qué se ajustará |
| [08 · Casos de uso](docs/08-casos-de-uso.md) | UC-01…UC-13 con flujos, postcondiciones y servicios de Azure |
| [09 · Descripción del cargo](docs/09-descripcion-del-cargo.md) | JD original, lectura estructurada y cobertura real |
| [10 · Tecnologías de Azure](docs/10-tecnologias-azure.md) | Inventario de servicios, paridad local y estado |
| [11 · Diccionario de datos y modelo ER](docs/11-diccionario-de-datos.md) | 13 tablas, columnas, índices, estados y datos personales |
| [12 · Plan de despliegue (Azure + AWS)](docs/12-plan-despliegue-azure.md) | Microfrontends en tres Static Web Apps, backend en EC2 y alternativa App Service |
| [ADR-0009](docs/adr/ADR-0009-despliegue-backend-ec2.md) | Decisión de desplegar el backend en EC2 y sus consecuencias |

## Estructura prevista

```text
.
├── docs/            # Requisitos, arquitectura, ADR, modernización, demo
├── backend/         # Spring Boot 3 · Java 17 · MySQL · Flyway
├── frontend/        # Angular 18 · shell + 2 remotos (Module Federation) + libs
├── azure/           # Functions y Bicep
├── infra/           # Dockerfiles y docker-compose
├── pipelines/       # Azure DevOps
├── postman/         # Colección y entorno
└── legacy-lab/      # Laboratorio de migración Java 8 / Boot 2.3
```

### Si la aplicación no conecta a la base local

Spring Boot da prioridad a las variables de entorno sobre `application.yml`. Si tu máquina tiene
`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` o `SPRING_DATASOURCE_PASSWORD` definidas —por
ejemplo, por otro proyecto— la aplicación intentará conectarse a esa otra base, y el error será un
`Access denied` o un `Communications link failure` que no menciona el archivo de configuración. Para
esta sesión:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:mysql://127.0.0.1:3306/colsubsidio_demo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:SPRING_DATASOURCE_USERNAME = "colsubsidio"
$env:SPRING_DATASOURCE_PASSWORD = "colsubsidio"
```

## Identidad visual

Los estilos del portal se tomaron de los **valores computados del sitio real** (`colsubsidio.com`, inspeccionado el 20/10/2026 con `getComputedStyle`), no de una aproximación:

| Token | Valor | Origen |
|-------|-------|--------|
| Tipografía | Poppins 400/500/600/700 | `font-family` del `body` |
| Texto base | `#212529` | `color` del `body` |
| Títulos y barra | `#333333` | `color` de `h1`/`h2` y fondo del menú |
| Fondo de contenido | `#F6F7FC` | `background-color` del `main` |
| Botones | píldora `80px`, alto `44px`, fondo `#333333` | `.ButtonLink_btnBasic` |

El azul `#0067B1` se usa solo como acento de foco: aparece como color de marca en la ficha pública, pero no en la portada inspeccionada.

**Logotipo:** se usa el logo oficial de Colsubsidio sin modificarlo (`assets/logo-colsubsidio.svg`,
181 × 36, descargado de `colsubsidio.com` el 2026-10-21). No se imita la marca: el prototipo se identifica
como demostración en la cabecera y en el pie, y todos los datos son sintéticos.
## Requisitos de entorno (objetivo)

| Herramienta | Versión |
|-------------|---------|
| JDK | 17 (compilación con `--release 17`) |
| Maven | 3.9+ |
| Node.js | 20 o 22 LTS |
| MySQL | 8.0 |
| Docker | Opcional: MySQL nativo sirve para local; Docker es necesario para el despliegue |

## Ejecución local

```powershell
# 1. Base de datos: aplica V1 (esquema), V2 (índices) y V3 (datos sintéticos)
#    Pide la contraseña del root de MySQL y crea la base y el usuario de la aplicación.
powershell -ExecutionPolicy Bypass -File tools\apply_local_db.ps1

# 2. Backend
cd backend
./mvnw spring-boot:run
#    Con Maven instalado fuera del wrapper:
#    mvn spring-boot:run

# 3. Frontend
cd frontend
npm install
npm run build:lib        # la librería de contratos se compila antes que las aplicaciones
npm run start:shell      # http://localhost:4200
npm run start:affiliates # http://localhost:4201
```

El shell descubre el remoto en `projects/shell/src/assets/mf.manifest.json` y la URL de la API en
`projects/shell/src/assets/config.json`. Para una demostración con artefactos compilados, sirve
`dist/mfe-affiliates` como estático con CORS (`tools/serve_static_cors.py`) en lugar de `ng serve`: el
servidor de desarrollo inyecta su cliente de recarga dentro del remoto y ese cliente acaba recargando el
shell.

Pruebas del frontend: `npm test` (Jest, 4 suites).

### Arranque en un solo paso

```powershell
powershell -ExecutionPolicy Bypass -File tools\start-demo.ps1
```

Levanta la API, el remoto compilado y el shell, fuerza las variables locales de la base y espera a que
`/actuator/health` responda antes de devolver el control. Es la forma recomendada de abrir la demo: evita
tener que exportar variables a mano y deja los tres servicios en segundo plano con sus logs en
`backend\target\demo-api.log`.
Credenciales de demostración previstas: `afiliado.demo`, `empresa.demo`, `backoffice.demo`,
`auditor.demo`, todas con contraseña `Demo123!` y sobreescribibles por variables de entorno.

## Uso

Repositorio de evaluación técnica. El contenido es demostrativo y no debe desplegarse en producción sin
sustituir la autenticación de demostración, las reglas de negocio y los controles de seguridad
documentados como pendientes.
