#!/usr/bin/env python3
"""Builds one standalone HTML page per Markdown document in docs/.

Each page shares docs/assets/docs.css and the sticky candidate header, and includes a link back to
the documentation index. Mermaid diagrams are emitted as <pre class="mermaid"> so that the browser can
render them when the CDN script is available, while the source remains readable without JavaScript.
"""

from __future__ import annotations

import html as html_lib
import pathlib
import re

import markdown

DOCS = pathlib.Path("docs")
CSS = "assets/docs.css"

HEADER = """<header class="candidate-sticky-header" id="candidate-sticky-header" aria-label="Información básica de Manuela Cortés Granados">
  <img class="candidate-photo" src="assets/manuela-cortes-granados.jpg" alt="Fotografía de perfil de Manuela Cortés Granados" width="200" height="200" loading="eager" decoding="async">
  <div class="candidate-identity">
    <strong>Manuela Cortés Granados</strong>
    <span>Senior Software Engineer Candidate</span>
  </div>
  <div class="candidate-basic-data">
    <span>📍 Bogotá, Colombia</span>
    <a href="mailto:manuelacortesgranados@gmail.com">✉️ manuelacortesgranados@gmail.com</a>
    <span>🕒 Immediate / negotiable</span>
    <a href="https://github.com/mcortesgranados" target="_blank" rel="noopener">💻 GitHub</a>
    <a href="index.html">📚 Documentación</a>
    <a href="04-arquitectura-baseline.html">🧭 Arquitectura</a>
  </div>
</header>"""

MERMAID = """<script src="https://cdn.jsdelivr.net/npm/mermaid@10.9.1/dist/mermaid.min.js"></script>
<script>
  if (window.mermaid) {
    mermaid.initialize({ startOnLoad: true, theme: 'default', securityLevel: 'loose' });
  }
</script>"""

TEMPLATE = """<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{title}</title>
<link rel="stylesheet" href="{css}">
</head>
<body>
{header}
<main class="container doc-content">
  <nav class="doc-nav" aria-label="Navegación de documentación">
    <a href="index.html">← Índice de documentación</a>
  </nav>
  {quick}
{body}
  <div class="footer">
    Documentación generada para la prueba técnica · <span class="tag">v1.0</span>
  </div>
</main>
{mermaid}
</body>
</html>
"""


def build_quick_access(body: str) -> str:
    """Builds the sticky quick-access bar from the level-2 headings of the page."""
    headings = re.findall(r'<h2 id="([^"]+)"[^>]*>(.*?)</h2>', body, re.S)
    if len(headings) < 2:
        return ""

    items = []
    for anchor, raw in headings:
        text = html_lib.unescape(re.sub(r"<[^>]+>", "", raw)).strip()
        if len(text) > 44:
            text = text[:41].rstrip() + "…"
        items.append(f'<li><a href="#{anchor}">{html_lib.escape(text)}</a></li>')

    return (
        '<nav class="quick-access" aria-label="Acceso rápido a secciones">'
        '<span class="quick-access-label">Acceso rápido</span>'
        f'<ul class="quick-access-list">{"".join(items)}</ul>'
        "</nav>"
    )


def convert(markdown_text: str) -> tuple[str, str]:
    """Returns the converted HTML body and the document title."""
    html = markdown.markdown(
        markdown_text,
        extensions=["extra", "tables", "fenced_code", "sane_lists", "def_list", "toc"],
        output_format="html5",
    )
    html = re.sub(
        r'<pre><code class="language-mermaid">(.*?)</code></pre>',
        r'<pre class="mermaid">\1</pre>',
        html,
        flags=re.S,
    )
    # Cross-document links must point to the generated pages, not to the Markdown sources.
    html = re.sub(
        r'href="([0-9]{2}-[a-z0-9\-]+)\.md(#[^"]*)?"',
        r'href="\1.html\2"',
        html,
    )
    match = re.search(r"<h1[^>]*>(.*?)</h1>", html, re.S)
    title = re.sub(r"<[^>]+>", "", match.group(1)).strip() if match else "Documentación"
    return html, title


def main() -> int:
    built = 0
    for source in sorted(DOCS.glob("[0-9][0-9]-*.md")):
        html, title = convert(source.read_text(encoding="utf-8"))
        page = TEMPLATE.format(
            title=title,
            css=CSS,
            header=HEADER,
            quick=build_quick_access(html),
            body=html,
            mermaid=MERMAID,
        )
        target = source.with_suffix(".html")
        target.write_text(page, encoding="utf-8")
        built += 1
        print(f"built {target.name} ({len(page)} bytes)")
    print(f"pages: {built}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
