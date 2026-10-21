"""Serves a build directory with the CORS headers that a cross-origin module remote needs."""

from __future__ import annotations

import functools
import sys
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer


class CorsHandler(SimpleHTTPRequestHandler):
    """Static handler that allows the shell to import the remote entry from another origin."""

    extensions_map = {
        **SimpleHTTPRequestHandler.extensions_map,
        ".js": "application/javascript",
        ".json": "application/json",
        ".map": "application/json",
        ".css": "text/css",
        ".html": "text/html",
    }

    def end_headers(self) -> None:
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Cache-Control", "no-store")
        super().end_headers()

    def log_message(self, fmt: str, *args) -> None:  # noqa: A003 - matches the base signature
        sys.stderr.write("%s - %s\n" % (self.address_string(), fmt % args))


def main() -> int:
    directory = sys.argv[1]
    port = int(sys.argv[2])
    handler = functools.partial(CorsHandler, directory=directory)
    ThreadingHTTPServer(("127.0.0.1", port), handler).serve_forever()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
