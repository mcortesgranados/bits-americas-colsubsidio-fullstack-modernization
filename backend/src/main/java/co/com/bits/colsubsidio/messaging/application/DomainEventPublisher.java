package co.com.bits.colsubsidio.messaging.application;

import co.com.bits.colsubsidio.messaging.domain.DomainEventEnvelope;

/**
 * Output port for the broker. Implementations: local in-process or Azure Service Bus. Represents the
 * DomainEventPublisher component of the application module. It participates in the demonstration
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
public interface DomainEventPublisher {

    /**
     * Executes the {@code publish} application operation. It coordinates the collaborators required by the
     * use case, applies the authorization rules that belong to the operation and translates the outcome
     * into the value expected by the caller.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param envelope input value required by the operation (DomainEventEnvelope); it is validated before
     * the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    void publish(DomainEventEnvelope envelope);
}
