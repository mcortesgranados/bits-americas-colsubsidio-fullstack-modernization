"""Raises the component style budget so the responsive tables fit without warnings."""

import json
from pathlib import Path

MANIFEST = Path(__file__).resolve().parent.parent / "frontend" / "angular.json"
data = json.loads(MANIFEST.read_text(encoding="utf-8"))

for project in ("shell", "mfe-affiliates"):
    budgets = data["projects"][project]["architect"]["build"]["configurations"]["production"]["budgets"]
    for budget in budgets:
        if budget["type"] == "anyComponentStyle":
            budget["maximumWarning"] = "3kB"
            budget["maximumError"] = "5kB"

MANIFEST.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print("presupuestos ajustados")
