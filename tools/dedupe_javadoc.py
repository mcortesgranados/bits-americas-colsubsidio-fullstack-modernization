#!/usr/bin/env python3
"""Removes duplicated paragraphs and duplicated tags inside Javadoc blocks.

The first pass of the enricher was not idempotent for type-level documentation, so a second run
appended the generated paragraphs again. This utility rebuilds every block keeping the first
occurrence of each paragraph and of each tag line, preserving the original order.
"""

from __future__ import annotations

import pathlib
import re

BLOCK_RE = re.compile(r"(?P<indent>[ \t]*)/\*\*(?P<body>.*?)\*/", re.S)


def clean_block(match: re.Match[str]) -> str:
    indent = match.group("indent")
    raw_lines = match.group("body").split("\n")
    content: list[str] = []
    for line in raw_lines:
        stripped = line.strip()
        if stripped.startswith("*"):
            stripped = stripped[1:]
        content.append(stripped.strip())

    while content and not content[0]:
        content.pop(0)
    while content and not content[-1]:
        content.pop()

    paragraphs: list[list[str]] = []
    for line in content:
        if not line:
            paragraphs.append([])
        elif paragraphs and paragraphs[-1] and not line.startswith("@"):
            paragraphs[-1].append(line)
        elif line.startswith("@"):
            paragraphs.append([line])
        else:
            paragraphs.append([line])

    seen: set[str] = set()
    kept: list[list[str]] = []
    for paragraph in paragraphs:
        if not paragraph:
            continue
        key = " ".join(paragraph)
        if key in seen:
            continue
        seen.add(key)
        kept.append(paragraph)

    rebuilt = [f"{indent}/**"]
    for paragraph in kept:
        for line in paragraph:
            rebuilt.append(f"{indent} * {line}")
        rebuilt.append(f"{indent} *")
    if rebuilt[-1].strip() == "*":
        rebuilt.pop()
    rebuilt.append(f"{indent} */")
    return "\n".join(rebuilt)


def main() -> int:
    root = pathlib.Path("backend/src/main/java")
    changed = 0
    for path in sorted(root.rglob("*.java")):
        text = path.read_text(encoding="utf-8")
        updated = BLOCK_RE.sub(clean_block, text)
        if updated != text:
            path.write_text(updated, encoding="utf-8")
            changed += 1
    print(f"archivos deduplicados: {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
