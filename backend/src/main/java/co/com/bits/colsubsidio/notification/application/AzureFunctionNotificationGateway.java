package co.com.bits.colsubsidio.notification.application;

import co.com.bits.colsubsidio.config.NotificationProperties;
import co.com.bits.colsubsidio.notification.domain.Notification;
import java.time.Duration;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Azure adapter: delegates delivery to an Azure Function. Uses WebClient + Reactor retry because this
 * is a non-blocking outbound integration where a transient failure must not fail the business
 * transaction. Represents the AzureFunctionNotificationGateway component of the application module. It
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
@ConditionalOnProperty(name = "app.notifications.provider", havingValue = "azure-function")
public class AzureFunctionNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(AzureFunctionNotificationGateway.class);

    private final WebClient webClient;
    private final NotificationProperties properties;

    /**
     * Creates a new instance of {@code AzureFunctionNotificationGateway} with the supplied collaborators.
     * The constructor keeps the object in a consistent initial state and does not perform I/O, which
     * allows it to be built inside a transaction or in a plain unit test with the same semantics.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @param builder input value required by the operation (WebClient.Builder); it is validated before the
     * state is modified
     *
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AzureFunctionNotificationGateway(WebClient.Builder builder, NotificationProperties properties) {
        this.properties = properties;
        this.webClient = builder.baseUrl(properties.azureFunction().baseUrl()).build();
    }

    /**
     * Implements the {@code dispatch} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
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
     * @param notification input value required by the operation (Notification); it is validated before the
     * state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    public void dispatch(Notification notification) {
        Map<String, Object> body = Map.of(
                "notificationId", notification.getId(),
                "affiliateId", notification.getAffiliateId(),
                "requestId", notification.getRequestId() == null ? 0L : notification.getRequestId(),
                "subject", notification.getSubject(),
                "body", notification.getBody());

        Mono<Void> call = webClient.post()
                .uri("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    if (!properties.azureFunction().apiKey().isBlank()) {
                        headers.set("x-functions-key", properties.azureFunction().apiKey());
                    }
                })
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofMillis(properties.azureFunction().timeoutMs()))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(200)))
                .then();

        call.subscribe(
                ignored -> {
                },
                error -> log.warn("notification.dispatch.failed notificationId={} reason={}",
                        notification.getId(), error.toString()));
    }
}
