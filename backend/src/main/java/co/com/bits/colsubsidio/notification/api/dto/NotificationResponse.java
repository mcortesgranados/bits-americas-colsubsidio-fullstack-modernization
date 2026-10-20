package co.com.bits.colsubsidio.notification.api.dto;

import co.com.bits.colsubsidio.notification.domain.Notification;
import java.time.Instant;

/**
 * Represents the NotificationResponse component of the dto module. It participates in the
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
public record NotificationResponse(
        Long id,
        Long requestId,
        String channel,
        String subject,
        String body,
        boolean read,
        Instant createdAt) {

    /**
     * Creates a new {@code NotificationResponse} representation of the given source. The method is static
     * because the mapping is a pure function of its argument and must be usable from tests without a
     * Spring context.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * @param notification input value required by the operation (Notification); it is validated before the
     * state is modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(), notification.getRequestId(),
                notification.getChannel().name(), notification.getSubject(), notification.getBody(),
                notification.isRead(), notification.getCreatedAt());
    }
}
