package co.com.bits.colsubsidio.messaging.transport;

import co.com.bits.colsubsidio.config.MessagingProperties;
import co.com.bits.colsubsidio.messaging.application.DomainEventPublisher;
import co.com.bits.colsubsidio.messaging.application.MessageProcessingService;
import co.com.bits.colsubsidio.messaging.domain.DomainEventEnvelope;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local transport: delivers the envelope to the same consumer pipeline used by Azure, either
 * asynchronously on a bounded-elastic scheduler (default) or synchronously for deterministic tests.
 * This keeps the demo runnable with no cloud subscription without inventing a different code path.
 * Represents the LocalDomainEventPublisher component of the transport module. It participates in the
 * demonstration platform for affiliates, companies and subsidy requests, where the behaviour is driven
 * by explicit business rules rather than by implicit framework magic. The type keeps a single
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
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "local", matchIfMissing = true)
public class LocalDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalDomainEventPublisher.class);

    private final MessageProcessingService processingService;
    private final MessagingProperties properties;

    /**
     * Builds an instance of {@code LocalDomainEventPublisher} from the given arguments. Every required
     * value is assigned exactly once, and optional values keep their documented defaults so partial
     * construction cannot leave the object in an ambiguous state.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @param processingService collaborator injected through its port, which keeps the class independent
     * from the concrete implementation
     *
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public LocalDomainEventPublisher(MessageProcessingService processingService, MessagingProperties properties) {
        this.processingService = processingService;
        this.properties = properties;
    }

    /**
     * Executes the {@code publish} application operation. It coordinates the collaborators required by the
     * use case, applies the authorization rules that belong to the operation and translates the outcome
     * into the value expected by the caller.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * The method belongs to the transport layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param envelope input value required by the operation (DomainEventEnvelope); it is validated before
     * the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    public void publish(DomainEventEnvelope envelope) {
        var pipeline = processingService.process(envelope);
        if (properties.local().simulatedLatencyMs() > 0) {
            pipeline = pipeline.delaySubscription(Duration.ofMillis(properties.local().simulatedLatencyMs()));
        }
        if (properties.local().async()) {
            pipeline.subscribe(
                    ignored -> {
                    },
                    error -> log.error("local.transport.delivery.failed messageId={} reason={}",
                            envelope.messageId(), error.toString()));
        } else {
            pipeline.block(Duration.ofSeconds(30));
        }
    }
}
