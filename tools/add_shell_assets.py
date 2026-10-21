"""Adds projects/shell/src/assets to the shell asset list so config.json and the manifest are served."""

import json
from pathlib import Path

MANIFEST = Path(__file__).resolve().parent.parent / "frontend" / "angular.json"
data = json.loads(MANIFEST.read_text(encoding="utf-8"))

assets = data["projects"]["shell"]["architect"]["build"]["options"]["assets"]
entry = {"glob": "**/*", "input": "projects/shell/src/assets", "output": "assets"}

if entry not in assets:
    assets.append(entry)

MANIFEST.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print("assets del shell:", assets)
