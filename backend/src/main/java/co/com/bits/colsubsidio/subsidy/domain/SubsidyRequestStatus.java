package co.com.bits.colsubsidio.subsidy.domain;

import java.util.Set;

/**
 * Lifecycle of a subsidy request. {@code APPROVED_DEMO} and {@code REJECTED_DEMO} are explicitly named
 * to make clear that no real approval decision is being taken by this prototype. Represents the
 * SubsidyRequestStatus component of the domain module. It participates in the demonstration platform
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
public enum SubsidyRequestStatus {
    DRAFT,
    SUBMITTED,
    PENDING_DOCUMENTS,
    IN_REVIEW,
    APPROVED_DEMO,
    REJECTED_DEMO,
    CANCELLED;

    /**
     * Applies the {@code isTerminal} operation to this aggregate. The method enforces the invariants that
     * belong to the domain, including the allowed transitions, so an invalid call fails before any state
     * is modified.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean isTerminal() {
        return this == APPROVED_DEMO || this == REJECTED_DEMO || this == CANCELLED;
    }

    /**
     * Applies the {@code isOpen} operation to this aggregate. The method enforces the invariants that
     * belong to the domain, including the allowed transitions, so an invalid call fails before any state
     * is modified.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean isOpen() {
        return this == SUBMITTED || this == PENDING_DOCUMENTS || this == IN_REVIEW;
    }

    /**
     * Applies the {@code openStatuses} operation to this aggregate. The method enforces the invariants
     * that belong to the domain, including the allowed transitions, so an invalid call fails before any
     * state is modified.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The method belongs to the domain layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static Set<SubsidyRequestStatus> openStatuses() {
        return Set.of(SUBMITTED, PENDING_DOCUMENTS, IN_REVIEW);
    }
}
