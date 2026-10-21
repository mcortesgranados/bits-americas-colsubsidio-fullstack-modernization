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
| 2 | Backend, modelo de datos y APIs | En progreso (esqueleto inicial) |
| 3 | Seguridad, pruebas y OpenAPI | Pendiente |
| 4 | Portales Angular y Module Federation | Pendiente |
| 5 | Mensajería asíncrona y adaptadores Azure | Parcial (esqueleto) |
| 6 | Docker, CI/CD y Postman | Pendiente |
| 7 | ADR, modernización y guion de demo | Pendiente |

El backend **todavía no compila**: faltan servicios y controladores listados en
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

## Requisitos de entorno (objetivo)

| Herramienta | Versión |
|-------------|---------|
| JDK | 17 (compilación con `--release 17`) |
| Maven | 3.9+ |
| Node.js | 20 o 22 LTS |
| MySQL | 8.0 |
| Docker | Opcional (facilita MySQL local) |

## Ejecución local (prevista al cerrar la Fase 2)

```bash
# 1. Base de datos
docker compose -f infra/docker-compose.yml up -d mysql

# 2. Backend
cd backend
./mvnw spring-boot:run

# 3. Frontend (tres procesos)
cd frontend
npm install
npm run start:shell      # http://localhost:4200
npm run start:affiliates # http://localhost:4201
npm run start:companies  # http://localhost:4202
```

Credenciales de demostración previstas: `afiliado.demo`, `empresa.demo`, `backoffice.demo`,
`auditor.demo`, todas con contraseña `Demo123!` y sobreescribibles por variables de entorno.

## Uso

Repositorio de evaluación técnica. El contenido es demostrativo y no debe desplegarse en producción sin
sustituir la autenticación de demostración, las reglas de negocio y los controles de seguridad
documentados como pendientes.
