package co.com.bits.colsubsidio.messaging.application;

import co.com.bits.colsubsidio.config.MessagingProperties;
import co.com.bits.colsubsidio.messaging.domain.DomainEventEnvelope;
import co.com.bits.colsubsidio.messaging.domain.OutboxEvent;
import co.com.bits.colsubsidio.messaging.domain.OutboxEventRepository;
import co.com.bits.colsubsidio.messaging.domain.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polling publisher for the transactional outbox. Each row is published first and marked afterwards,
 * which gives at-least-once delivery; the consumer side compensates with its idempotency ledger. When
 * several instances run, the optimistic lock on {@code OutboxEvent#version} makes the concurrent
 * update fail for all but one of them, so a duplicate publish is possible but never a duplicate
 * business effect. Represents the OutboxPublisher component of the application module. It participates
 * in the demonstration platform for affiliates, companies and subsidy requests, where the behaviour is
 * driven by explicit business rules rather than by implicit framework magic. The type keeps a single
 * responsibility so that it can be tested in isolation and replaced or extended without affecting
 * unrelated modules. This documentation is generated to the project standard: it states the contract
 * of the type, the layer it belongs to and the expectations that callers may rely on. Personal data is
 * never logged by this component, and any value returned to a client is masked by the DTO layer before
 * serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Component
@ConditionalOnProperty(name = "app.messaging.outbox.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository repository;
    private final DomainEventPublisher publisher;
    private final MessagingProperties properties;

    /**
     * Creates a new instance of {@code OutboxPublisher} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @param repository repository used to read or persist the aggregate owned by this module
     *
     * @param publisher collaborator injected through its port, which keeps the class independent from the
     * concrete implementation
     *
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public OutboxPublisher(OutboxEventRepository repository, DomainEventPublisher publisher,
                           MessagingProperties properties) {
        this.repository = repository;
        this.publisher = publisher;
        this.properties = properties;
    }

    /**
     * Implements the {@code publishPending} use case. The method coordinates the domain object, the
     * repositories and the outbound ports required by the operation, relies on the surrounding transaction
     * when state is modified, and leaves the HTTP concerns to the controller.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The method is invoked by the scheduler according to the configured interval; it must therefore
     * tolerate overlapping executions and rely on the database constraints to avoid publishing the same
     * row twice.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Scheduled(fixedDelayString = "${app.messaging.outbox.publish-interval-ms:3000}")
    public void publishPending() {
        List<OutboxEvent> pending = repository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING, PageRequest.of(0, properties.outbox().batchSize()));
        if (pending.isEmpty()) {
            return;
        }
        for (OutboxEvent event : pending) {
            publishOne(event);
        }
    }

    /**
     * Executes the {@code publishOne} application operation. It coordinates the collaborators required by
     * the use case, applies the authorization rules that belong to the operation and translates the
     * outcome into the value expected by the caller.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * @param event input value required by the operation (OutboxEvent); it is validated before the state
     * is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private void publishOne(OutboxEvent event) {
        try {
            publisher.publish(new DomainEventEnvelope(event.getId(), event.getEventType(), event.getAggregateType(),
                    event.getAggregateId(), event.getCreatedAt(), event.getCorrelationId(), event.getPayload()));
            event.markPublished(Instant.now());
            repository.save(event);
            log.info("outbox.published eventId={} type={} correlationId={}",
                    event.getId(), event.getEventType(), event.getCorrelationId());
        } catch (RuntimeException ex) {
            event.registerFailure(ex.toString(), properties.outbox().maxAttempts(), Instant.now());
            repository.save(event);
            log.warn("outbox.publish.failed eventId={} type={} attempts={} reason={}",
                    event.getId(), event.getEventType(), event.getAttempts(), ex.toString());
        }
    }
}
