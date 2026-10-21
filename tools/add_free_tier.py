"""Adds free-tier status and capacity to the Azure row of every use case."""

from __future__ import annotations

import pathlib
import re

ROWS = {
    "UC-01": "Ninguno · solo Static Web Apps como hosting — free tier sí: 100 GB de banda al mes por suscripción",
    "UC-02": "Ninguno",
    "UC-03": "Service Bus · `subsidy.request.submitted` — sin free tier: se paga por operaciones; cubierto por el crédito de 200 USD",
    "UC-04": "Blob Storage · evidencia — sin free tier: pago por GB y operaciones. Event Grid previsto — free tier sí: 100 000 operaciones al mes",
    "UC-05": "Ninguno · SSE en proceso",
    "UC-06": "Ninguno",
    "UC-07": "Ninguno",
    "UC-08": "Ninguno",
    "UC-09": "Ninguno",
    "UC-10": "Ninguno",
    "UC-11": "Ninguno",
    "UC-12": "Service Bus · evento de decisión (previsto) — sin free tier: se paga por operaciones",
    "UC-13": "Service Bus · consumo — sin free tier. Function de notificación (prevista) — free tier sí: 1 000 000 de ejecuciones y 400 000 GB-s al mes",
}

path = pathlib.Path("docs/08-casos-de-uso.md")
lines = path.read_text(encoding="utf-8").split("\n")

current = None
updated = 0
for index, line in enumerate(lines):
    if line.startswith("## UC-"):
        current = line[3:8].strip()
        continue
    if line.startswith("| Servicios Azure |") and current in ROWS:
        lines[index] = f"| Servicios Azure | {ROWS[current]} |"
        updated += 1

text = "\n".join(lines)

section = """## 8.5 Free tier de los servicios usados

Consulta del **2026-10-20** sobre las páginas públicas de precios y la lista de servicios gratuitos de
Azure. Las cuotas cambian con el tiempo: hay que reconfirmarlas antes de desplegar.

| Servicio | ¿Entra en free tier? | Capacidad incluida | Al superarla |
|----------|----------------------|--------------------|--------------|
| Azure Functions (Consumption) | Sí, permanente | 1 000 000 de ejecuciones y 400 000 GB-s al mes por suscripción | 0,20 USD por millón de ejecuciones y 0,000016 USD por GB-s |
| Azure Functions (Flex Consumption) | Sí, permanente | 250 000 ejecuciones y 100 000 GB-s al mes | Se factura por ejecución y por GB-s |
| Service Bus | **No** | Sin cuota gratuita; Basic se factura por operaciones y Standard por hora base | Crédito de 200 USD o pago por uso |
| Event Grid | Sí, permanente | 100 000 operaciones al mes | 0,60 USD por millón de operaciones |
| Blob Storage | **No** | No figura en la lista de servicios gratuitos; se paga por GB almacenado y por operaciones | Pago por uso |
| Static Web Apps (plan Free) | Sí, permanente | 100 GB de banda al mes por suscripción, 0,5 GB de almacenamiento por app y 2 dominios | El sitio deja de servirse hasta el siguiente ciclo; sin SLA |
| Azure Database for MySQL Flexible | Solo 12 meses, cuentas nuevas | 750 h de Burstable B1MS, 32 GB de datos y 32 GB de backup | Pago por uso |
| Key Vault | **No** | Se factura por operaciones | Pago por uso |
| Application Insights | Sí, parcial | 5 GB al mes por cuenta de facturación | 2,30 USD por GB |

**Conclusión:** el flujo de la prueba cabe en free tier salvo **Service Bus**, **Blob Storage**,
**Key Vault** y MySQL tras los primeros 12 meses. Para una demo de 2,5 meses el crédito de 200 USD de
la cuenta gratuita cubre esos tres servicios con holgura, y el consumo esperado se mide en centavos por
operaciones.

**Aviso sobre el alojamiento del backend:** App Service no está en la lista anterior porque no participa
en ningún caso de uso, pero publicar la API sí cuesta. Su cuota gratuita permanente (hasta 10 apps con
1 GB y 1 hora al día) no alcanza para un servicio disponible 24/7, así que un despliegue real requiere
plan de pago o Container Apps.

## 8.6 Cobertura"""

text = text.replace("## 8.5 Cobertura", section, 1)
path.write_text(text, encoding="utf-8")
print(f"filas actualizadas: {updated}")
