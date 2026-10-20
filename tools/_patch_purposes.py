import pathlib

path = pathlib.Path(__file__).with_name("javadoc_enricher.py")
text = path.read_text(encoding="utf-8")

replacements = [
    ('"Creates a new {cls} with the supplied values. The constructor keeps the instance in a "',
     '"Creates a new instance of {@code {cls}} with the supplied collaborators and initial state. "'),
    ('"Builds a {cls} from the given arguments. Every required value is assigned exactly once, "',
     '"Builds an instance of {@code {cls}} from the given arguments. Every required value is "'),
    ('"Builds a {cls} view from the supplied source object. The conversion centralises the "',
     '"Builds a new {@code {cls}} from the supplied source object. The conversion centralises the "'),
    ('"Creates a {cls} representation of the given source. The method is static because the "',
     '"Creates a new {@code {cls}} representation of the given source. The method is static because the "'),
    ('"Performs the {name} operation described by its signature. The method keeps a single "',
     '"Performs the {@code {name}} operation described by its signature. The method keeps a single "'),
    ('"Implements the {name} step of the flow. Inputs are expected to be validated at the "',
     '"Implements the {@code {name}} step of the flow. Inputs are expected to be validated at the "'),
    ('"Declares the {name} bean used by the application context. Keeping the construction in a "',
     '"Declares the {@code {name}} bean used by the application context. Keeping the construction in a "'),
    ('"Registers the {name} collaborator in the Spring context. The bean is built from typed "',
     '"Registers the {@code {name}} collaborator in the Spring context. The bean is built from typed "'),
    ('"Applies the {name} operation to this aggregate. The method enforces the invariants that "',
     '"Applies the {@code {name}} operation to this aggregate. The method enforces the invariants that "'),
    ('"Implements the {name} domain behaviour. Business rules live here instead of in the "',
     '"Implements the {@code {name}} domain behaviour. Business rules live here instead of in the "'),
    ('"Implements the {name} use case. The method coordinates the domain object, the repositories "',
     '"Implements the {@code {name}} use case. The method coordinates the domain object, the repositories "'),
    ('"Executes the {name} application operation. It resolves the caller from the security "',
     '"Executes the {@code {name}} application operation. It resolves the caller from the security "'),
    ('"Implements the standard {name} contract for this type. The implementation is consistent "',
     '"Implements the standard {@code {name}} contract for this type. The implementation is consistent "'),
    ('"Provides the conventional {name} behaviour expected from a Java object. Equality and hash "',
     '"Provides the conventional {@code {name}} behaviour expected from a Java object. Equality and hash "'),
]

for old, new in replacements:
    if old not in text:
        raise SystemExit(f"not found: {old[:60]}")
    text = text.replace(old, new)

path.write_text(text, encoding="utf-8")
print("purposes patched")
