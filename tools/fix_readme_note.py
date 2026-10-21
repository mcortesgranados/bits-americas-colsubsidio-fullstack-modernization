"""Repairs the README block damaged by PowerShell backtick escaping."""

from pathlib import Path

README = Path(__file__).resolve().parent.parent / "README.md"
BROKEN = "### Si la aplicación no conecta a la base local"
FIXED = """### Si la aplicación no conecta a la base local

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

"""

text = README.read_text(encoding="utf-8")
start = text.index(BROKEN)
end = text.index("## Requisitos de entorno (objetivo)")
README.write_text(text[:start] + FIXED + text[end:], encoding="utf-8")
print("README reparado:", len(text), "->", len(README.read_text(encoding="utf-8")))
