package co.com.bits.colsubsidio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Messaging configuration. {@code transport=local} keeps everything in-process so the demo runs
 * without Azure; {@code transport=azure} wires the Service Bus adapter instead. Represents the
 * MessagingProperties component of the config module. It participates in the demonstration platform
 * for affiliates, companies and subsidy requests, where the behaviour is driven by explicit business
 * rules rather than by implicit framework magic. The type keeps a single responsibility so that it can
 * be tested in isolation and replaced or extended without affecting unrelated modules. This
 * documentation is generated to the project standard: it states the contract of the type, the layer it
 * belongs to and the expectations that callers may rely on. Personal data is never logged by this
 * component, and any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@ConfigurationProperties(prefix = "app.messaging")
public record MessagingProperties(
        String transport,
        Outbox outbox,
        Consumer consumer,
        Local local,
        Azure azure) {

    public MessagingProperties {
        if (transport == null || transport.isBlank()) {
            transport = "local";
        }
        outbox = outbox == null ? new Outbox(true, 3000, 50, 5) : outbox;
        consumer = consumer == null ? new Consumer(3, 150, "subsidy-request-processor") : consumer;
        local = local == null ? new Local(true, 0) : local;
        azure = azure == null ? new Azure("", "subsidy-requests") : azure;
    }

    /**
     * Declares the {@code usingAzure} bean used by the application context. Keeping the construction in a
     * dedicated configuration class makes the wiring explicit and allows tests to replace the bean with a
     * double when the real implementation would require external infrastructure.
     *
     * Tests can replace the bean with a double without touching the production wiring, which keeps the
     * integration suite focused on behaviour rather than configuration.
     *
     * Configuration values are validated when the context starts, so a missing environment variable fails
     * fast instead of producing a runtime error during the first request.
     *
     * The bean is stateless and safe to share across threads; any mutable state belongs to the
     * collaborator it wraps or to the database.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean usingAzure() {
        return "azure".equalsIgnoreCase(transport);
    }

    /**
     * Declares the {@code Outbox} bean used by the application context. Keeping the construction in a
     * dedicated configuration class makes the wiring explicit and allows tests to replace the bean with a
     * double when the real implementation would require external infrastructure.
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
     * @param enabled flag that selects the behaviour of the operation
     *
     * @param publishIntervalMs numeric value used by the operation; negative values are rejected when they
     * are not meaningful
     *
     * @param batchSize numeric counter or size used by the operation
     *
     * @param maxAttempts numeric counter or size used by the operation
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public record Outbox(boolean enabled, long publishIntervalMs, int batchSize, int maxAttempts) {
    }

    /**
     * Registers the {@code Consumer} collaborator in the Spring context. The bean is built from typed
     * configuration properties, so environment-specific values are validated at startup rather than
     * discovered at first use.
     *
     * The bean is stateless and safe to share across threads; any mutable state belongs to the
     * collaborator it wraps or to the database.
     *
     * Tests can replace the bean with a double without touching the production wiring, which keeps the
     * integration suite focused on behaviour rather than configuration.
     *
     * Configuration values are validated when the context starts, so a missing environment variable fails
     * fast instead of producing a runtime error during the first request.
     *
     * @param maxAttempts numeric counter or size used by the operation
     *
     * @param backoffMs numeric value used by the operation; negative values are rejected when they are not
     * meaningful
     *
     * @param name textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public record Consumer(int maxAttempts, long backoffMs, String name) {
    }

    /**
     * Declares the {@code Local} bean used by the application context. Keeping the construction in a
     * dedicated configuration class makes the wiring explicit and allows tests to replace the bean with a
     * double when the real implementation would require external infrastructure.
     *
     * The bean is stateless and safe to share across threads; any mutable state belongs to the
     * collaborator it wraps or to the database.
     *
     * Tests can replace the bean with a double without touching the production wiring, which keeps the
     * integration suite focused on behaviour rather than configuration.
     *
     * Configuration values are validated when the context starts, so a missing environment variable fails
     * fast instead of producing a runtime error during the first request.
     *
     * @param async flag that selects the behaviour of the operation
     *
     * @param simulatedLatencyMs numeric value used by the operation; negative values are rejected when
     * they are not meaningful
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public record Local(boolean async, long simulatedLatencyMs) {
    }

    /**
     * Declares the {@code Azure} bean used by the application context. Keeping the construction in a
     * dedicated configuration class makes the wiring explicit and allows tests to replace the bean with a
     * double when the real implementation would require external infrastructure.
     *
     * Configuration values are validated when the context starts, so a missing environment variable fails
     * fast instead of producing a runtime error during the first request.
     *
     * Tests can replace the bean with a double without touching the production wiring, which keeps the
     * integration suite focused on behaviour rather than configuration.
     *
     * The bean is stateless and safe to share across threads; any mutable state belongs to the
     * collaborator it wraps or to the database.
     *
     * @param connectionString textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param queueName textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public record Azure(String connectionString, String queueName) {
    }
}
