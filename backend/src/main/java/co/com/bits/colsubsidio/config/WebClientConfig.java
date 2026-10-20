package co.com.bits.colsubsidio.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Shared reactive HTTP client used by outbound Azure adapters. Timeouts are explicit: an unbounded
 * outbound call is a common cause of thread exhaustion in production. Represents the WebClientConfig
 * component of the config module. It participates in the demonstration platform for affiliates,
 * companies and subsidy requests, where the behaviour is driven by explicit business rules rather than
 * by implicit framework magic. The type keeps a single responsibility so that it can be tested in
 * isolation and replaced or extended without affecting unrelated modules. This documentation is
 * generated to the project standard: it states the contract of the type, the layer it belongs to and
 * the expectations that callers may rely on. Personal data is never logged by this component, and any
 * value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Configuration
public class WebClientConfig {

    /**
     * Declares the {@code webClientBuilder} bean used by the application context. Keeping the construction
     * in a dedicated configuration class makes the wiring explicit and allows tests to replace the bean
     * with a double when the real implementation would require external infrastructure.
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
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Bean
    WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .responseTimeout(Duration.ofSeconds(5));
        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
