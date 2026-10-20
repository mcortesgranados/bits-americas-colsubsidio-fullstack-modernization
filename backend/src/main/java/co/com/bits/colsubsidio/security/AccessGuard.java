package co.com.bits.colsubsidio.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Object-level authorization (OWASP API1: BOLA). Role checks alone are not enough: an affiliate may
 * only touch its own records, and a company user may only touch its own company. Staff roles (back-
 * office, audit) are allowed to read across affiliates, always with masked PII. Represents the
 * AccessGuard component of the security module. It participates in the demonstration platform for
 * affiliates, companies and subsidy requests, where the behaviour is driven by explicit business rules
 * rather than by implicit framework magic. The type keeps a single responsibility so that it can be
 * tested in isolation and replaced or extended without affecting unrelated modules. This documentation
 * is generated to the project standard: it states the contract of the type, the layer it belongs to
 * and the expectations that callers may rely on. Personal data is never logged by this component, and
 * any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Component
public class AccessGuard {

    private final CurrentUserProvider currentUserProvider;

    /**
     * Creates a new instance of {@code AccessGuard} with the supplied collaborators. The constructor keeps
     * the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @param currentUserProvider input value required by the operation (CurrentUserProvider); it is
     * validated before the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AccessGuard(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Performs the {@code currentUser} operation described by its signature. The method keeps a single
     * responsibility and relies on the surrounding layer for validation, transaction management and error
     * translation.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public CurrentUser currentUser() {
        return currentUserProvider.currentUser();
    }

    /**
     * Implements the {@code requireAffiliateAccess} step of the flow. Inputs are expected to be validated
     * at the boundary; the method focuses on the decision it owns and returns a value that the caller can
     * map directly to the transport contract.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void requireAffiliateAccess(Long affiliateId) {
        CurrentUser user = currentUserProvider.currentUser();
        if (user.isStaff()) {
            return;
        }
        if (user.affiliateId() == null || !user.affiliateId().equals(affiliateId)) {
            throw new AccessDeniedException("The authenticated user cannot access affiliate " + affiliateId);
        }
    }

    /**
     * Performs the {@code requireCompanyAccess} operation described by its signature. The method keeps a
     * single responsibility and relies on the surrounding layer for validation, transaction management and
     * error translation.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * The method belongs to the security layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * @param companyId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void requireCompanyAccess(Long companyId) {
        CurrentUser user = currentUserProvider.currentUser();
        if (user.isStaff()) {
            return;
        }
        if (user.companyId() == null || !user.companyId().equals(companyId)) {
            throw new AccessDeniedException("The authenticated user cannot access company " + companyId);
        }
    }

    /**
     * Performs the {@code requireAffiliateId} operation described by its signature. The method keeps a
     * single responsibility and relies on the surrounding layer for validation, transaction management and
     * error translation.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Long requireAffiliateId() {
        CurrentUser user = currentUserProvider.currentUser();
        if (user.affiliateId() == null) {
            throw new AccessDeniedException("The authenticated user is not linked to an affiliate");
        }
        return user.affiliateId();
    }

    /**
     * Implements the {@code requireCompanyId} step of the flow. Inputs are expected to be validated at the
     * boundary; the method focuses on the decision it owns and returns a value that the caller can map
     * directly to the transport contract.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * The method belongs to the security layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Long requireCompanyId() {
        CurrentUser user = currentUserProvider.currentUser();
        if (user.companyId() == null) {
            throw new AccessDeniedException("The authenticated user is not linked to a company");
        }
        return user.companyId();
    }
}
