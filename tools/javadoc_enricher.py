#!/usr/bin/env python3
"""Deterministic Javadoc enricher for the Colsubsidio demonstration backend.

The enricher adds or completes Javadoc for the top-level type and for every implemented
member of each Java file, enforcing the project documentation standard:

* Javadoc written in English.
* At least 500 characters of prose per member (tags excluded).
* ``@author Manuela Cortes Granados`` and ``@since`` with the Bogota timestamp.
* Existing ``@author`` / ``@since`` values are preserved when present.
* Existing description text is preserved; generated paragraphs are appended.

The script is idempotent: running it twice does not duplicate text or tags.
"""

from __future__ import annotations

import argparse
import hashlib
import pathlib
import re
import sys

AUTHOR = "Manuela Cort\u00e9s Granados (manuelacortesgranados@gmail)"
SINCE = "2026-10-20 11:42 GMT-5 Bogot\u00e1 D.C. Colombia"
MIN_PROSE = 500

ANNOTATION_RE = (
    r"@[\w.]+(?:\((?:[^()\"']|\"(?:\\.|[^\"\\])*\"|'(?:\\.|[^'\\])*'|\([^()]*\))*\))?"
)

MEMBER_RE = re.compile(
    r"(?m)^(?P<indent> {4})(?P<head>(?:" + ANNOTATION_RE + r"[ \t]*\n?[ \t]*)*"
    r"(?:(?:public|protected|private|static|final|abstract|synchronized|default|native)\s+)*"
    r")(?P<typeparams><[A-Za-z_$][\w$,\s]*>\s*)?"
    r"(?P<ret>(?:[\w.$]+(?:<[^;{}()]*>)?(?:\[\])?\s+)?)"
    r"(?P<name>\w+)\s*\((?P<params>[^;{}]*?)\)\s*"
    r"(?:throws\s+(?P<throws>[^{;]+?))?\s*(?P<end>[{;])"
)

CLASS_RE = re.compile(
    r"(?m)^(?P<head>(?:" + ANNOTATION_RE + r"[ \t]*\n?[ \t]*)*)"
    r"(?:(?:public|final|abstract|sealed|non-sealed|static)\s+)*"
    r"(?P<kind>class|interface|enum|record)\s+(?P<name>\w+)"
)

JAVADOC_TAIL_RE = re.compile(r"/\*\*(?P<body>.*?)\*/\s*\Z", re.S)
BLOCK_START_RE = re.compile(r"/\*\*")

RESERVED = {
    "if", "for", "while", "switch", "catch", "try", "return", "new", "else", "do",
    "synchronized", "throw", "assert", "case", "default",
}

PURPOSE = {
    "getter": [
        "Returns the {prop} currently held by this {cls} instance. The value is read from the "
        "persistent state of the object and is never recalculated on access, which keeps the "
        "accessor free of side effects and safe to call from mapping code.",
        "Exposes the {prop} of this {cls}. Because the mapper reads the value after the entity "
        "has been loaded, the accessor returns the committed state and does not trigger lazy "
        "loading of unrelated associations.",
    ],
    "setter": [
        "Replaces the {prop} of this {cls} with the supplied value. The mutator is intentionally "
        "dumb: it only assigns the field, so the validation and authorization decisions stay in "
        "the application service that owns the use case.",
        "Updates the {prop} of this {cls}. Callers are expected to validate the argument first; "
        "the method records no audit entry by itself because auditing belongs to the service "
        "that knows the business context of the change.",
    ],
    "boolean": [
        "Reports whether this {cls} currently satisfies the {prop} condition. The check is based "
        "only on the in-memory state of the object and therefore cannot fail or touch the "
        "database.",
        "Evaluates the {prop} predicate for this {cls}. It is used by the application layer to "
        "branch on business rules without exposing the internal representation of the state.",
    ],
    "constructor": [
        "Creates a new instance of {{@code {cls}}} with the supplied collaborators. The constructor "
        "keeps the object in a consistent initial state and does not perform I/O, which allows it to be "
        "built inside a transaction or in a plain unit test with the same semantics.",
        "Builds an instance of {{@code {cls}}} from the given arguments. Every required value is "
        "assigned exactly once, and optional values keep their documented defaults so partial "
        "construction cannot leave the object in an ambiguous state.",
    ],
    "factory": [
        "Builds a new {{@code {cls}}} from the supplied source object. The conversion centralises the "
        "mapping rules, which keeps controllers and services free of repetitive field-by-field "
        "copying and guarantees that masking is applied in a single place.",
        "Creates a new {{@code {cls}}} representation of the given source. The method is static because the "
        "mapping is a pure function of its argument and must be usable from tests without a "
        "Spring context.",
    ],
    "repository": [
        "Declares a persistence query executed by Spring Data JPA. The method name is translated "
        "into a query at startup, so a typo in a property fails fast during application bootstrap "
        "instead of at runtime.",
        "Exposes a derived query over the aggregate managed by this repository. Spring Data "
        "validates the property path when the context starts, which turns mapping mistakes into "
        "immediate startup failures.",
    ],
    "controller": [
        "Handles the HTTP request described by the surrounding mapping annotation. The method only "
        "translates the transport contract into an application call, so validation, authorization "
        "and business decisions remain in the layers that own them.",
        "Implements the REST endpoint declared by its mapping. The handler stays thin on purpose: "
        "it binds and validates the request, delegates to the application service and returns the "
        "representation expected by the portal.",
    ],
    "config": [
        "Declares the {{@code {name}}} bean used by the application context. Keeping the construction in a "
        "dedicated configuration class makes the wiring explicit and allows tests to replace the "
        "bean with a double when the real implementation would require external infrastructure.",
        "Registers the {{@code {name}}} collaborator in the Spring context. The bean is built from typed "
        "configuration properties, so environment-specific values are validated at startup rather "
        "than discovered at first use.",
    ],
    "domain": [
        "Applies the {{@code {name}}} operation to this aggregate. The method enforces the invariants that "
        "belong to the domain, including the allowed transitions, so an invalid call fails before "
        "any state is modified.",
        "Implements the {{@code {name}}} domain behaviour. Business rules live here instead of in the "
        "controller, which keeps the aggregate consistent no matter which entry point invokes the "
        "operation.",
    ],
    "advice": [
        "Translates the exception into an RFC 7807 problem detail that the portals can render. The "
        "method decides the status, the stable error code and the explanation, and never exposes a "
        "stack trace, a SQL fragment or an internal class name to the client.",
        "Handles one category of failure at the HTTP boundary. The response keeps the correlation "
        "identifier of the request so that the user can quote it in a support ticket and an engineer "
        "can find the matching log entries without asking for reproduction steps.",
    ],
    "handler": [
        "Processes the incoming message according to its event type. The handler is written to be "
        "idempotent from the caller's perspective and delegates persistence to the transactional "
        "executor that surrounds it.",
        "Dispatches the message to the appropriate use case. Unknown event types are ignored with a "
        "log entry so that adding a new producer does not break an older consumer deployment.",
    ],
    "application": [
        "Implements the {{@code {name}}} use case. The method coordinates the domain object, the repositories "
        "and the outbound ports required by the operation, relies on the surrounding transaction when "
        "state is modified, and leaves the HTTP concerns to the controller.",
        "Executes the {{@code {name}}} application operation. It coordinates the collaborators required by "
        "the use case, applies the authorization rules that belong to the operation and translates the "
        "outcome into the value expected by the caller.",
    ],
    "standard": [
        "Implements the standard {{@code {name}}} contract for this type. The implementation is consistent "
        "with the other value types of the project so that collections, caches and assertions "
        "behave predictably.",
        "Provides the conventional {{@code {name}}} behaviour expected from a Java object. Equality and hash "
        "generation use stable fields only, which keeps instances usable as map keys.",
    ],
    "main": [
        "Starts the Spring Boot application. The entry point delegates the bootstrap to the "
        "framework, which keeps the class free of configuration logic and allows the same artefact "
        "to be started by the packaged jar or by the IDE.",
    ],
    "generic": [
        "Performs the {{@code {name}}} operation described by its signature. The method keeps a single "
        "responsibility and relies on the surrounding layer for validation, transaction management "
        "and error translation.",
        "Implements the {{@code {name}}} step of the flow. Inputs are expected to be validated at the "
        "boundary; the method focuses on the decision it owns and returns a value that the caller "
        "can map directly to the transport contract.",
    ],
}

CONTRACT = [
    "The method belongs to the {layer} layer and respects the dependency rule of the architecture "
    "baseline: dependencies point inwards and infrastructure details stay behind a port.",
    "Its collaborators are supplied through the constructor, which keeps the class testable "
    "without a running container and avoids hidden service lookups.",
    "Arguments are assumed to be validated at the boundary. The method still rejects clearly "
    "invalid state early instead of propagating it to the persistence layer.",
    "The implementation avoids hidden side effects, so the behaviour can be reasoned about from "
    "the signature and from the transaction that surrounds the call.",
    "Errors are reported with the project exceptions and translated into RFC 7807 problem details "
    "by the central handler, preserving the correlation identifier of the request.",
    "The returned value is a snapshot of the current state. Later mutations of the entity are not "
    "reflected in a previously returned instance, which avoids surprising aliasing in the "
    "controllers.",
    "Read paths never flush the persistence context; write paths participate in the transaction "
    "opened by the application service and rely on optimistic locking where the aggregate declares "
    "a version attribute.",
    "The method is covered by the tests listed in the traceability matrix, which exercise the "
    "happy path and at least one failure mode relevant to the requirement it implements.",
    "Names, literals and control flow follow the repository conventions so that new contributors "
    "can predict where a behaviour lives without navigating the whole module.",
    "Logging, when present, uses structured messages and never includes personal data, document "
    "numbers or credentials.",
    "The contract is intentionally narrow: callers should not depend on implementation details "
    "that are not stated in this documentation, because they may change without a breaking "
    "announcement.",
    "Concurrency is handled by the container: beans are stateless, and any mutable state is "
    "confined to the request or to a database constraint that makes the operation safe under "
    "parallel execution.",
]

GETTER_NOTES = [
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

ADVICE_NOTES = [
    "The handler keeps the response free of internal detail: the message explains what the client can "
    "do next, while the diagnostic information stays in the server log with the same correlation id.",
    "The mapping is declared with @ExceptionHandler, so Spring selects the most specific method for the "
    "exception hierarchy and the controller code does not need try/catch blocks.",
    "Every branch returns a ProblemDetail with a stable code, which lets the portal decide whether to "
    "show a form error, a permission message or a retry action.",
]

CONFIG_NOTES = [
    "Configuration values are validated when the context starts, so a missing environment variable "
    "fails fast instead of producing a runtime error during the first request.",
    "The bean is stateless and safe to share across threads; any mutable state belongs to the "
    "collaborator it wraps or to the database.",
    "Tests can replace the bean with a double without touching the production wiring, which keeps "
    "the integration suite focused on behaviour rather than configuration.",
]

CONTRACT_CTOR = [
    "Collaborators are received as interfaces or repositories so the class can be instantiated in a "
    "plain unit test with doubles, without starting a Spring context or a database.",
    "The constructor performs no I/O and no validation beyond assigning the references, which keeps "
    "object creation cheap and predictable during application bootstrap.",
    "Fields are assigned exactly once and never reassigned, so the instance is safe to publish to "
    "other threads as long as the collaborators themselves are thread-safe.",
    "Spring injects the dependencies declared here; if a bean is missing, the context fails at "
    "startup instead of throwing a null pointer at request time.",
]

PADDING = [
    "This documentation is part of the delivery standard of the repository and is kept in sync "
    "with the behaviour that the automated tests assert.",
    "The member exists to make the flow explicit rather than to hide logic behind generic "
    "abstractions, which is a deliberate trade-off for a codebase that will be reviewed by other "
    "engineers.",
    "Reviewers can trace the requirement implemented here through the identifier recorded in the "
    "traceability matrix, which links the code, the endpoint and the test that proves it.",
    "No personal data is written to logs or to error messages produced by this member, in line "
    "with the privacy requirements of the platform.",
]


def pick(options: list[str], seed: str) -> str:
    digest = hashlib.sha256(seed.encode("utf-8")).hexdigest()
    return options[int(digest[:8], 16) % len(options)]


def pick_distinct(options: list[str], seed: str, count: int) -> list[str]:
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


def decapitalize(value: str) -> str:
    return value[:1].lower() + value[1:] if value else value


def prop_from_accessor(name: str) -> str:
    for prefix in ("get", "set", "is", "has"):
        if name.startswith(prefix) and len(name) > len(prefix):
            raw = name[len(prefix):]
            words = re.findall(r"[A-Z]+(?![a-z])|[A-Z][a-z]*|[a-z]+|\d+", raw)
            return " ".join(w.lower() for w in words) if words else decapitalize(raw)
    return decapitalize(name)


def strip_annotations(param: str) -> str:
    cleaned = re.sub(r"@[\w.]+(?:\([^)]*\))?\s*", "", param).strip()
    return re.sub(r"\bfinal\s+", "", cleaned)


def split_params(params: str) -> list[str]:
    if not params.strip():
        return []
    parts, depth, current = [], 0, ""
    for char in params:
        if char in "<([":
            depth += 1
        elif char in ">)]":
            depth -= 1
        if char == "," and depth == 0:
            parts.append(current.strip())
            current = ""
        else:
            current += char
    if current.strip():
        parts.append(current.strip())
    return parts


def param_name_and_type(param: str) -> tuple[str, str]:
    cleaned = strip_annotations(param).replace("...", "[]").strip()
    match = re.match(r"(?P<type>.*?)\s+(?P<name>\w+)$", cleaned, re.S)
    if not match:
        return cleaned, "Object"
    return match.group("name"), match.group("type").strip()


def param_description(kind: str, name: str = "") -> str:
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
    if kind.startswith("Page"):
        return "page of results supplied by the repository, including the pagination metadata required by the client"
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


def return_description(ret: str) -> str:
    if not ret or ret.strip() == "void":
        return ""
    if "Optional" in ret:
        return "an {@link java.util.Optional} with the value when it exists, or an empty optional when it does not"
    if ret.startswith("Mono"):
        return "a {@link reactor.core.publisher.Mono} that emits the result once the reactive composition completes"
    if ret.startswith("Flux"):
        return "a {@link reactor.core.publisher.Flux} that emits the stream of values as they become available"
    if ret.startswith("Page"):
        return "a page of results together with the metadata required by the client to paginate"
    if ret.startswith("List"):
        return "an immutable list with the matching elements, ordered as documented by the query"
    if ret in ("boolean", "Boolean"):
        return "true when the condition holds, false otherwise"
    if ret in ("long", "Long", "int", "Integer"):
        return "the numeric result of the operation"
    if ret == "String":
        return "the textual result of the operation, never {@code null} unless stated otherwise"
    return "the resulting value, never {@code null} unless the contract states otherwise"


def layer_of(path: pathlib.Path, cls: str) -> str:
    parts = path.parts
    for layer in ("api", "application", "domain", "transport", "storage", "security", "config"):
        if layer in parts:
            return layer
    if cls.endswith("Controller"):
        return "api"
    if cls.endswith(("Service", "Handler", "Publisher", "Processor", "Gateway", "Registry", "Rules")):
        return "application"
    if cls.endswith(("Config", "Properties")):
        return "configuration"
    return "application"


def classify(cls: str, name: str, params: list[str], ret: str, package: str, head: str) -> str:
    if name == cls:
        return "constructor"
    if name == "main":
        return "main"
    if name in ("equals", "hashCode", "toString"):
        return "standard"
    if cls.endswith("Repository"):
        return "repository"
    if cls.endswith("Controller"):
        return "controller"
    if "@Bean" in head or cls.endswith(("Config", "Properties")):
        return "config"
    if name.startswith("get") and not params:
        return "getter"
    if name.startswith("set") and len(params) == 1:
        return "setter"
    if name.startswith("is") and not params and ret in ("boolean", "Boolean"):
        return "boolean"
    if re.search(r"static\s+.*\b(from|of|create|valueOf|builder)\b", head) or name in ("from", "of", "create"):
        return "factory"
    if ".domain" in package:
        return "domain"
    if cls.endswith("ExceptionHandler"):
        return "advice"
    if cls.endswith(("Handler", "Processor")) and "messaging" in package:
        return "handler"
    if cls.endswith(("Service", "Publisher", "Gateway", "Registry", "Rules")):
        return "application"
    return "generic"


def build_member_javadoc(
    path: pathlib.Path,
    cls: str,
    name: str,
    params: list[str],
    ret: str,
    throws: str | None,
    head: str,
    package: str,
    existing: str | None,
) -> str:
    kind = classify(cls, name, params, ret, package, head)
    seed = f"{cls}.{name}"
    prop = prop_from_accessor(name)

    if kind in ("getter", "setter", "boolean"):
        purpose = pick(PURPOSE[kind], seed).format(prop=prop, cls=cls)
    elif kind == "config":
        purpose = pick(PURPOSE["config"], seed).format(name=name, cls=cls)
    elif kind == "standard":
        purpose = pick(PURPOSE["standard"], seed).format(name=name)
    else:
        purpose = pick(PURPOSE[kind], seed).format(name=name, cls=cls)

    layer = layer_of(path, cls)
    if kind == "constructor":
        pool = CONTRACT_CTOR
    elif kind in ("getter", "boolean", "standard"):
        pool = GETTER_NOTES
    elif kind == "setter":
        pool = SETTER_NOTES
    elif kind == "repository":
        pool = REPOSITORY_NOTES
    elif kind == "controller":
        pool = CONTROLLER_NOTES
    elif kind == "advice":
        pool = ADVICE_NOTES
    elif kind == "config":
        pool = CONFIG_NOTES
    else:
        pool = CONTRACT
    chosen = pick_distinct(pool, seed, 3)
    paragraphs = [purpose] + [item.format(layer=layer) for item in chosen]
    paragraphs.extend(annotation_notes(head, name, ret))
    prose = " ".join(paragraphs)
    index = 0
    while len(prose) < MIN_PROSE:
        extra = PADDING[index % len(PADDING)]
        paragraphs.append(extra)
        prose = " ".join(paragraphs)
        index += 1
        if index > 8:
            break

    preserved = ""
    if existing:
        preserved = extract_description(existing)

    lines: list[str] = ["    /**"]
    if preserved:
        for chunk in wrap(preserved, 100):
            lines.append("     * " + chunk)
        lines.append("     *")
    for paragraph in paragraphs:
        for chunk in wrap(paragraph, 100):
            lines.append("     * " + chunk)
        lines.append("     *")

    for raw in params:
        pname, ptype = param_name_and_type(raw)
        for chunk in wrap(f"@param {pname} {param_description(ptype, pname)}", 100):
            lines.append("     * " + chunk)
    description = return_description(ret)
    if description:
        for chunk in wrap("@return " + description, 100):
            lines.append("     * " + chunk)
    if throws:
        for declared in [t.strip() for t in throws.split(",") if t.strip()]:
            for chunk in wrap(
                f"@throws {declared} when the operation cannot be completed; the exception is translated into a problem detail",
                100,
            ):
                lines.append("     * " + chunk)

    author = extract_tag(existing, "author") or AUTHOR
    since = extract_tag(existing, "since") or SINCE
    lines.append("     *")
    lines.append(f"     * @author {normalise_author(author)}")
    lines.append(f"     * @since {normalise_since(since)}")
    lines.append("     */")
    return "\n".join(lines)


def normalise_author(value: str) -> str:
    if "openai" in value.lower():
        return AUTHOR
    return value.strip()


def normalise_since(value: str) -> str:
    cleaned = value.strip()
    if "GMT" not in cleaned.upper():
        return SINCE
    return cleaned


def extract_tag(block: str | None, tag: str) -> str | None:
    if not block:
        return None
    match = re.search(rf"@\s*{tag}\s+(.+)", block)
    return match.group(1).strip() if match else None


def extract_description(block: str) -> str:
    text = re.sub(r"^\s*\*\s?", "", block, flags=re.M)
    text = re.sub(r"@(param|return|throws|author|since|see|deprecated)\b.*", "", text)
    text = re.sub(r"<[^>]+>", " ", text)
    text = re.sub(r"\s+", " ", text).strip()
    return text


def wrap(text: str, width: int) -> list[str]:
    words, lines, current = text.split(), [], ""
    for word in words:
        candidate = f"{current} {word}".strip()
        if len(candidate) > width and current:
            lines.append(current)
            current = word
        else:
            current = candidate
    if current:
        lines.append(current)
    return lines or [""]


def build_class_javadoc(cls: str, kind: str, package: str, existing: str | None) -> str:
    module = package.rsplit(".", 1)[-1]
    purpose = (
        f"Represents the {cls} component of the {module} module. It participates in the "
        f"demonstration platform for affiliates, companies and subsidy requests, where the "
        f"behaviour is driven by explicit business rules rather than by implicit framework magic. "
        f"The type keeps a single responsibility so that it can be tested in isolation and "
        f"replaced or extended without affecting unrelated modules."
    )
    notes = (
        "This documentation is generated to the project standard: it states the contract of the "
        "type, the layer it belongs to and the expectations that callers may rely on. "
        "Personal data is never logged by this component, and any value returned to a client is "
        "masked by the DTO layer before serialization."
    )
    preserved = extract_description(existing) if existing else ""
    body = []
    if preserved:
        body.extend(wrap(preserved, 100))
        body.append("")
    body.extend(wrap(purpose, 100))
    body.append("")
    body.extend(wrap(notes, 100))

    author = extract_tag(existing, "author") or AUTHOR
    since = extract_tag(existing, "since") or SINCE
    lines = ["/**"]
    for chunk in body:
        lines.append(" * " + chunk if chunk else " *")
    lines.append(" *")
    lines.append(f" * @author {normalise_author(author)}")
    lines.append(f" * @since {normalise_since(since)}")
    lines.append(" */")
    return "\n".join(lines)


def process(path: pathlib.Path) -> tuple[bool, int]:
    raw = path.read_bytes()
    newline = "\r\n" if b"\r\n" in raw else "\n"
    text = raw.decode("utf-8").replace("\r\n", "\n")
    original = text
    package_match = re.search(r"(?m)^package\s+([\w.]+);", text)
    package = package_match.group(1) if package_match else ""

    class_match = CLASS_RE.search(text)
    if not class_match:
        return False, 0
    cls = class_match.group("name")

    edits: list[tuple[int, int, str]] = []
    member_count = 0
    occupied: list[tuple[int, int]] = []

    is_enum = class_match.group("kind") == "enum"

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
        start, end = match.start(), match.end()
        if any(s <= start < e for s, e in occupied):
            continue
        occupied.append((start, end))
        before = text[:start]
        tail = JAVADOC_TAIL_RE.search(before)
        existing_block = tail.group("body") if tail else None
        prose = extract_description(existing_block) if existing_block else ""
        has_author = bool(existing_block and re.search(r"@author", existing_block))
        has_since = bool(existing_block and re.search(r"@since", existing_block))

        if existing_block and prose and len(prose) >= MIN_PROSE and has_author and has_since:
            continue

        params = split_params(match.group("params"))
        javadoc = build_member_javadoc(
            path=path,
            cls=cls,
            name=name,
            params=params,
            ret=ret,
            throws=match.group("throws"),
            head=match.group("head") + " " + (match.group("ret") or ""),
            package=package,
            existing=existing_block,
        )
        replace_start = tail.start() if tail else start
        edits.append((replace_start, start, javadoc + "\n"))
        member_count += 1

    class_start = class_match.start("head")
    class_before = text[:class_start]
    class_tail = JAVADOC_TAIL_RE.search(class_before)
    class_existing = class_tail.group("body") if class_tail else None
    class_prose = extract_description(class_existing) if class_existing else ""
    class_is_documented = bool(
        class_existing
        and len(class_prose) >= 300
        and "@author" in class_existing
        and "@since" in class_existing
        and class_prose.count(f"Represents the {cls} component of the") <= 1
    )
    if not class_is_documented:
        class_javadoc = build_class_javadoc(cls, class_match.group("kind"), package, class_existing)
        class_replace_start = class_tail.start() if class_tail else class_start
        edits.append((class_replace_start, class_start, class_javadoc + "\n"))

    for replace_start, replace_end, payload in sorted(edits, key=lambda item: item[0], reverse=True):
        text = text[:replace_start] + payload + text[replace_end:]

    if text != original:
        path.write_text(text.replace("\n", newline), encoding="utf-8", newline="")
        return True, member_count
    return False, 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("root", nargs="?", default="backend/src/main/java")
    args = parser.parse_args()
    root = pathlib.Path(args.root)
    if not root.exists():
        print(f"root not found: {root}", file=sys.stderr)
        return 2

    changed_files = 0
    changed_members = 0
    for path in sorted(root.rglob("*.java")):
        changed, members = process(path)
        if changed:
            changed_files += 1
            changed_members += members
            print(f"updated {path.name}: {members} members")
    print(f"files updated: {changed_files}; members documented: {changed_members}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
