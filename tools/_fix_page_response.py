import pathlib
import re

target = pathlib.Path(
    "backend/src/main/java/co/com/bits/colsubsidio/common/api/PageResponse.java"
)
text = target.read_text(encoding="utf-8")
text, count = re.subn(r"/\*\*.*?\*/\n(?=\s*public static <T>)", "", text, flags=re.S)
target.write_text(text, encoding="utf-8")
print(f"bloques eliminados: {count}")

enricher = pathlib.Path(__file__).with_name("javadoc_enricher.py")
source = enricher.read_text(encoding="utf-8")
old = '''    if kind.startswith("List"):'''
new = '''    if kind.startswith("Page"):
        return "page of results supplied by the repository, including the pagination metadata required by the client"
    if kind.startswith("List"):'''
if old not in source:
    raise SystemExit("param anchor not found")
source = source.replace(old, new, 1)
enricher.write_text(source, encoding="utf-8")
print("descripcion de Page agregada")
