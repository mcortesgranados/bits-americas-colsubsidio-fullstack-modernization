package co.com.bits.colsubsidio.common.error;

/**
 * Raised when a request violates a business rule. The {@code code} is a stable machine-readable
 * identifier that clients can map to user-facing messages. Represents the BusinessRuleException
 * component of the error module. It participates in the demonstration platform for affiliates,
 * companies and subsidy requests, where the behaviour is driven by explicit business rules rather than
 * by implicit framework magic. The type keeps a single responsibility so that it can be tested in
 * isolation and replaced or extended without affecting unrelated modules. This documentation is
 * generated to the project standard: it states the contract of the type, the layer it belongs to and
 * the expectations that callers may rely on. Personal data is never logged by this component, and any
 * value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public class BusinessRuleException extends RuntimeException {

    private final String code;

    /**
     * Builds an instance of {@code BusinessRuleException} from the given arguments. Every required value
     * is assigned exactly once, and optional values keep their documented defaults so partial construction
     * cannot leave the object in an ambiguous state.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * @param code textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param message textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public BusinessRuleException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Returns the code currently held by this BusinessRuleException instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getCode() {
        return code;
    }
}
