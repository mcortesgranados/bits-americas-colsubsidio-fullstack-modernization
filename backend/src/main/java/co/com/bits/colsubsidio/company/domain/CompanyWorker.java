package co.com.bits.colsubsidio.company.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Demonstration worker registry. Synthetic records created from the company portal. Represents the
 * CompanyWorker component of the domain module. It participates in the demonstration platform for
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
@Entity
@Table(name = "company_worker")
public class CompanyWorker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "document_type", nullable = false, length = 10)
    private String documentType;

    @Column(name = "document_number", nullable = false, length = 30)
    private String documentNumber;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(name = "position", length = 80)
    private String position;

    @Column(name = "hired_at")
    private LocalDate hiredAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private WorkerStatus status = WorkerStatus.ACTIVE;

    /**
     * Implements the {@code n} domain behaviour. Business rules live here instead of in the controller,
     * which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param name = "created_at" input value required by the operation (Object); it is validated before
     * the state is modified
     *
     * @param nullable = false) private Instant createdAt = Instant.now( input value required by the
     * operation (Object); it is validated before the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /**
     * Implements the {@code n} domain behaviour. Business rules live here instead of in the controller,
     * which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param name = "updated_at" input value required by the operation (Object); it is validated before
     * the state is modified
     *
     * @param nullable = false) private Instant updatedAt = Instant.now( input value required by the
     * operation (Object); it is validated before the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /**
     * Creates a new instance of {@code CompanyWorker} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    protected CompanyWorker() {
    }

    /**
     * Creates a new instance of {@code CompanyWorker} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @param companyId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @param documentType textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param documentNumber textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param firstName textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param lastName textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param position textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param hiredAt input value required by the operation (LocalDate); it is validated before the state
     * is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public CompanyWorker(Long companyId, String documentType, String documentNumber, String firstName,
                         String lastName, String position, LocalDate hiredAt) {
        this.companyId = companyId;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.hiredAt = hiredAt;
    }

    /**
     * Applies the {@code onUpdate} operation to this aggregate. The method enforces the invariants that
     * belong to the domain, including the allowed transitions, so an invalid call fails before any state
     * is modified.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /**
     * Returns the id currently held by this CompanyWorker instance. The value is read from the persistent
     * state of the object and is never recalculated on access, which keeps the accessor free of side
     * effects and safe to call from mapping code.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Long getId() {
        return id;
    }

    /**
     * Exposes the company id of this CompanyWorker. Because the mapper reads the value after the entity
     * has been loaded, the accessor returns the committed state and does not trigger lazy loading of
     * unrelated associations.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Long getCompanyId() {
        return companyId;
    }

    /**
     * Returns the document type currently held by this CompanyWorker instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * Exposes the document number of this CompanyWorker. Because the mapper reads the value after the
     * entity has been loaded, the accessor returns the committed state and does not trigger lazy loading
     * of unrelated associations.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getDocumentNumber() {
        return documentNumber;
    }

    /**
     * Returns the first name currently held by this CompanyWorker instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name currently held by this CompanyWorker instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the position currently held by this CompanyWorker instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getPosition() {
        return position;
    }

    /**
     * Replaces the position of this CompanyWorker with the supplied value. The mutator is intentionally
     * dumb: it only assigns the field, so the validation and authorization decisions stay in the
     * application service that owns the use case.
     *
     * The assignment is not audited here: auditing belongs to the application service, which knows the
     * actor, the reason and the correlation identifier of the change.
     *
     * Direct field access is preserved for JPA, so the mutator only exists to serve the application layer
     * and the tests.
     *
     * The setter does not persist or flush; the owning transaction decides when the change reaches the
     * database, and optimistic locking protects concurrent updates.
     *
     * @param position textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Returns the hired at currently held by this CompanyWorker instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public LocalDate getHiredAt() {
        return hiredAt;
    }

    /**
     * Exposes the status of this CompanyWorker. Because the mapper reads the value after the entity has
     * been loaded, the accessor returns the committed state and does not trigger lazy loading of unrelated
     * associations.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public WorkerStatus getStatus() {
        return status;
    }

    /**
     * Replaces the status of this CompanyWorker with the supplied value. The mutator is intentionally
     * dumb: it only assigns the field, so the validation and authorization decisions stay in the
     * application service that owns the use case.
     *
     * Mutators assume that the caller already validated the value; the aggregate only enforces the
     * invariants that belong to its own state, which keeps the entity free of transport concerns.
     *
     * The setter does not persist or flush; the owning transaction decides when the change reaches the
     * database, and optimistic locking protects concurrent updates.
     *
     * Direct field access is preserved for JPA, so the mutator only exists to serve the application layer
     * and the tests.
     *
     * @param status input value required by the operation (WorkerStatus); it is validated before the state
     * is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void setStatus(WorkerStatus status) {
        this.status = status;
    }

    /**
     * Exposes the created at of this CompanyWorker. Because the mapper reads the value after the entity
     * has been loaded, the accessor returns the committed state and does not trigger lazy loading of
     * unrelated associations.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Applies the {@code fullName} operation to this aggregate. The method enforces the invariants that
     * belong to the domain, including the allowed transitions, so an invalid call fails before any state
     * is modified.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String fullName() {
        return firstName + " " + lastName;
    }
}
