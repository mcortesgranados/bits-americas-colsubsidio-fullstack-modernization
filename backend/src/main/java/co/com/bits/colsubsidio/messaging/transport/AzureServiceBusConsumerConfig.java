package co.com.bits.colsubsidio.messaging.transport;

import co.com.bits.colsubsidio.config.MessagingProperties;
import co.com.bits.colsubsidio.messaging.application.MessageProcessingService;
import co.com.bits.colsubsidio.messaging.domain.DomainEventEnvelope;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Service Bus consumer. Delivery is completed even when processing fails, because the application
 * already retried with backoff and stored the message in the dead-letter table; letting the broker
 * redeliver forever would turn a poison message into a loop. Represents the
 * AzureServiceBusConsumerConfig component of the transport module. It participates in the
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
@Configuration
@ConditionalOnProperty(name = "app.messaging.transport", havingValue = "azure")
public class AzureServiceBusConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureServiceBusConsumerConfig.class);

    /**
     * Declares the {@code serviceBusProcessorClient} bean used by the application context. Keeping the
     * construction in a dedicated configuration class makes the wiring explicit and allows tests to
     * replace the bean with a double when the real implementation would require external infrastructure.
     *
     * The bean is stateless and safe to share across threads; any mutable state belongs to the
     * collaborator it wraps or to the database.
     *
     * Configuration values are validated when the context starts, so a missing environment variable fails
     * fast instead of producing a runtime error during the first request.
     *
     * Tests can replace the bean with a double without touching the production wiring, which keeps the
     * integration suite focused on behaviour rather than configuration.
     *
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @param processingService collaborator injected through its port, which keeps the class independent
     * from the concrete implementation
     *
     * @param objectMapper Jackson mapper used to serialize or deserialize the message payload
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Bean(destroyMethod = "close")
    ServiceBusProcessorClient serviceBusProcessorClient(MessagingProperties properties,
                                                        MessageProcessingService processingService,
                                                        ObjectMapper objectMapper) {
        ServiceBusProcessorClient client = new ServiceBusClientBuilder()
                .connectionString(properties.azure().connectionString())
                .processor()
                .queueName(properties.azure().queueName())
                .maxConcurrentCalls(2)
                .processMessage(context -> {
                    try {
                        DomainEventEnvelope envelope = objectMapper.readValue(
                                context.getMessage().getBody().toString(), DomainEventEnvelope.class);
                        processingService.processBlocking(envelope);
                    } catch (Exception ex) {
                        log.error("servicebus.message.unreadable messageId={} reason={}",
                                context.getMessage().getMessageId(), ex.toString());
                    } finally {
                        context.complete();
                    }
                })
                .processError(context -> log.error("servicebus.processor.error entityPath={} reason={}",
                        context.getEntityPath(), context.getException().toString()))
                .buildProcessorClient();
        client.start();
        log.info("servicebus.processor.started queue={}", properties.azure().queueName());
        return client;
    }
}
