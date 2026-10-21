"""Registers Azure Database for MySQL Flexible Server in the Azure inventory (doc 10)."""

from __future__ import annotations

from pathlib import Path

DOC = Path(__file__).resolve().parent.parent / "docs" / "10-tecnologias-azure.md"
INDEX = Path(__file__).resolve().parent.parent / "docs" / "index.html"

FREE_TIER = {
    "**Service Bus**": "No: sin cuota gratuita",
    "**Azure Functions**": "Sí, permanente: 1 M de ejecuciones y 400 000 GB-s al mes",
    "**Event Grid**": "Sí, permanente: 100 000 operaciones al mes",
    "**Blob Storage**": "No: sin cuota gratuita",
    "**Table Storage**": "No aplica: fuera de alcance",
    "**Static Web Apps**": "Sí, plan Free: 100 GB de banda al mes",
    "**Key Vault**": "No: se factura por operaciones",
    "**Azure Database for MySQL Flexible Server**": "Sí, pero solo 12 meses: 750 h B1MS y 32 GB en cuentas nuevas",
    "**Application Insights**": "Sí, parcial: 5 GB de ingesta al mes",
}

MYSQL_ROW = (
    "| **Azure Database for MySQL Flexible Server** | Persistencia transaccional del dominio y del "
    "historial; Flyway versiona el esquema | JDBC con Hikari (`mysql-connector-j`) y migraciones con "
    "`flyway-mysql` | Esquema versionado en `db/migration`; TLS obligatorio (`sslMode=REQUIRED`) fuera "
    "de local | Contenedor `mysql:8.4` en `docker-compose` | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | "
    "Pendiente: solo contenedor local, sin servidor real | "
    "Sí, pero solo 12 meses: 750 h B1MS y 32 GB en cuentas nuevas |"
)

PURPOSE_ROW = (
    "| Persistir el estado transaccional | Azure Database for MySQL | Es el motor relacional del "
    "dominio; en local corre como contenedor y en Azure como servidor flexible |"
)


def main() -> int:
    lines = DOC.read_text(encoding="utf-8").replace("\r\n", "\n").split("\n")

    # 10.1 · purpose table
    if not any("Azure Database for MySQL" in line for line in lines if line.startswith("|")):
        for index, line in enumerate(lines):
            if line.startswith("| Guardar evidencias fuera de la base"):
                lines.insert(index + 1, PURPOSE_ROW)
                break

    # 10.3 · intervention matrix with a free-tier column
    start = next(i for i, line in enumerate(lines) if line.startswith("| Servicio | Rol en la prueba"))
    end = start
    while end < len(lines) and lines[end].startswith("|"):
        end += 1

    header = lines[start].rstrip()
    if "Free tier" not in header:
        header = header + " Free tier |"
    separator = "|" + "---|" * (header.count("|") - 1)

    rows = []
    for line in lines[start + 2 : end]:
        name = line.split("|")[1].strip()
        value = FREE_TIER.get(name, "No verificado")
        rows.append(line.rstrip() + f" {value} |")
        if name == "**Key Vault**":
            rows.append(MYSQL_ROW)

    lines[start:end] = [header, separator] + rows
    DOC.write_text("\n".join(lines), encoding="utf-8")

    html = INDEX.read_text(encoding="utf-8")
    INDEX.write_text(html.replace(">8 servicios<", ">9 servicios<"), encoding="utf-8")

    print(f"matrix rows: {len(rows)}")
    print("index tag updated:", "9 servicios" in INDEX.read_text(encoding="utf-8"))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
