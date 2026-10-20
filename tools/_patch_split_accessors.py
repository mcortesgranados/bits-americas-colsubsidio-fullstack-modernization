import pathlib

path = pathlib.Path(__file__).with_name("javadoc_enricher.py")
text = path.read_text(encoding="utf-8")

old_start = text.index("ACCESSOR_NOTES = [")
old_end = text.index("REPOSITORY_NOTES = [")
new_pool = '''GETTER_NOTES = [
    "Accessors stay trivial on purpose: JPA field access remains the single source of truth for "
    "persistence, and no business decision is hidden inside a getter.",
    "The member is used by the DTO mappers, so its return type is treated as a stable internal API "
    "even though it is not published directly to clients.",
    "The value is read from the state already loaded in the persistence context; accessing it does "
    "not trigger additional queries or lazy loading of unrelated associations.",
    "The getter never mutates the entity nor normalises the value, which keeps mapping code free of "
    "surprising side effects.",
]

SETTER_NOTES = [
    "Mutators assume that the caller already validated the value; the aggregate only enforces the "
    "invariants that belong to its own state, which keeps the entity free of transport concerns.",
    "The assignment is not audited here: auditing belongs to the application service, which knows "
    "the actor, the reason and the correlation identifier of the change.",
    "The setter does not persist or flush; the owning transaction decides when the change reaches "
    "the database, and optimistic locking protects concurrent updates.",
    "Direct field access is preserved for JPA, so the mutator only exists to serve the application "
    "layer and the tests.",
]

'''
text = text[:old_start] + new_pool + text[old_end:]

old = '''    elif kind in ("getter", "setter", "boolean", "standard"):
        pool = ACCESSOR_NOTES'''
new = '''    elif kind in ("getter", "boolean", "standard"):
        pool = GETTER_NOTES
    elif kind == "setter":
        pool = SETTER_NOTES'''
if old not in text:
    raise SystemExit("selection not found")
text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("accessor pools split")
