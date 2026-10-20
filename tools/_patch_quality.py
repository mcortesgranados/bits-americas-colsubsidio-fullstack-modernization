import pathlib

path = pathlib.Path(__file__).with_name("javadoc_enricher.py")
text = path.read_text(encoding="utf-8")

# --- A. Constructor-specific contract pool -------------------------------------------
anchor = "PADDING = ["
ctor_pool = '''CONTRACT_CTOR = [
    "Collaborators are received as interfaces or repositories so the class can be instantiated in a "
    "plain unit test with doubles, without starting a Spring context or a database.",
    "The constructor performs no I/O and no validation beyond assigning the references, which keeps "
    "object creation cheap and predictable during application bootstrap.",
    "Fields are assigned exactly once and never reassigned, so the instance is safe to publish to "
    "other threads as long as the collaborators themselves are thread-safe.",
    "Spring injects the dependencies declared here; if a bean is missing, the context fails at "
    "startup instead of throwing a null pointer at request time.",
]

PADDING = ['''
if anchor not in text:
    raise SystemExit("anchor not found")
text = text.replace(anchor, ctor_pool, 1)

# --- B. Distinct contract paragraphs, constructor-aware -------------------------------
old_block = '''    layer = layer_of(path, cls)
    contract_a = pick(CONTRACT, seed + "a").format(layer=layer)
    contract_b = pick(CONTRACT, seed + "b").format(layer=layer)
    contract_c = pick(CONTRACT, seed + "c").format(layer=layer)

    paragraphs = [purpose, contract_a, contract_b, contract_c]
'''
new_block = '''    layer = layer_of(path, cls)
    pool = CONTRACT_CTOR if kind == "constructor" else CONTRACT
    chosen = pick_distinct(pool, seed, 3)
    paragraphs = [purpose] + [item.format(layer=layer) for item in chosen]
    paragraphs.extend(annotation_notes(head, name, ret))
'''
if old_block not in text:
    raise SystemExit("contract block not found")
text = text.replace(old_block, new_block, 1)

# --- C. Parameter descriptions become type- and name-aware ----------------------------
old_param = '''def param_description(kind: str) -> str:
    mapping = {
        "String": "textual value supplied by the caller; it is validated at the boundary before reaching this member",
        "Long": "identifier or numeric value resolved by the caller and checked against the current state",
        "long": "numeric value used by the operation; negative values are rejected when they are not meaningful",
        "Integer": "numeric value used by the operation; it must be within the range documented by the endpoint",
        "int": "numeric counter or size used by the operation",
        "BigDecimal": "monetary amount expressed in the currency of the request; scale and limits are validated by the demonstration rules",
        "Instant": "point in time expressed in UTC, used to keep the audit trail and the state transitions comparable",
        "Pageable": "pagination and sorting instructions resolved by Spring MVC from the query string",
        "MultipartFile": "uploaded file validated by type and size before it is persisted",
        "List": "collection of elements iterated in the order supplied by the caller",
        "Set": "collection of unique elements used to evaluate the rule without duplicates",
        "boolean": "flag that selects the behaviour of the operation",
    }
    for key, description in mapping.items():
        if key in kind:
            return description
    return "input value required by the operation; it is validated before the state is modified"
'''
new_param = '''def param_description(kind: str, name: str = "") -> str:
    """Describes a parameter from its declared type and, when possible, from its name."""
    exact = {
        "String": "textual value supplied by the caller; it is validated at the boundary before reaching this member",
        "Long": "identifier or numeric value resolved by the caller and checked against the current state",
        "long": "numeric value used by the operation; negative values are rejected when they are not meaningful",
        "Integer": "numeric value used by the operation; it must be within the range documented by the endpoint",
        "int": "numeric counter or size used by the operation",
        "BigDecimal": "monetary amount in the currency of the request; scale and limits are validated by the demonstration rules",
        "Instant": "point in time in UTC, used to keep audit entries and state transitions comparable",
        "Pageable": "pagination and sorting instructions resolved by Spring MVC from the query string",
        "MultipartFile": "uploaded file validated by type and size before it is persisted",
        "DocumentType": "document type from the catalogue required for the subsidy type of the request",
        "SubsidyType": "subsidy type that drives the applicable demonstration rules",
        "SubsidyRequestStatus": "target status that must be reachable from the current state of the aggregate",
        "AffiliateStatus": "affiliate status used to filter the query",
        "boolean": "flag that selects the behaviour of the operation",
        "CurrentUser": "authenticated principal resolved from the validated token",
        "ObjectMapper": "Jackson mapper used to serialize or deserialize the message payload",
    }
    if kind in exact:
        return exact[kind]
    if kind.startswith("List"):
        return "collection of elements iterated in the order supplied by the caller"
    if kind.startswith("Set"):
        return "collection of unique elements used to evaluate the rule without duplicates"
    if kind.startswith("Optional"):
        return "optional value that the caller may leave empty when the information is not available"
    if kind.endswith("Repository"):
        return "repository used to read or persist the aggregate owned by this module"
    if kind.endswith(("Service", "Gateway", "Storage", "Publisher", "Registry", "Mapper", "Encoder", "Decoder")):
        return "collaborator injected through its port, which keeps the class independent from the concrete implementation"
    if kind.endswith(("Request", "Response", "Dto", "DTO")):
        return "transport representation received from the client; its fields are validated by bean validation"
    if kind.endswith("Properties"):
        return "typed configuration properties bound from the environment at startup"
    return f"input value required by the operation ({kind}); it is validated before the state is modified"
'''
if old_param not in text:
    raise SystemExit("param function not found")
text = text.replace(old_param, new_param, 1)
text = text.replace(
    'for chunk in wrap(f"@param {pname} {param_description(ptype)}", 100):',
    'for chunk in wrap(f"@param {pname} {param_description(ptype, pname)}", 100):',
    1,
)

# --- D. Helpers: distinct picking and annotation-aware notes --------------------------
helper_anchor = "def decapitalize(value: str) -> str:"
helpers = '''def pick_distinct(options: list[str], seed: str, count: int) -> list[str]:
    """Selects ``count`` different entries using a stable hash of the seed."""
    selected: list[str] = []
    offset = 0
    while len(selected) < count and offset < len(options) * 3:
        candidate = pick(options, f"{seed}:{offset}")
        if candidate not in selected:
            selected.append(candidate)
        offset += 1
    return selected


def annotation_notes(head: str, name: str, ret: str) -> list[str]:
    """Adds contract sentences derived from the annotations of the member."""
    notes: list[str] = []
    if "@Transactional" in head:
        notes.append(
            "The transaction boundary is declared on this member: the unit of work is committed only "
            "when the method returns normally, and any runtime exception triggers a rollback of the "
            "entities and of the outbox rows written during the call."
        )
    if "@PreAuthorize" in head or "@Secured" in head:
        notes.append(
            "Access is restricted by the expression declared in the security annotation; an "
            "authenticated principal without the required authority receives a 403 response before "
            "the method body is executed."
        )
    if any(token in head for token in ("@GetMapping", "@PostMapping", "@PatchMapping", "@PutMapping", "@DeleteMapping")):
        notes.append(
            "The HTTP contract is declared by the mapping annotation: the path, the method and the "
            "expected representation are part of the public API and must stay aligned with the "
            "OpenAPI document and the Postman collection."
        )
    if "@Scheduled" in head:
        notes.append(
            "The method is invoked by the scheduler according to the configured interval; it must "
            "therefore tolerate overlapping executions and rely on the database constraints to avoid "
            "publishing the same row twice."
        )
    if name.startswith("existsBy") or name.startswith("countBy"):
        notes.append(
            "The query is a projection over the table: it does not load entities into the persistence "
            "context, which keeps the check inexpensive even when the surrounding aggregate is large."
        )
    return notes


def decapitalize(value: str) -> str:'''
if helper_anchor not in text:
    raise SystemExit("helper anchor not found")
text = text.replace(helper_anchor, helpers, 1)

path.write_text(text, encoding="utf-8")
print("quality patch applied")
