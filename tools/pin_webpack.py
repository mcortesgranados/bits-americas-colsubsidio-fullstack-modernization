"""Pins a single webpack version so Module Federation and Angular share the same compiler."""

import json
from pathlib import Path

PACKAGE = Path(__file__).resolve().parent.parent / "frontend" / "package.json"
data = json.loads(PACKAGE.read_text(encoding="utf-8"))
data.setdefault("overrides", {})["webpack"] = "5.94.0"
PACKAGE.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print("overrides:", data["overrides"])
