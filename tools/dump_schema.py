"""Dumps table and column metadata from the JPA entities as a compact TSV."""

from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "backend" / "src" / "main" / "java" / "co" / "com" / "bits" / "colsubsidio"
PATTERN = re.compile(r'@(Column|JoinColumn|Id|GeneratedValue|Enumerated|Version|Table|Entity|ManyToOne|OneToMany)\b(\([^)]*\))?')


def annotations(text: str) -> list[str]:
    return [m.group(0) for m in PATTERN.finditer(text)]


for path in sorted(ROOT.rglob("domain/*.java")):
    if path.stem.endswith(("Repository", "Specifications", "Actions", "Envelope")):
        continue
    body = path.read_text(encoding="utf-8").replace("\r\n", "\n")
    table = re.search(r'@Table\(\s*name\s*=\s*"([^"]+)"', body)
    if not table:
        continue
    print(f"\n### {path.stem} -> {table.group(1)}")
    for match in re.finditer(
        r"(?P<annos>(?:^\s*@[^\n]*\n)+)\s*private\s+(?P<type>[\w<>, .]+?)\s+(?P<name>\w+)\s*;",
        body,
        re.M,
    ):
        annos = " ".join(
            a.strip()
            for a in match.group("annos").splitlines()
            if a.strip() and not a.strip().startswith(("//", "@Override"))
        )
        annos = re.sub(r"@(Getter|Setter|Builder|NoArgsConstructor|AllArgsConstructor)\b", "", annos)
        annos = re.sub(r"\s+", " ", annos).strip()
        print(f"{match.group('name')}\t{match.group('type').strip()}\t{annos}")
