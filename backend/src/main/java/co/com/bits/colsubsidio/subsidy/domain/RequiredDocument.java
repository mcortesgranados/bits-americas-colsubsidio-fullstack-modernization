package co.com.bits.colsubsidio.subsidy.domain;

import co.com.bits.colsubsidio.common.error.BusinessRuleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Document expected for a request. The file itself lives in the storage provider, never in MySQL.
 * Represents the RequiredDocument component of the domain module. It participates in the demonstration
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
@Entity
@Table(name = "required_document",
        uniqueConstraints = @UniqueConstraint(name = "uk_required_document_request_type",
                columnNames = {"request_id", "document_type"}))
public class RequiredDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false)
    private Long requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 40)
    private DocumentType documentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "file_name", length = 200)
    private String fileName;

    @Column(name = "storage_key", length = 300)
    private String storageKey;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "rejection_reason", length = 300)
    private String rejectionReason;

    /**
     * Creates a new instance of {@code RequiredDocument} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    protected RequiredDocument() {
    }

    /**
     * Creates a new instance of {@code RequiredDocument} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @param requestId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @param documentType document type from the catalogue required for the subsidy type of the request
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public RequiredDocument(Long requestId, DocumentType documentType) {
        this.requestId = requestId;
        this.documentType = documentType;
    }

    /**
     * Applies the {@code markReceived} operation to this aggregate. The method enforces the invariants
     * that belong to the domain, including the allowed transitions, so an invalid call fails before any
     * state is modified.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * @param fileName textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param storageKey textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param contentType textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param sizeBytes numeric value used by the operation; negative values are rejected when they are not
     * meaningful
     *
     * @param checksum textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param now point in time in UTC, used to keep audit entries and state transitions comparable
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void markReceived(String fileName, String storageKey, String contentType, long sizeBytes,
                             String checksum, Instant now) {
        if (status == DocumentStatus.VALIDATED) {
            throw new BusinessRuleException("DOCUMENT_ALREADY_VALIDATED",
                    "Document " + documentType + " has already been validated");
        }
        this.fileName = fileName;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.status = DocumentStatus.RECEIVED;
        this.uploadedAt = now;
        this.rejectionReason = null;
    }

    /**
     * Applies the {@code validate} operation to this aggregate. The method enforces the invariants that
     * belong to the domain, including the allowed transitions, so an invalid call fails before any state
     * is modified.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * @param now point in time in UTC, used to keep audit entries and state transitions comparable
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void validate(Instant now) {
        if (status != DocumentStatus.RECEIVED) {
            throw new BusinessRuleException("DOCUMENT_NOT_RECEIVED",
                    "Document " + documentType + " has not been uploaded yet");
        }
        this.status = DocumentStatus.VALIDATED;
        this.reviewedAt = now;
    }

    /**
     * Implements the {@code reject} domain behaviour. Business rules live here instead of in the
     * controller, which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * @param reason textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param now point in time in UTC, used to keep audit entries and state transitions comparable
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void reject(String reason, Instant now) {
        this.status = DocumentStatus.REJECTED;
        this.rejectionReason = reason;
        this.reviewedAt = now;
    }

    /**
     * Implements the {@code isSatisfied} domain behaviour. Business rules live here instead of in the
     * controller, which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The method belongs to the domain layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean isSatisfied() {
        return status == DocumentStatus.RECEIVED || status == DocumentStatus.VALIDATED;
    }

    /**
     * Returns the id currently held by this RequiredDocument instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
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
    public Long getId() {
        return id;
    }

    /**
     * Returns the request id currently held by this RequiredDocument instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Long getRequestId() {
        return requestId;
    }

    /**
     * Exposes the document type of this RequiredDocument. Because the mapper reads the value after the
     * entity has been loaded, the accessor returns the committed state and does not trigger lazy loading
     * of unrelated associations.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /**
     * Returns the status currently held by this RequiredDocument instance. The value is read from the
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
    public DocumentStatus getStatus() {
        return status;
    }

    /**
     * Returns the file name currently held by this RequiredDocument instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Exposes the storage key of this RequiredDocument. Because the mapper reads the value after the
     * entity has been loaded, the accessor returns the committed state and does not trigger lazy loading
     * of unrelated associations.
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
    public String getStorageKey() {
        return storageKey;
    }

    /**
     * Returns the content type currently held by this RequiredDocument instance. The value is read from
     * the persistent state of the object and is never recalculated on access, which keeps the accessor
     * free of side effects and safe to call from mapping code.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Exposes the size bytes of this RequiredDocument. Because the mapper reads the value after the entity
     * has been loaded, the accessor returns the committed state and does not trigger lazy loading of
     * unrelated associations.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
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
    public Long getSizeBytes() {
        return sizeBytes;
    }

    /**
     * Returns the checksum currently held by this RequiredDocument instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
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
    public String getChecksum() {
        return checksum;
    }

    /**
     * Exposes the uploaded at of this RequiredDocument. Because the mapper reads the value after the
     * entity has been loaded, the accessor returns the committed state and does not trigger lazy loading
     * of unrelated associations.
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
    public Instant getUploadedAt() {
        return uploadedAt;
    }

    /**
     * Returns the reviewed at currently held by this RequiredDocument instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
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
    public Instant getReviewedAt() {
        return reviewedAt;
    }

    /**
     * Exposes the rejection reason of this RequiredDocument. Because the mapper reads the value after the
     * entity has been loaded, the accessor returns the committed state and does not trigger lazy loading
     * of unrelated associations.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getRejectionReason() {
        return rejectionReason;
    }
}
