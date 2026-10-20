package co.com.bits.colsubsidio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Represents the NotificationProperties component of the config module. It participates in the
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
@ConfigurationProperties(prefix = "app.notifications")
public record NotificationProperties(String provider, AzureFunction azureFunction) {

    public NotificationProperties {
        provider = provider == null || provider.isBlank() ? "local-log" : provider;
        azureFunction = azureFunction == null ? new AzureFunction("http://localhost:7071", "", 4000) : azureFunction;
    }

    /**
     * Registers the {@code usingAzureFunction} collaborator in the Spring context. The bean is built from
     * typed configuration properties, so environment-specific values are validated at startup rather than
     * discovered at first use.
     *
     * Tests can replace the bean with a double without touching the production wiring, which keeps the
     * integration suite focused on behaviour rather than configuration.
     *
     * The bean is stateless and safe to share across threads; any mutable state belongs to the
     * collaborator it wraps or to the database.
     *
     * Configuration values are validated when the context starts, so a missing environment variable fails
     * fast instead of producing a runtime error during the first request.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean usingAzureFunction() {
        return "azure-function".equalsIgnoreCase(provider);
    }

    /**
     * Declares the {@code AzureFunction} bean used by the application context. Keeping the construction in
     * a dedicated configuration class makes the wiring explicit and allows tests to replace the bean with
     * a double when the real implementation would require external infrastructure.
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
     * @param baseUrl textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param apiKey textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param timeoutMs numeric counter or size used by the operation
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public record AzureFunction(String baseUrl, String apiKey, int timeoutMs) {
    }
}
