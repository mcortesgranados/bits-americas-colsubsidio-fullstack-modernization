"""Applies the required/preferred CSS classes to the JD headings."""

from __future__ import annotations

import pathlib

path = pathlib.Path("docs/09-descripcion-del-cargo.md")
text = path.read_text(encoding="utf-8")

replacements = {
    "<h4>Requisitos técnicos obligatorios</h4>": '<h4 class="jd-required">Requisitos técnicos obligatorios</h4>',
    "<h4>Requisitos técnicos deseables</h4>": '<h4 class="jd-preferred">Requisitos técnicos deseables</h4>',
}

for old, new in replacements.items():
    text = text.replace(old, new)

path.write_text(text, encoding="utf-8")
print("clases aplicadas:", sum(text.count(v) for v in replacements.values()))
