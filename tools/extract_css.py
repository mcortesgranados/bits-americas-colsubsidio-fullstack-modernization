"""Extracts the inline stylesheet of docs/index.html into docs/assets/docs.css."""

from __future__ import annotations

import pathlib
import re

index = pathlib.Path("docs/index.html")
text = index.read_text(encoding="utf-8")
match = re.search(r"<style>(.*?)</style>", text, re.S)
if not match:
    raise SystemExit("no inline style block found")

css = match.group(1).strip()
target = pathlib.Path("docs/assets/docs.css")
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(css + "\n", encoding="utf-8")

link = '<link rel="stylesheet" href="assets/docs.css">'
updated = text[: match.start()] + link + text[match.end():]
index.write_text(updated, encoding="utf-8")

print(f"css bytes: {target.stat().st_size}")
print(f"index bytes: {len(updated)}")
