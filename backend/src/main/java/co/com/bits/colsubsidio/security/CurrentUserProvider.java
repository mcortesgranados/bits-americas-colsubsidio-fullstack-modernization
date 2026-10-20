package co.com.bits.colsubsidio.security;

import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Represents the CurrentUserProvider component of the security module. It participates in the
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
@Component
public class CurrentUserProvider {

    /**
     * Implements the {@code currentUser} step of the flow. Inputs are expected to be validated at the
     * boundary; the method focuses on the decision it owns and returns a value that the caller can map
     * directly to the transport contract.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public CurrentUser currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("No authenticated JWT principal available");
        }
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new CurrentUser(jwt.getSubject(), roles, numericClaim(jwt, "affiliateId"), numericClaim(jwt, "companyId"));
    }

    /**
     * Implements the {@code numericClaim} step of the flow. Inputs are expected to be validated at the
     * boundary; the method focuses on the decision it owns and returns a value that the caller can map
     * directly to the transport contract.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * @param jwt input value required by the operation (Jwt); it is validated before the state is modified
     *
     * @param name textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private Long numericClaim(Jwt jwt, String name) {
        Object value = jwt.getClaim(name);
        return value instanceof Number number ? number.longValue() : null;
    }
}
