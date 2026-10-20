import pathlib

path = pathlib.Path(__file__).with_name("javadoc_enricher.py")
text = path.read_text(encoding="utf-8")

anchor = "CONTRACT_CTOR = ["
pools = '''ACCESSOR_NOTES = [
    "Accessors stay trivial on purpose: JPA field access remains the single source of truth for "
    "persistence, and no business decision is hidden inside a getter or a setter.",
    "The member is used by the DTO mappers, so its return type is treated as a stable internal API "
    "even though it is not published directly to clients.",
    "Mutators assume that the caller already validated the value; the aggregate only enforces the "
    "invariants that belong to its own state, which keeps the entity free of transport concerns.",
    "The value is read from the state already loaded in the persistence context; accessing it does "
    "not trigger additional queries or lazy loading of unrelated associations.",
]

REPOSITORY_NOTES = [
    "The query runs inside the read-only transaction opened by the application service; no entity "
    "is modified and the persistence context is never flushed by this call.",
    "Filtering and pagination are supplied by the caller, so the method never loads an unbounded "
    "result set into memory.",
    "Property names embedded in the method name are validated when the context starts, which turns "
    "a refactoring mistake into an immediate startup failure instead of a runtime error.",
    "Count and existence queries are projections over the table: they avoid materialising entities "
    "and keep the check inexpensive even for large aggregates.",
]

CONTROLLER_NOTES = [
    "The handler binds and validates the request, delegates the decision to the application service "
    "and returns the representation expected by the portal, without business logic of its own.",
    "Authorization happens before the body executes: the role check is declared on the method and "
    "the ownership check is enforced by the application service.",
    "The response keeps the correlation identifier in the headers so that a user can quote it in a "
    "support request and an engineer can find the matching log entries.",
    "Errors raised by the service are translated into RFC 7807 problem details by the central "
    "exception handler, so this method does not build error payloads by hand.",
]

CONFIG_NOTES = [
    "Configuration values are validated when the context starts, so a missing environment variable "
    "fails fast instead of producing a runtime error during the first request.",
    "The bean is stateless and safe to share across threads; any mutable state belongs to the "
    "collaborator it wraps or to the database.",
    "Tests can replace the bean with a double without touching the production wiring, which keeps "
    "the integration suite focused on behaviour rather than configuration.",
]

CONTRACT_CTOR = ['''
if anchor not in text:
    raise SystemExit("anchor not found")
text = text.replace(anchor, pools, 1)

old = '''    pool = CONTRACT_CTOR if kind == "constructor" else CONTRACT'''
new = '''    if kind == "constructor":
        pool = CONTRACT_CTOR
    elif kind in ("getter", "setter", "boolean", "standard"):
        pool = ACCESSOR_NOTES
    elif kind == "repository":
        pool = REPOSITORY_NOTES
    elif kind == "controller":
        pool = CONTROLLER_NOTES
    elif kind == "config":
        pool = CONFIG_NOTES
    else:
        pool = CONTRACT'''
if old not in text:
    raise SystemExit("pool selection not found")
text = text.replace(old, new, 1)

path.write_text(text, encoding="utf-8")
print("notes pools applied")
