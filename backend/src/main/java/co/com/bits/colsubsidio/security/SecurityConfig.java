package co.com.bits.colsubsidio.security;

import co.com.bits.colsubsidio.common.correlation.CorrelationIds;
import co.com.bits.colsubsidio.config.CorsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Stateless API security: OAuth2 resource server with locally signed JWTs (HS256) for the demo.
 *
 * <p>The token endpoint is the only public business endpoint; everything else requires a valid token
 * and object-level authorization enforced by {@link AccessGuard}. Method security is enabled so that
 * role checks can be declared next to the endpoint they protect, and the filter chain is stateless
 * because the application never creates an HTTP session.
 *
 * <p>Authentication and authorization failures are rendered as RFC 7807 problem details, with the
 * correlation identifier of the request, so a portal can show a consistent error and a support engineer
 * can trace the call in the logs.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/demo-token",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/info",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**"
    };

    /**
     * Builds the single filter chain used by the API.
     *
     * <p>CSRF is disabled because the API is stateless and authenticated with a bearer token, which is
     * not automatically attached by the browser; form login, HTTP basic and logout are disabled for the
     * same reason. The chain permits pre-flight requests and the documented public paths, requires a
     * token for every other business endpoint and denies anything that does not match an explicit rule,
     * so a new controller cannot become public by accident.
     *
     * <p>Both failure paths are customised to return a problem detail with the correlation identifier
     * instead of the default HTML page, and the JWT converter maps the {@code roles} claim directly to
     * authorities without adding a second prefix.
     *
     * @param http                      builder supplied by Spring Security
     * @param jwtAuthenticationConverter converter that translates the {@code roles} claim
     * @param objectMapper              mapper used to serialize the problem details
     * @return the configured filter chain
     * @throws Exception when the builder cannot assemble the chain
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Bean
    SecurityFilterChain apiFilterChain(HttpSecurity http,
                                       JwtAuthenticationConverter jwtAuthenticationConverter,
                                       ObjectMapper objectMapper) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint((request, response, ex) -> writeProblem(response, objectMapper,
                                HttpStatus.UNAUTHORIZED, "Unauthenticated",
                                "A valid bearer token is required to access this resource", "UNAUTHENTICATED"))
                        .accessDeniedHandler((request, response, ex) -> writeProblem(response, objectMapper,
                                HttpStatus.FORBIDDEN, "Forbidden",
                                "The authenticated principal is not allowed to perform this operation",
                                "ACCESS_DENIED")))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, failure) -> writeProblem(response, objectMapper,
                                HttpStatus.UNAUTHORIZED, "Unauthenticated",
                                "A valid bearer token is required to access this resource", "UNAUTHENTICATED"))
                        .accessDeniedHandler((request, response, failure) -> writeProblem(response, objectMapper,
                                HttpStatus.FORBIDDEN, "Forbidden",
                                "The authenticated principal is not allowed to perform this operation",
                                "ACCESS_DENIED")));
        return http.build();
    }

    /**
     * Declares the CORS policy applied to the three portals.
     *
     * <p>Only the origins listed in {@code app.cors.allowed-origins} are accepted, and the list is read
     * from typed configuration so that a deployment can restrict it without recompiling. Credentials
     * are not allowed because the token travels in the {@code Authorization} header and never in a
     * cookie, which also prevents a misconfigured browser from sending ambient credentials. The
     * correlation header is exposed so a portal can quote it when reporting an incident.
     *
     * @param properties typed CORS configuration bound from the environment
     * @return the configuration source consumed by the security filter chain
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.allowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Correlation-Id"));
        configuration.setExposedHeaders(List.of("X-Correlation-Id", "Location"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    /**
     * Translates the {@code roles} claim of the token into Spring Security authorities.
     *
     * <p>The claim already contains values such as {@code ROLE_AFFILIATE}, so the converter is
     * configured with an empty authority prefix; adding the default {@code SCOPE_} prefix would make
     * every {@code hasRole} expression fail. The claim name is set explicitly instead of relying on the
     * default {@code scope} claim, because the demonstration token is not an OAuth2 scope token and the
     * authorization model is role based.
     *
     * @return converter used by the resource server to populate the security context
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthorities = new JwtGrantedAuthoritiesConverter();
        grantedAuthorities.setAuthoritiesClaimName("roles");
        grantedAuthorities.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthorities);
        return converter;
    }

    /**
     * Writes an authentication or authorization failure as an RFC 7807 problem detail.
     *
     * <p>The response uses {@code application/problem+json} and carries the same fields as the errors
     * produced by the controller advice: a stable code and the correlation identifier of the request.
     * The body never includes the exception message, because it may reveal internal details about the
     * token validation, and it never includes the token itself. The method is static because it does not
     * depend on the configuration instance and is easier to reuse from both handlers.
     *
     * @param response servlet response to populate
     * @param mapper   Jackson mapper used to serialize the problem detail
     * @param status   HTTP status to return
     * @param title    short human-readable summary
     * @param detail   explanation without internal details
     * @param code     stable machine-readable code consumed by the portals
     * @throws IOException when the response cannot be written
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private static void writeProblem(HttpServletResponse response, ObjectMapper mapper, HttpStatus status,
                                     String title, String detail, String code) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("correlationId", CorrelationIds.current());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), problem);
    }
}
