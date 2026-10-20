package co.com.bits.colsubsidio.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Security configuration. JWT signing material and demo credentials are injected through environment
 * variables; the defaults exist only so the demo can boot on a developer machine. Represents the
 * AppSecurityProperties component of the config module. It participates in the demonstration platform
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
@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(Jwt jwt, List<DemoUser> demoUsers) {

    /**
     * Declares the {@code Jwt} bean used by the application context. Keeping the construction in a
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
     * @param issuer textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param secret textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param ttl input value required by the operation (Duration); it is validated before the state is
     * modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public record Jwt(String issuer, String secret, Duration ttl) {
        public Jwt {
            if (ttl == null) {
                ttl = Duration.ofHours(2);
            }
        }
    }

    /**
     * Declares the {@code DemoUser} bean used by the application context. Keeping the construction in a
     * dedicated configuration class makes the wiring explicit and allows tests to replace the bean with a
     * double when the real implementation would require external infrastructure.
     *
     * Configuration values are validated when the context starts, so a missing environment variable fails
     * fast instead of producing a runtime error during the first request.
     *
     * The bean is stateless and safe to share across threads; any mutable state belongs to the
     * collaborator it wraps or to the database.
     *
     * Tests can replace the bean with a double without touching the production wiring, which keeps the
     * integration suite focused on behaviour rather than configuration.
     *
     * @param username textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param password textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param role textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param affiliateDocument textual value supplied by the caller; it is validated at the boundary
     * before reaching this member
     *
     * @param companyNit textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public record DemoUser(
            String username,
            String password,
            String role,
            String affiliateDocument,
            String companyNit) {
    }
}
