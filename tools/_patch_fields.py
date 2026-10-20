import pathlib

path = pathlib.Path(__file__).with_name("javadoc_enricher.py")
text = path.read_text(encoding="utf-8")

old = '''    for match in MEMBER_RE.finditer(text):
        name = match.group("name")
        if name in RESERVED:
            continue
'''
new = '''    is_enum = class_match.group("kind") == "enum"

    for match in MEMBER_RE.finditer(text):
        name = match.group("name")
        ret = match.group("ret") or ""
        if name in RESERVED:
            continue
        # Field initialisers, enum constants and method calls can look like declarations once
        # parentheses appear in the statement; discard them before generating any documentation.
        declaration = re.sub(ANNOTATION_RE, " ", match.group("head") + ret)
        if "=" in declaration or ret.strip().endswith("."):
            continue
        if is_enum and name.isupper():
            continue
'''
if old not in text:
    raise SystemExit("member loop not found")
text = text.replace(old, new, 1)

# Reuse the already computed return type instead of reading the group again.
text = text.replace('            ret=match.group("ret") or "",', '            ret=ret,', 1)

path.write_text(text, encoding="utf-8")
print("field guard applied")
