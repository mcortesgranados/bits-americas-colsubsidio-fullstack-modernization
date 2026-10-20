package co.com.bits.colsubsidio.audit.domain;

/**
 * Stable action codes used in the audit trail and in operational dashboards. Represents the
 * AuditActions component of the domain module. It participates in the demonstration platform for
 * affiliates, companies and subsidy requests, where the behaviour is driven by explicit business rules
 * rather than by implicit framework magic. The type keeps a single responsibility so that it can be
 * tested in isolation and replaced or extended without affecting unrelated modules. This documentation
 * is generated to the project standard: it states the contract of the type, the layer it belongs to
 * and the expectations that callers may rely on. Personal data is never logged by this component, and
 * any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public final class AuditActions {

    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILURE = "LOGIN_FAILURE";
    public static final String PII_ACCESS = "PII_ACCESS";
    public static final String AFFILIATE_CONTACT_UPDATED = "AFFILIATE_CONTACT_UPDATED";
    public static final String SUBSIDY_REQUEST_CREATED = "SUBSIDY_REQUEST_CREATED";
    public static final String SUBSIDY_REQUEST_STATUS_CHANGED = "SUBSIDY_REQUEST_STATUS_CHANGED";
    public static final String SUBSIDY_DOCUMENT_UPLOADED = "SUBSIDY_DOCUMENT_UPLOADED";
    public static final String SUBSIDY_REQUEST_REVIEWED = "SUBSIDY_REQUEST_REVIEWED";
    public static final String WORKER_CREATED = "WORKER_CREATED";
    public static final String WORKER_UPDATED = "WORKER_UPDATED";

    /**
     * Creates a new instance of {@code AuditActions} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private AuditActions() {
    }
}
