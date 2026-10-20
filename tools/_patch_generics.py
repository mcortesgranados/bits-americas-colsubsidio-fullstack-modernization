import pathlib

path = pathlib.Path(__file__).with_name("javadoc_enricher.py")
text = path.read_text(encoding="utf-8")

old = '''    r"(?:(?:public|protected|private|static|final|abstract|synchronized|default|native)\\s+)*"
    r"(?P<ret>(?:[\\w.$]+(?:<[^;{}()]*>)?(?:\\[\\])?\\s+)?)"'''
new = '''    r"(?:(?:public|protected|private|static|final|abstract|synchronized|default|native)\\s+)*"
    r"(?P<typeparams><[A-Za-z0-9_,\\s?.\\[\\]&<>]+>\\s*)?"
    r"(?P<ret>(?:[\\w.$]+(?:<[^;{}()]*>)?(?:\\[\\])?\\s+)?)"'''
if old not in text:
    raise SystemExit("member head not found")
text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("generic method support added")
