import pathlib

path = pathlib.Path(__file__).with_name("javadoc_enricher.py")
text = path.read_text(encoding="utf-8")

# 1. Constructor template: repair the truncated continuation.
broken = '''        "Creates a new instance of {{@code {cls}}} with the supplied collaborators and initial state. "
        "consistent initial state and does not perform I/O, which allows the object to be built "
        "inside a transaction or in a plain unit test with the same semantics.",'''
fixed = '''        "Creates a new instance of {{@code {cls}}} with the supplied collaborators. The constructor "
        "keeps the object in a consistent initial state and does not perform I/O, which allows it to be "
        "built inside a transaction or in a plain unit test with the same semantics.",'''
if broken not in text:
    raise SystemExit("constructor template not found")
text = text.replace(broken, fixed, 1)

# 2. Annotation pattern that tolerates parentheses inside quoted arguments.
old_member = '''MEMBER_RE = re.compile(
    r"(?m)^(?P<indent> {4})(?P<head>(?:@[\\w.]+(?:\\([^)]*\\))?[ \\t]*\\n?[ \\t]*)*"'''
new_member = '''ANNOTATION_RE = (
    r"@[\\w.]+(?:\\((?:[^()\\"']|\\"(?:\\\\.|[^\\"\\\\])*\\"|'(?:\\\\.|[^'\\\\])*'|\\([^()]*\\))*\\))?"
)

MEMBER_RE = re.compile(
    r"(?m)^(?P<indent> {4})(?P<head>(?:" + ANNOTATION_RE + r"[ \\t]*\\n?[ \\t]*)*"'''
if old_member not in text:
    raise SystemExit("member regex not found")
text = text.replace(old_member, new_member, 1)

old_class = '''CLASS_RE = re.compile(
    r"(?m)^(?P<head>(?:@[\\w.]+(?:\\([^)]*\\))?[ \\t]*\\n?[ \\t]*)*)"'''
new_class = '''CLASS_RE = re.compile(
    r"(?m)^(?P<head>(?:" + ANNOTATION_RE + r"[ \\t]*\\n?[ \\t]*)*)"'''
if old_class not in text:
    raise SystemExit("class regex not found")
text = text.replace(old_class, new_class, 1)

path.write_text(text, encoding="utf-8")
print("parser patch applied")
