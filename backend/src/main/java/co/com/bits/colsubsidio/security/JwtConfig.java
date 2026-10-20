package co.com.bits.colsubsidio.security;

import co.com.bits.colsubsidio.config.AppSecurityProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * Represents the JwtConfig component of the security module. It participates in the demonstration
 * platform for affiliates, companies and subsidy requests, where the behaviour is driven by explicit
 * business rules rather than by implicit framework magic. The type keeps a single responsibility so
 * that it can be tested in isolation and replaced or extended without affecting unrelated modules.
 * This documentation is generated to the project standard: it states the contract of the type, the
 * layer it belongs to and the expectations that callers may rely on. Personal data is never logged by
 * this component, and any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Configuration
public class JwtConfig {

    private static final int MIN_SECRET_BYTES = 32;

    /**
     * Declares the {@code jwtDecoder} bean used by the application context. Keeping the construction in a
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
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Bean
    JwtDecoder jwtDecoder(AppSecurityProperties properties) {
        return NimbusJwtDecoder.withSecretKey(secretKey(properties)).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /**
     * Declares the {@code jwtEncoder} bean used by the application context. Keeping the construction in a
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
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Bean
    JwtEncoder jwtEncoder(AppSecurityProperties properties) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey(properties)));
    }

    /**
     * Registers the {@code passwordEncoder} collaborator in the Spring context. The bean is built from
     * typed configuration properties, so environment-specific values are validated at startup rather than
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
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Declares the {@code secretKey} bean used by the application context. Keeping the construction in a
     * dedicated configuration class makes the wiring explicit and allows tests to replace the bean with a
     * double when the real implementation would require external infrastructure.
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
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private SecretKey secretKey(AppSecurityProperties properties) {
        byte[] bytes = properties.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must be at least " + MIN_SECRET_BYTES + " bytes for HS256");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
}
