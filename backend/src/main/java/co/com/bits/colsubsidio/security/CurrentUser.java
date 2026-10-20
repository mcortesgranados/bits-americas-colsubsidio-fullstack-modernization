package co.com.bits.colsubsidio.security;

import java.util.List;

/**
 * Authenticated principal derived from the validated JWT.
 *
 * <p>Controllers and application services depend on this record instead of reading the security
 * context directly, which keeps them testable: a unit test can build a principal with the exact
 * claims it needs and pass it explicitly. The record carries only the data required for authorization
 * decisions (subject, roles, affiliate identifier and company identifier) and never holds personal
 * data such as names, document numbers or contact details.
 *
 * <p>The lists are defensively copied by {@link List#copyOf(java.util.Collection)} so the instance is
 * immutable and safe to share between threads, which matters because the object is created once per
 * request and consumed by several collaborators.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public record CurrentUser(String subject, List<String> roles, Long affiliateId, Long companyId) {

    /**
     * Normalises the role collection so the principal is immutable and free of {@code null} elements.
     *
     * <p>Spring Security already returns an unmodifiable collection of authorities, but this compact
     * constructor makes the invariant explicit and protects the record when it is instantiated from a
     * test or from the reactive consumer, where the list may come from a plain array. The subject is
     * required because it is the value recorded in the audit trail; the affiliate and company
     * identifiers are optional because staff users are not bound to either of them.
     *
     * @param subject     authenticated username taken from the {@code sub} claim of the token
     * @param roles       authorities granted to the principal, never {@code null}
     * @param affiliateId affiliate associated with the token, or {@code null} for staff users
     * @param companyId   company associated with the token, or {@code null} for staff users
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public CurrentUser {
        roles = roles == null ? List.of() : List.copyOf(roles);
    }

    /**
     * Reports whether the principal holds a specific authority.
     *
     * <p>The comparison is exact and case-sensitive, which is deliberate: authorities are constants
     * defined by the {@code Role} enumeration and a case-insensitive match would hide a typo in a
     * security expression. The check is used by the application layer to decide whether a staff member
     * may read data across affiliates, so a false positive here would widen the visibility of personal
     * information; that risk justifies the strict comparison.
     *
     * @param role authority to look for, for example {@code ROLE_BACKOFFICE}
     * @return {@code true} when the authority is present, {@code false} otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Reports whether the principal holds at least one of the supplied authorities.
     *
     * <p>The method short-circuits on the first match, so the order of the arguments does not affect
     * the result but does affect the number of comparisons. It is used where several roles share the
     * same capability, for example when the back-office and the auditor can both read a request, and
     * it avoids repeating a chain of {@code hasRole} calls with disjunctions in every service.
     *
     * @param candidates authorities to evaluate; an empty array yields {@code false}
     * @return {@code true} when at least one authority is present, {@code false} otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean hasAnyRole(String... candidates) {
        for (String candidate : candidates) {
            if (roles.contains(candidate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Reports whether the principal belongs to the back-office or to the audit team.
     *
     * <p>Staff members may read information across affiliates, always with masked personal data and
     * always leaving an audit entry. The predicate centralises that decision so a future role can be
     * added in one place instead of in every guard, and so the rule is easy to find when reviewing the
     * authorization model. It is intentionally expressed in terms of {@link #hasAnyRole(String...)}
     * rather than duplicating the list of authorities.
     *
     * @return {@code true} when the principal holds {@code ROLE_BACKOFFICE} or {@code ROLE_AUDITOR}
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean isStaff() {
        return hasAnyRole("ROLE_BACKOFFICE", "ROLE_AUDITOR");
    }
}
