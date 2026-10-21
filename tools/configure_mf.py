"""Switches the two Angular applications to the webpack builder required by Module Federation."""

from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent / "frontend"
MANIFEST = ROOT / "angular.json"
PORTS = {"shell": 4200, "mfe-affiliates": 4201}

data = json.loads(MANIFEST.read_text(encoding="utf-8"))

for project, port in PORTS.items():
    architect = data["projects"][project]["architect"]

    build = architect["build"]
    options = build["options"]
    options["main"] = options.pop("browser")
    options.pop("polyfills", None)
    options["customWebpackConfig"] = {"path": f"projects/{project}/webpack.config.js"}
    options["polyfills"] = ["zone.js"]
    build["builder"] = "@angular-builders/custom-webpack:browser"

    architect["serve"] = {
        "builder": "@angular-builders/custom-webpack:dev-server",
        "options": {
            "buildTarget": f"{project}:build",
            "port": port,
            "host": "localhost",
        },
        "configurations": {
            "production": {"buildTarget": f"{project}:build:production"},
            "development": {"buildTarget": f"{project}:build:development"},
        },
        "defaultConfiguration": "development",
    }

MANIFEST.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
print("angular.json actualizado para:", ", ".join(PORTS))
