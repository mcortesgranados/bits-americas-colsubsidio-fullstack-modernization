package co.com.bits.colsubsidio.common.correlation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Propagates a correlation identifier for every request: either the caller supplied {@code
 * X-Correlation-Id} or a generated UUID. The value is echoed in the response, stored in the logging
 * MDC, and later persisted in audit records and outbox events so an operation can be traced across the
 * API, the database and the asynchronous consumer. Represents the CorrelationIdFilter component of the
 * correlation module. It participates in the demonstration platform for affiliates, companies and
 * subsidy requests, where the behaviour is driven by explicit business rules rather than by implicit
 * framework magic. The type keeps a single responsibility so that it can be tested in isolation and
 * replaced or extended without affecting unrelated modules. This documentation is generated to the
 * project standard: it states the contract of the type, the layer it belongs to and the expectations
 * that callers may rely on. Personal data is never logged by this component, and any value returned to
 * a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final int MAX_LENGTH = 64;

    /**
     * Performs the {@code doFilterInternal} operation described by its signature. The method keeps a
     * single responsibility and relies on the surrounding layer for validation, transaction management and
     * error translation.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * @param request transport representation received from the client; its fields are validated by bean
     * validation
     *
     * @param response transport representation received from the client; its fields are validated by bean
     * validation
     *
     * @param chain input value required by the operation (FilterChain); it is validated before the state
     * is modified
     *
     * @throws ServletException when the operation cannot be completed; the exception is translated into a
     * problem detail
     *
     * @throws IOException when the operation cannot be completed; the exception is translated into a
     * problem detail
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String correlationId = sanitize(request.getHeader(CorrelationIds.HEADER));
        MDC.put(CorrelationIds.MDC_KEY, correlationId);
        response.setHeader(CorrelationIds.HEADER, correlationId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationIds.MDC_KEY);
        }
    }

    /**
     * Performs the {@code sanitize} operation described by its signature. The method keeps a single
     * responsibility and relies on the surrounding layer for validation, transaction management and error
     * translation.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * @param candidate textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private String sanitize(String candidate) {
        if (!StringUtils.hasText(candidate)) {
            return UUID.randomUUID().toString();
        }
        String trimmed = candidate.trim();
        if (trimmed.length() > MAX_LENGTH || trimmed.chars().anyMatch(Character::isISOControl)) {
            return UUID.randomUUID().toString();
        }
        return trimmed;
    }
}
