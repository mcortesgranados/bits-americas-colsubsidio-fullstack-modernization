#!/usr/bin/env python3
"""Removes the blank comment line that separates @author from @since.

The blank line is harmless for javadoc but makes the blocks harder to read. The fix is line based so it
never spans across comment boundaries.
"""

from __future__ import annotations

import pathlib

AUTHOR = "@author"


def main() -> int:
    root = pathlib.Path("backend/src/main/java")
    changed = 0
    for path in sorted(root.rglob("*.java")):
        lines = path.read_text(encoding="utf-8").split("\n")
        result: list[str] = []
        index = 0
        touched = False
        while index < len(lines):
            line = lines[index]
            if AUTHOR in line and index + 2 < len(lines):
                blank = lines[index + 1].strip()
                following = lines[index + 2]
                if blank == "*" and "@since" in following:
                    result.append(line)
                    result.append(following)
                    index += 3
                    touched = True
                    continue
            result.append(line)
            index += 1
        if touched:
            path.write_text("\n".join(result), encoding="utf-8")
            changed += 1
    print(f"archivos con etiquetas normalizadas: {changed}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
