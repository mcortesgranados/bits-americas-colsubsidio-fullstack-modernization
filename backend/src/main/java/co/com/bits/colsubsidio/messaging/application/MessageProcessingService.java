package co.com.bits.colsubsidio.messaging.application;

import co.com.bits.colsubsidio.config.MessagingProperties;
import co.com.bits.colsubsidio.messaging.domain.DomainEventEnvelope;
import co.com.bits.colsubsidio.messaging.domain.FailedMessage;
import co.com.bits.colsubsidio.messaging.domain.FailedMessageRepository;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

/**
 * Reactive processing pipeline shared by every transport.
 *
 * <p>The pipeline executes the blocking JPA work on the bounded-elastic scheduler, retries with
 * exponential backoff and, once the attempts are exhausted, writes a dead-letter record. The transport
 * adapter (in-process or Service Bus) only has to call {@link #process(DomainEventEnvelope)} and does
 * not need to know how failures are handled, which keeps the idempotency and the retry policy in a
 * single place.
 *
 * <p>Deliveries are at-least-once: the same envelope may arrive twice, and the transactional executor
 * is the component that guarantees a single effect through its idempotency ledger.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Service
public class MessageProcessingService {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessingService.class);
    private static final int MAX_ERROR_LENGTH = 1000;

    private final TransactionalMessageExecutor executor;
    private final FailedMessageRepository failedMessages;
    private final MessagingProperties properties;

    /**
     * Creates the processing service with its three collaborators.
     *
     * <p>The executor owns the transaction boundary, the repository persists dead letters in its own
     * transaction and the properties supply the retry policy, so the service itself remains stateless
     * and thread-safe. All three are mandatory: a missing bean must fail the context at startup rather
     * than producing a null pointer while a message is being consumed, because a failure in the
     * consumer is much harder to diagnose than a failure during bootstrap.
     *
     * @param executor       transactional component that performs the idempotent unit of work
     * @param failedMessages repository used to record messages whose retries were exhausted
     * @param properties     messaging configuration, including the number of attempts and the backoff
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public MessageProcessingService(TransactionalMessageExecutor executor,
                                    FailedMessageRepository failedMessages,
                                    MessagingProperties properties) {
        this.executor = executor;
        this.failedMessages = failedMessages;
        this.properties = properties;
    }

    /**
     * Processes one envelope reactively, with retries and dead-lettering.
     *
     * <p>The blocking unit of work runs on {@link Schedulers#boundedElastic()} because the consumer
     * uses JPA and must not occupy the event loop. The number of retries is derived from the configured
     * maximum attempts; when it is greater than zero the pipeline applies an exponential backoff
     * capped at five seconds and logs every retry with the event type, the message identifier and the
     * failure. When the attempts are exhausted, the error is converted into a dead-letter row and the
     * returned {@link Mono} completes empty, so the transport can acknowledge the message instead of
     * redelivering a poison payload forever.
     *
     * @param envelope message to process, including its payload and correlation identifier
     * @return a {@link Mono} that completes when the message was processed or dead-lettered; it never
     *         emits a value and never signals the original error to the caller
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Mono<Void> process(DomainEventEnvelope envelope) {
        int retries = Math.max(0, properties.consumer().maxAttempts() - 1);
        Mono<Void> pipeline = Mono.fromRunnable(() -> executor.processOnce(envelope))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
        if (retries > 0) {
            pipeline = pipeline.retryWhen(Retry
                    .backoff(retries, Duration.ofMillis(Math.max(10, properties.consumer().backoffMs())))
                    .maxBackoff(Duration.ofSeconds(5))
                    .doBeforeRetry(signal -> log.warn("message.retry eventType={} messageId={} attempt={} reason={}",
                            envelope.eventType(), envelope.messageId(), signal.totalRetries() + 1,
                            signal.failure().toString())));
        }
        return pipeline.onErrorResume(error -> {
            deadLetter(envelope, error);
            return Mono.empty();
        });
    }

    /**
     * Blocking variant for SDK-driven consumers that own their thread.
     *
     * <p>The Azure Service Bus processor invokes the handler on a thread it controls, so it needs a
     * blocking call to decide whether the message can be completed. The method reuses the same reactive
     * pipeline, which guarantees identical retry and dead-letter semantics in both transports, and
     * applies a thirty-second ceiling so a stuck dependency cannot block the processor indefinitely.
     *
     * @param envelope message to process, including its payload and correlation identifier
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void processBlocking(DomainEventEnvelope envelope) {
        process(envelope).block(Duration.ofSeconds(30));
    }

    /**
     * Persists a dead-letter record for a message whose retries were exhausted.
     *
     * <p>The record keeps the payload, the consumer name and the last error so that support can replay
     * or repair the message without querying the broker. Truncating the error avoids exceeding the
     * column length when a driver includes a long stack trace in the message. Failures while writing
     * the dead letter are logged and swallowed: throwing here would replace the original error with a
     * secondary one and would leave the transport without a chance to acknowledge the message.
     *
     * @param envelope message that could not be processed
     * @param error    last failure observed by the pipeline, used to fill the diagnostic column
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private void deadLetter(DomainEventEnvelope envelope, Throwable error) {
        try {
            failedMessages.save(new FailedMessage(envelope.messageId(), properties.consumer().name(),
                    envelope.eventType(), envelope.payload(), truncate(error.toString()),
                    properties.consumer().maxAttempts()));
            log.error("message.dead-lettered eventType={} messageId={} reason={}",
                    envelope.eventType(), envelope.messageId(), error.toString());
        } catch (RuntimeException ex) {
            log.error("message.dead-letter.failed messageId={} reason={}", envelope.messageId(), ex.toString());
        }
    }

    /**
     * Truncates a diagnostic message to the maximum length accepted by the dead-letter column.
     *
     * <p>The error column is sized for a short diagnosis, not for a full stack trace, so the method
     * keeps the first thousand characters and discards the rest. A null value cannot reach this point
     * because the caller passes {@code error.toString()}, but the length check also protects the
     * persistence layer from an oversized driver message that would otherwise fail the insert and hide
     * the original failure.
     *
     * @param value diagnostic text produced by the failure
     * @return the original text when it fits, or its first thousand characters
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private String truncate(String value) {
        return value.length() <= MAX_ERROR_LENGTH ? value : value.substring(0, MAX_ERROR_LENGTH);
    }
}
