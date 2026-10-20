package co.com.bits.colsubsidio.messaging.application;

import co.com.bits.colsubsidio.config.MessagingProperties;
import co.com.bits.colsubsidio.messaging.domain.DomainEventEnvelope;
import co.com.bits.colsubsidio.messaging.domain.ProcessedMessage;
import co.com.bits.colsubsidio.messaging.domain.ProcessedMessageRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * One transaction per delivery attempt: idempotency check, domain handling, and the processed-message
 * marker commit together. If the handler fails, both the business change and the marker roll back, so
 * the retry is safe. Represents the TransactionalMessageExecutor component of the application module.
 * It participates in the demonstration platform for affiliates, companies and subsidy requests, where
 * the behaviour is driven by explicit business rules rather than by implicit framework magic. The type
 * keeps a single responsibility so that it can be tested in isolation and replaced or extended without
 * affecting unrelated modules. This documentation is generated to the project standard: it states the
 * contract of the type, the layer it belongs to and the expectations that callers may rely on.
 * Personal data is never logged by this component, and any value returned to a client is masked by the
 * DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Service
public class TransactionalMessageExecutor {

    private static final Logger log = LoggerFactory.getLogger(TransactionalMessageExecutor.class);

    private final ProcessedMessageRepository processedMessages;
    private final SubsidyMessageHandler handler;
    private final MessagingProperties properties;

    /**
     * Builds an instance of {@code TransactionalMessageExecutor} from the given arguments. Every required
     * value is assigned exactly once, and optional values keep their documented defaults so partial
     * construction cannot leave the object in an ambiguous state.
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
     * @param processedMessages repository used to read or persist the aggregate owned by this module
     *
     * @param handler input value required by the operation (SubsidyMessageHandler); it is validated before
     * the state is modified
     *
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public TransactionalMessageExecutor(ProcessedMessageRepository processedMessages, SubsidyMessageHandler handler,
                                        MessagingProperties properties) {
        this.processedMessages = processedMessages;
        this.handler = handler;
        this.properties = properties;
    }

    /**
     * Performs the {@code processOnce} operation described by its signature. The method keeps a single
     * responsibility and relies on the surrounding layer for validation, transaction management and error
     * translation.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param envelope input value required by the operation (DomainEventEnvelope); it is validated before
     * the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional
    public void processOnce(DomainEventEnvelope envelope) {
        String consumer = properties.consumer().name();
        if (processedMessages.existsByMessageIdAndConsumer(envelope.messageId(), consumer)) {
            log.info("message.duplicate.ignored messageId={} consumer={}", envelope.messageId(), consumer);
            return;
        }
        handler.handle(envelope);
        processedMessages.save(new ProcessedMessage(envelope.messageId(), consumer, "PROCESSED", Instant.now()));
    }
}
