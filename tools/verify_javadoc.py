#!/usr/bin/env python3
"""Verifies the Javadoc standard across the backend sources.

Checks performed:
* every documented member has @author and @since;
* every member has at least 500 characters of prose;
* no OpenAI authorship remains;
* files decode as UTF-8 without replacement characters.
"""

from __future__ import annotations

import pathlib
import re
import sys

sys.path.insert(0, str(pathlib.Path(__file__).parent))
import javadoc_enricher as enricher  # noqa: E402

MIN = 500


def main() -> int:
    root = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else "backend/src/main/java")
    problems: list[str] = []
    members = 0
    files = 0

    for path in sorted(root.rglob("*.java")):
        files += 1
        raw = path.read_bytes()
        try:
            text = raw.decode("utf-8").replace("\r\n", "\n")
        except UnicodeDecodeError as exc:
            problems.append(f"{path.name}: invalid UTF-8 ({exc})")
            continue
        if "\ufffd" in text:
            problems.append(f"{path.name}: replacement character found")
        if "openai" in text.lower():
            problems.append(f"{path.name}: OpenAI attribution still present")

        class_match = enricher.CLASS_RE.search(text)
        if class_match:
            before = text[: class_match.start("head")]
            tail = enricher.JAVADOC_TAIL_RE.search(before)
            block = tail.group("body") if tail else None
            if not block or "@author" not in block or "@since" not in block:
                problems.append(f"{path.name}: class Javadoc missing author/since")

        is_enum = bool(class_match and class_match.group("kind") == "enum")
        for match in enricher.MEMBER_RE.finditer(text):
            name = match.group("name")
            ret = match.group("ret") or ""
            if name in enricher.RESERVED:
                continue
            declaration = re.sub(enricher.ANNOTATION_RE, " ", match.group("head") + ret)
            if "=" in declaration or ret.strip().endswith("."):
                continue
            if is_enum and name.isupper():
                continue
            members += 1
            before = text[: match.start()]
            tail = enricher.JAVADOC_TAIL_RE.search(before)
            block = tail.group("body") if tail else None
            if not block:
                problems.append(f"{path.name}:{text[:match.start()].count(chr(10)) + 1}: no Javadoc for {name}")
                continue
            prose = enricher.extract_description(block)
            if len(prose) < MIN:
                problems.append(
                    f"{path.name}:{text[:match.start()].count(chr(10)) + 1}: {name} prose {len(prose)} < {MIN}"
                )
            if "@author" not in block or "@since" not in block:
                problems.append(
                    f"{path.name}:{text[:match.start()].count(chr(10)) + 1}: {name} missing author/since"
                )

    print(f"files: {files}; members: {members}; problems: {len(problems)}")
    for problem in problems[:40]:
        print(" -", problem)
    return 1 if problems else 0


if __name__ == "__main__":
    raise SystemExit(main())
