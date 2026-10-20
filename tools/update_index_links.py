"""Rewrites the documentation index links from Markdown to the generated HTML pages."""

from __future__ import annotations

import pathlib
import re

index = pathlib.Path("docs/index.html")
text = index.read_text(encoding="utf-8")
updated, count = re.subn(r'href="(0[1-7]-[a-z\-]+)\.md"', r'href="\1.html"', text)
index.write_text(updated, encoding="utf-8")

remaining = len(re.findall(r'href="0[1-7]-[a-z\-]+\.md"', updated))
print(f"links rewritten: {count}; markdown links left: {remaining}")
