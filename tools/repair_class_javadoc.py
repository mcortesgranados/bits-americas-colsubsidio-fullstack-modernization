#!/usr/bin/env python3
"""Repairs type-level Javadoc blocks duplicated by an early non-idempotent run.

The repair keeps the authentic description, removes the repeated generated paragraphs and leaves a
single @author / @since pair at the end of the block.
"""

from __future__ import annotations

import pathlib
import re
import textwrap

BLOCK_RE = re.compile(r"(?m)^/\*\*(?P<body>.*?)^ \*/", re.S)


def normalise(lines: list[str]) -> str:
    cleaned = []
    for line in lines:
        stripped = line.strip()
        if stripped.startswith("*"):
            stripped = stripped[1:]
        cleaned.append(stripped.strip())
    return re.sub(r"\s+", " ", " ".join(cleaned)).strip()


def main() -> int:
    root = pathlib.Path("backend/src/main/java")
    repaired = 0
    for path in sorted(root.rglob("*.java")):
        text = path.read_text(encoding="utf-8")
        match = BLOCK_RE.search(text)
        if not match:
            continue
        body = match.group("body")
        lines = body.split("\n")
        text_lines: list[str] = []
        author = since = None
        for line in lines:
            stripped = line.strip().lstrip("*").strip()
            if stripped.startswith("@author"):
                author = stripped
            elif stripped.startswith("@since"):
                since = stripped
            elif stripped.startswith("@"):
                continue
            else:
                text_lines.append(stripped)
        prose = normalise(text_lines)
        if not prose:
            continue

        class_match = re.search(r"\b(class|interface|enum|record)\s+(\w+)", text[match.end():])
        cls = class_match.group(2) if class_match else ""
        marker = f"Represents the {cls} component of the"
        first = prose.find(marker)
        if first >= 0:
            second = prose.find(marker, first + 1)
            if second > 0:
                prose = prose[:second].strip()

        body_lines = textwrap.wrap(prose, 100) if prose else []
        block = ["/**"]
        block.extend(f" * {line}".rstrip() for line in body_lines)
        block.append(" *")
        block.append(f" * {author or '@author Manuela Cortés Granados (manuelacortesgranados@gmail)'}")
        block.append(f" * {since or '@since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia'}")
        block.append(" */")
        replacement = "\n".join(block)
        text = text[: match.start()] + replacement + text[match.end():]
        path.write_text(text, encoding="utf-8")
        repaired += 1
    print(f"bloques de clase reparados: {repaired}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
