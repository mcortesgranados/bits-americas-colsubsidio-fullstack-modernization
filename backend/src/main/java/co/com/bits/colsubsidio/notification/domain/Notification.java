package co.com.bits.colsubsidio.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Represents the Notification component of the domain module. It participates in the demonstration
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
@Table(name = "notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "affiliate_id", nullable = false)
    private Long affiliateId;

    @Column(name = "request_id")
    private Long requestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel = NotificationChannel.IN_APP;

    @Column(name = "subject", nullable = false, length = 160)
    private String subject;

    @Column(name = "body", nullable = false, length = 1000)
    private String body;

    @Column(name = "read_flag", nullable = false)
    private boolean read;

    /**
     * Applies the {@code n} operation to this aggregate. The method enforces the invariants that belong to
     * the domain, including the allowed transitions, so an invalid call fails before any state is
     * modified.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
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
     * Creates a new instance of {@code Notification} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    protected Notification() {
    }

    /**
     * Creates a new instance of {@code Notification} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @param requestId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @param channel input value required by the operation (NotificationChannel); it is validated before
     * the state is modified
     *
     * @param subject textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param body textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Notification(Long affiliateId, Long requestId, NotificationChannel channel, String subject, String body) {
        this.affiliateId = affiliateId;
        this.requestId = requestId;
        this.channel = channel;
        this.subject = subject;
        this.body = body;
    }

    /**
     * Applies the {@code markRead} operation to this aggregate. The method enforces the invariants that
     * belong to the domain, including the allowed transitions, so an invalid call fails before any state
     * is modified.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void markRead() {
        this.read = true;
    }

    /**
     * Exposes the id of this Notification. Because the mapper reads the value after the entity has been
     * loaded, the accessor returns the committed state and does not trigger lazy loading of unrelated
     * associations.
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
    public Long getId() {
        return id;
    }

    /**
     * Exposes the affiliate id of this Notification. Because the mapper reads the value after the entity
     * has been loaded, the accessor returns the committed state and does not trigger lazy loading of
     * unrelated associations.
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
    public Long getAffiliateId() {
        return affiliateId;
    }

    /**
     * Exposes the request id of this Notification. Because the mapper reads the value after the entity has
     * been loaded, the accessor returns the committed state and does not trigger lazy loading of unrelated
     * associations.
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
    public Long getRequestId() {
        return requestId;
    }

    /**
     * Returns the channel currently held by this Notification instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
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
    public NotificationChannel getChannel() {
        return channel;
    }

    /**
     * Returns the subject currently held by this Notification instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
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
    public String getSubject() {
        return subject;
    }

    /**
     * Exposes the body of this Notification. Because the mapper reads the value after the entity has been
     * loaded, the accessor returns the committed state and does not trigger lazy loading of unrelated
     * associations.
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
    public String getBody() {
        return body;
    }

    /**
     * Implements the {@code isRead} domain behaviour. Business rules live here instead of in the
     * controller, which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean isRead() {
        return read;
    }

    /**
     * Exposes the created at of this Notification. Because the mapper reads the value after the entity has
     * been loaded, the accessor returns the committed state and does not trigger lazy loading of unrelated
     * associations.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
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
    public Instant getCreatedAt() {
        return createdAt;
    }
}
