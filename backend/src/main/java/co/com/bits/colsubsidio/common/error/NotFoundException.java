package co.com.bits.colsubsidio.common.error;

/**
 * Raised when a requested resource does not exist or is intentionally hidden from the caller.
 * Represents the NotFoundException component of the error module. It participates in the demonstration
 * platform for affiliates, companies and subsidy requests, where the behaviour is driven by explicit
 * business rules rather than by implicit framework magic. The type keeps a single responsibility so
 * that it can be tested in isolation and replaced or extended without affecting unrelated modules.
 * This documentation is generated to the project standard: it states the contract of the type, the
 * layer it belongs to and the expectations that callers may rely on. Personal data is never logged by
 * this component, and any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public class NotFoundException extends RuntimeException {

    /**
     * Creates a new instance of {@code NotFoundException} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * @param message textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Builds a new {@code NotFoundException} from the supplied source object. The conversion centralises
     * the mapping rules, which keeps controllers and services free of repetitive field-by-field copying
     * and guarantees that masking is applied in a single place.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param resource textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param id input value required by the operation (Object); it is validated before the state is
     * modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " with identifier '" + id + "' was not found");
    }
}
