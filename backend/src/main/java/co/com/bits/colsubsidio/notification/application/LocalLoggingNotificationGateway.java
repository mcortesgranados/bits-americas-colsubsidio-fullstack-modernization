package co.com.bits.colsubsidio.notification.application;

import co.com.bits.colsubsidio.notification.domain.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local adapter: the notification is persisted and logged, which is sufficient to demonstrate the flow
 * without external dependencies. Represents the LocalLoggingNotificationGateway component of the
 * application module. It participates in the demonstration platform for affiliates, companies and
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
@ConditionalOnProperty(name = "app.notifications.provider", havingValue = "local-log", matchIfMissing = true)
public class LocalLoggingNotificationGateway implements NotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(LocalLoggingNotificationGateway.class);

    /**
     * Implements the {@code dispatch} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * @param notification input value required by the operation (Notification); it is validated before the
     * state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    public void dispatch(Notification notification) {
        log.info("notification.dispatched channel={} affiliateId={} requestId={} subject='{}'",
                notification.getChannel(), notification.getAffiliateId(), notification.getRequestId(),
                notification.getSubject());
    }
}
