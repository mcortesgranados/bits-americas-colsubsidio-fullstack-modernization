# ADR-0009 · Despliegue del backend en AWS EC2

- **Estado:** aceptada
- **Fecha:** 2026-10-20
- **Decisores:** autor del prototipo
- **Revisión:** al cerrar la Fase 6 o si la entrega exige que todo el sistema viva en Azure

## Contexto

El prototipo debe poder evaluarse de punta a punta, y el requisito de «corre en local sin Azure» ya
obligaba a tener adaptadores locales para mensajería, almacenamiento y notificaciones. Levantar la
alternativa Azure completa (App Service con slots, MySQL Flexible, Service Bus, Blob, Key Vault) tiene un
coste mensual que no se justifica para una demostración de 2,5 meses, y el crédito de la cuenta gratuita
de Azure caduca a los 30 días. Una VM B1s gratuita (1 GiB) no sostiene API + MySQL + nginx a la vez.

## Decisión

El backend se publica en **una instancia AWS EC2 `t3.small`** (2 vCPU, 2 GiB) con tres contenedores
—API, MySQL 8 y nginx con TLS— sobre un volumen EBS. El backend corre con el **perfil local**
(`app.messaging.transport=local`, `app.storage.provider=local`, `app.notifications.provider=local-log`).

El frontend permanece en **Azure Static Web Apps** (tres apps del plan Free) y consume la API por HTTPS
con JWT en cabecera.

## Alternativas consideradas

| Alternativa | Por qué se descarta |
|-------------|---------------------|
| App Service Standard S1 + MySQL Flexible | Funcionalmente superior (slots, identidad administrada, TLS gestionado), pero es coste recurrente y el crédito dura 30 días |
| Azure VM B1s gratuita | 1 GiB: cabe la API sola o MySQL solo, no el sistema; sin slots ni SLA |
| Azure Container Apps | Descartado en el documento 10; añade registro de contenedores y no resuelve el coste |
| Todo en AWS (S3 + CloudFront para el frontend) | Dejaría la entrega sin ninguna pieza de Azure visible |

## Consecuencias

**Positivas**

- Coste cubierto con los créditos de AWS para cuentas nuevas (hasta 200 USD, 6 meses).
- El mismo `docker-compose.yml` sirve para local y para producción: menos deriva entre entornos.
- Reversible: volver a Azure es cambiar variables de entorno y desplegar el mismo `.jar`.

**Negativas, aceptadas**

- **Los requisitos `FR-AZR-01` a `FR-AZR-08` dejan de tener evidencia ejecutable.** Service Bus, Blob,
  Event Grid, la Function y Key Vault no participan, y el perfil solicitado incluye integración con Azure.
- Sin slots: cada despliegue corta el servicio entre 20 y 40 segundos.
- Parcheo del sistema operativo, TLS y copias de seguridad quedan a cargo del equipo.
- Sin point-in-time recovery: la recuperación depende de snapshots y volcados.
- La instancia tiende a convertirse en mascota si no se versiona su aprovisionamiento.

## Seguimiento

| Acción | Responsable | Cuándo |
|--------|-------------|--------|
| Versionar `docker-compose.yml` de producción y el `user-data` | Autor | Fase 6 |
| Automatizar snapshot diario y probar una restauración | Autor | Fase 6 |
| Revisar la decisión si la entrega exige Azure de extremo a extremo | Autor | Antes del cierre |
| Si se revierte: desplegar App Service y volver a los adaptadores `azure` | Autor | Según revisión |

Referencias: [12 · Plan de despliegue](../12-plan-despliegue-azure.md#1216-topologia-vigente-backend-en-aws-ec2),
[10 · Tecnologías de Azure](../10-tecnologias-azure.md),
[04 · Arquitectura baseline](../04-arquitectura-baseline.md#431-despliegue-vigente-frontend-en-azure-y-backend-en-aws-ec2).
