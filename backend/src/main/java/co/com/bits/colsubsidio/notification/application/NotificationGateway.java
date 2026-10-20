package co.com.bits.colsubsidio.notification.application;

import co.com.bits.colsubsidio.notification.domain.Notification;

/**
 * Outbound notification port. The local implementation writes structured logs (enough for the demo),
 * while the Azure adapter calls an Azure Function that would own e-mail/SMS delivery. Represents the
 * NotificationGateway component of the application module. It participates in the demonstration
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
public interface NotificationGateway {

    /**
     * Implements the {@code dispatch} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param notification input value required by the operation (Notification); it is validated before the
     * state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    void dispatch(Notification notification);
}
