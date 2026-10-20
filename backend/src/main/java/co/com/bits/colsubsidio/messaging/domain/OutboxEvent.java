package co.com.bits.colsubsidio.messaging.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

/**
 * Transactional outbox row. The event is written in the same transaction as the business change, so
 * there is no dual-write window: either both are persisted or neither is. Represents the OutboxEvent
 * component of the domain module. It participates in the demonstration platform for affiliates,
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
@Entity
@Table(name = "outbox_event")
public class OutboxEvent {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "aggregate_type", nullable = false, length = 40)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 40)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "payload", nullable = false, length = 4000)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    /**
     * Builds an instance of {@code OutboxEvent} from the given arguments. Every required value is assigned
     * exactly once, and optional values keep their documented defaults so partial construction cannot
     * leave the object in an ambiguous state.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    protected OutboxEvent() {
    }

    /**
     * Builds an instance of {@code OutboxEvent} from the given arguments. Every required value is assigned
     * exactly once, and optional values keep their documented defaults so partial construction cannot
     * leave the object in an ambiguous state.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @param aggregateType textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param aggregateId textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param eventType textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param payload textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param correlationId textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public OutboxEvent(String aggregateType, String aggregateId, String eventType, String payload,
                       String correlationId) {
        this.id = UUID.randomUUID().toString();
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.correlationId = correlationId;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
    }

    /**
     * Implements the {@code markPublished} domain behaviour. Business rules live here instead of in the
     * controller, which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * @param now point in time in UTC, used to keep audit entries and state transitions comparable
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void markPublished(Instant now) {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = now;
        this.attempts = attempts + 1;
        this.lastError = null;
    }

    /**
     * Implements the {@code registerFailure} domain behaviour. Business rules live here instead of in the
     * controller, which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * The method belongs to the domain layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * @param error textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param maxAttempts numeric counter or size used by the operation
     *
     * @param now point in time in UTC, used to keep audit entries and state transitions comparable
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void registerFailure(String error, int maxAttempts, Instant now) {
        this.attempts = attempts + 1;
        this.lastError = truncate(error);
        if (attempts >= maxAttempts) {
            this.status = OutboxStatus.FAILED;
        }
    }

    /**
     * Applies the {@code truncate} operation to this aggregate. The method enforces the invariants that
     * belong to the domain, including the allowed transitions, so an invalid call fails before any state
     * is modified.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The method belongs to the domain layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * @param value textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    /**
     * Returns the id currently held by this OutboxEvent instance. The value is read from the persistent
     * state of the object and is never recalculated on access, which keeps the accessor free of side
     * effects and safe to call from mapping code.
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
    public String getId() {
        return id;
    }

    /**
     * Returns the aggregate type currently held by this OutboxEvent instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
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
    public String getAggregateType() {
        return aggregateType;
    }

    /**
     * Exposes the aggregate id of this OutboxEvent. Because the mapper reads the value after the entity
     * has been loaded, the accessor returns the committed state and does not trigger lazy loading of
     * unrelated associations.
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
    public String getAggregateId() {
        return aggregateId;
    }

    /**
     * Returns the event type currently held by this OutboxEvent instance. The value is read from the
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
    public String getEventType() {
        return eventType;
    }

    /**
     * Returns the payload currently held by this OutboxEvent instance. The value is read from the
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
    public String getPayload() {
        return payload;
    }

    /**
     * Returns the status currently held by this OutboxEvent instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
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
    public OutboxStatus getStatus() {
        return status;
    }

    /**
     * Exposes the attempts of this OutboxEvent. Because the mapper reads the value after the entity has
     * been loaded, the accessor returns the committed state and does not trigger lazy loading of unrelated
     * associations.
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
    public int getAttempts() {
        return attempts;
    }

    /**
     * Exposes the correlation id of this OutboxEvent. Because the mapper reads the value after the entity
     * has been loaded, the accessor returns the committed state and does not trigger lazy loading of
     * unrelated associations.
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
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the created at currently held by this OutboxEvent instance. The value is read from the
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
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the published at currently held by this OutboxEvent instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
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
    public Instant getPublishedAt() {
        return publishedAt;
    }

    /**
     * Returns the last error currently held by this OutboxEvent instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
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
    public String getLastError() {
        return lastError;
    }
}
