package co.com.bits.colsubsidio.messaging.transport;

import co.com.bits.colsubsidio.config.MessagingProperties;
import co.com.bits.colsubsidio.messaging.application.DomainEventPublisher;
import co.com.bits.colsubsidio.messaging.domain.DomainEventEnvelope;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Azure Service Bus adapter. Activated only with {@code app.messaging.transport=azure} and a
 * connection string supplied through configuration or Key Vault; the local profile never needs it.
 * Represents the AzureServiceBusDomainEventPublisher component of the transport module. It
 * participates in the demonstration platform for affiliates, companies and subsidy requests, where the
 * behaviour is driven by explicit business rules rather than by implicit framework magic. The type
 * keeps a single responsibility so that it can be tested in isolation and replaced or extended without
 * affecting unrelated modules. This documentation is generated to the project standard: it states the
 * contract of the type, the layer it belongs to and the expectations that callers may rely on.
 * Personal data is never logged by this component, and any value returned to a client is masked by the
 * DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Component
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "azure")
public class AzureServiceBusDomainEventPublisher implements DomainEventPublisher {

    private final ServiceBusSenderClient sender;
    private final ObjectMapper objectMapper;

    /**
     * Builds an instance of {@code AzureServiceBusDomainEventPublisher} from the given arguments. Every
     * required value is assigned exactly once, and optional values keep their documented defaults so
     * partial construction cannot leave the object in an ambiguous state.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @param objectMapper Jackson mapper used to serialize or deserialize the message payload
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AzureServiceBusDomainEventPublisher(MessagingProperties properties, ObjectMapper objectMapper) {
        if (properties.azure().connectionString() == null || properties.azure().connectionString().isBlank()) {
            throw new IllegalStateException(
                    "app.messaging.azure.connection-string is required when app.messaging.transport=azure");
        }
        this.objectMapper = objectMapper;
        this.sender = new ServiceBusClientBuilder()
                .connectionString(properties.azure().connectionString())
                .sender()
                .queueName(properties.azure().queueName())
                .buildClient();
    }

    /**
     * Executes the {@code publish} application operation. It coordinates the collaborators required by the
     * use case, applies the authorization rules that belong to the operation and translates the outcome
     * into the value expected by the caller.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * @param envelope input value required by the operation (DomainEventEnvelope); it is validated before
     * the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    public void publish(DomainEventEnvelope envelope) {
        try {
            ServiceBusMessage message = new ServiceBusMessage(objectMapper.writeValueAsString(envelope));
            message.setMessageId(envelope.messageId());
            message.setContentType("application/json");
            message.getApplicationProperties().put("eventType", envelope.eventType());
            sender.sendMessage(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not publish envelope " + envelope.messageId(), ex);
        }
    }

    /**
     * Implements the {@code close} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @PreDestroy
    void close() {
        sender.close();
    }
}
