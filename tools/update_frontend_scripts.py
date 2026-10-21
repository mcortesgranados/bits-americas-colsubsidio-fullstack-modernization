"""Adds the workspace scripts that the build and test documentation relies on."""

import json
from pathlib import Path

PACKAGE = Path(__file__).resolve().parent.parent / "frontend" / "package.json"
data = json.loads(PACKAGE.read_text(encoding="utf-8"))
data["scripts"].update(
    {
        "build:lib": "ng build shared-core",
        "build:shell": "ng build shell --configuration production",
        "build:mfe-affiliates": "ng build mfe-affiliates --configuration production",
        "build:all": "npm run build:lib && npm run build:shell && npm run build:mfe-affiliates",
        "test": "jest",
        "test:watch": "jest --watch",
    }
)
PACKAGE.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print(data["scripts"])
