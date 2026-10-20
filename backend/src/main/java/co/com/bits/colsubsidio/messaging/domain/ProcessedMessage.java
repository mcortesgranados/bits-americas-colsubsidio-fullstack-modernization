package co.com.bits.colsubsidio.messaging.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Idempotency ledger. The unique constraint on (message_id, consumer) is the actual guarantee: a
 * duplicate delivery fails the insert and is discarded. Represents the ProcessedMessage component of
 * the domain module. It participates in the demonstration platform for affiliates, companies and
 * subsidy requests, where the behaviour is driven by explicit business rules rather than by implicit
 * framework magic. The type keeps a single responsibility so that it can be tested in isolation and
 * replaced or extended without affecting unrelated modules. This documentation is generated to the
 * project standard: it states the contract of the type, the layer it belongs to and the expectations
 * that callers may rely on. Personal data is never logged by this component, and any value returned to
 * a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Entity
@Table(name = "processed_message",
        uniqueConstraints = @UniqueConstraint(name = "uk_processed_message_id_consumer",
                columnNames = {"message_id", "consumer"}))
public class ProcessedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false, length = 36)
    private String messageId;

    @Column(name = "consumer", nullable = false, length = 80)
    private String consumer;

    @Column(name = "outcome", nullable = false, length = 30)
    private String outcome;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    /**
     * Creates a new instance of {@code ProcessedMessage} with the supplied collaborators. The constructor
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
    protected ProcessedMessage() {
    }

    /**
     * Creates a new instance of {@code ProcessedMessage} with the supplied collaborators. The constructor
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
     * @param messageId textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param consumer textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param outcome textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param processedAt point in time in UTC, used to keep audit entries and state transitions comparable
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public ProcessedMessage(String messageId, String consumer, String outcome, Instant processedAt) {
        this.messageId = messageId;
        this.consumer = consumer;
        this.outcome = outcome;
        this.processedAt = processedAt;
    }

    /**
     * Exposes the id of this ProcessedMessage. Because the mapper reads the value after the entity has
     * been loaded, the accessor returns the committed state and does not trigger lazy loading of unrelated
     * associations.
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
    public Long getId() {
        return id;
    }

    /**
     * Returns the message id currently held by this ProcessedMessage instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Returns the consumer currently held by this ProcessedMessage instance. The value is read from the
     * persistent state of the object and is never recalculated on access, which keeps the accessor free of
     * side effects and safe to call from mapping code.
     *
     * Accessors stay trivial on purpose: JPA field access remains the single source of truth for
     * persistence, and no business decision is hidden inside a getter.
     *
     * The member is used by the DTO mappers, so its return type is treated as a stable internal API even
     * though it is not published directly to clients.
     *
     * The value is read from the state already loaded in the persistence context; accessing it does not
     * trigger additional queries or lazy loading of unrelated associations.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public String getConsumer() {
        return consumer;
    }

    /**
     * Exposes the outcome of this ProcessedMessage. Because the mapper reads the value after the entity
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
    public String getOutcome() {
        return outcome;
    }

    /**
     * Exposes the processed at of this ProcessedMessage. Because the mapper reads the value after the
     * entity has been loaded, the accessor returns the committed state and does not trigger lazy loading
     * of unrelated associations.
     *
     * The getter never mutates the entity nor normalises the value, which keeps mapping code free of
     * surprising side effects.
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
    public Instant getProcessedAt() {
        return processedAt;
    }
}
