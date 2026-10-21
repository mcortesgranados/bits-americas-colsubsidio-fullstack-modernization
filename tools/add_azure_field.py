"""Adds an explicit "Servicios Azure" row to every use case in document 08."""

from __future__ import annotations

import pathlib

AZURE = {
    "UC-01": "Ninguno · solo Static Web Apps como hosting",
    "UC-02": "Ninguno",
    "UC-03": "Service Bus · evento `subsidy.request.submitted`",
    "UC-04": "Blob Storage · evidencia; Event Grid previsto",
    "UC-05": "Ninguno · SSE en proceso",
    "UC-06": "Ninguno",
    "UC-07": "Ninguno",
    "UC-08": "Ninguno",
    "UC-09": "Ninguno",
    "UC-10": "Ninguno",
    "UC-11": "Ninguno",
    "UC-12": "Service Bus · evento de decisión (previsto)",
    "UC-13": "Service Bus · consumo; Function de notificación (previsto)",
}

path = pathlib.Path("docs/08-casos-de-uso.md")
lines = path.read_text(encoding="utf-8").split("\n")

current = None
out: list[str] = []
inserted = 0

for line in lines:
    out.append(line)
    if line.startswith("## UC-"):
        current = line[3:8].strip()
        continue
    if current and line.startswith("| Precondición |") and current in AZURE:
        out.append(f"| Servicios Azure | {AZURE[current]} |")
        inserted += 1
        current = None

path.write_text("\n".join(out), encoding="utf-8")
print(f"filas insertadas: {inserted}")
