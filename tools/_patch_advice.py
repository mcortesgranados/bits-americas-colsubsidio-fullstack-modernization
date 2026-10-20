import pathlib

path = pathlib.Path(__file__).with_name("javadoc_enricher.py")
text = path.read_text(encoding="utf-8")

# 1. New purpose pool for @RestControllerAdvice classes.
anchor = '    "handler": ['
advice = '''    "advice": [
        "Translates the exception into an RFC 7807 problem detail that the portals can render. The "
        "method decides the status, the stable error code and the explanation, and never exposes a "
        "stack trace, a SQL fragment or an internal class name to the client.",
        "Handles one category of failure at the HTTP boundary. The response keeps the correlation "
        "identifier of the request so that the user can quote it in a support ticket and an engineer "
        "can find the matching log entries without asking for reproduction steps.",
    ],
    "handler": ['''
if anchor not in text:
    raise SystemExit("handler pool not found")
text = text.replace(anchor, advice, 1)

# 2. Classify exception handlers before generic messaging handlers.
old = '''    if cls.endswith(("Handler", "Processor")):
        return "handler"'''
new = '''    if cls.endswith("ExceptionHandler"):
        return "advice"
    if cls.endswith(("Handler", "Processor")) and "messaging" in package:
        return "handler"'''
if old not in text:
    raise SystemExit("classify block not found")
text = text.replace(old, new, 1)

# 3. Dedicated notes for error advice.
anchor2 = "CONFIG_NOTES = ["
advice_notes = '''ADVICE_NOTES = [
    "The handler keeps the response free of internal detail: the message explains what the client can "
    "do next, while the diagnostic information stays in the server log with the same correlation id.",
    "The mapping is declared with @ExceptionHandler, so Spring selects the most specific method for the "
    "exception hierarchy and the controller code does not need try/catch blocks.",
    "Every branch returns a ProblemDetail with a stable code, which lets the portal decide whether to "
    "show a form error, a permission message or a retry action.",
]

CONFIG_NOTES = ['''
if anchor2 not in text:
    raise SystemExit("config notes anchor not found")
text = text.replace(anchor2, advice_notes, 1)

# 4. Use the new notes for the advice kind.
old_pool = '''    elif kind == "config":
        pool = CONFIG_NOTES'''
new_pool = '''    elif kind == "advice":
        pool = ADVICE_NOTES
    elif kind == "config":
        pool = CONFIG_NOTES'''
text = text.replace(old_pool, new_pool, 1)

path.write_text(text, encoding="utf-8")
print("advice kind added")
