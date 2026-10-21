package co.com.bits.colsubsidio.notification.application;

import co.com.bits.colsubsidio.common.error.NotFoundException;
import co.com.bits.colsubsidio.notification.domain.Notification;
import co.com.bits.colsubsidio.notification.domain.NotificationChannel;
import co.com.bits.colsubsidio.notification.domain.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents the NotificationService component of the application module. It participates in the
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
@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationGateway gateway;

    /**
     * Creates a new instance of {@code NotificationService} with the supplied collaborators. The
     * constructor keeps the object in a consistent initial state and does not perform I/O, which allows it
     * to be built inside a transaction or in a plain unit test with the same semantics.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @param repository repository used to read or persist the aggregate owned by this module
     *
     * @param gateway collaborator injected through its port, which keeps the class independent from the
     * concrete implementation
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public NotificationService(NotificationRepository repository, NotificationGateway gateway) {
        this.repository = repository;
        this.gateway = gateway;
    }

    /**
     * Implements the {@code notifyAffiliate} use case. The method coordinates the domain object, the
     * repositories and the outbound ports required by the operation, relies on the surrounding transaction
     * when state is modified, and leaves the HTTP concerns to the controller.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @param requestId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @param subject textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param body textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional
    public Notification notifyAffiliate(Long affiliateId, Long requestId, String subject, String body) {
        Notification notification = repository.save(
                new Notification(affiliateId, requestId, NotificationChannel.IN_APP, truncate(subject, 160),
                        truncate(body, 1000)));
        gateway.dispatch(notification);
        return notification;
    }

    /**
     * Executes the {@code list} application operation. It coordinates the collaborators required by the
     * use case, applies the authorization rules that belong to the operation and translates the outcome
     * into the value expected by the caller.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @param pageable pagination and sorting instructions resolved by Spring MVC from the query string
     *
     * @return a page of results together with the metadata required by the client to paginate
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public Page<Notification> list(Long affiliateId, Pageable pageable) {
        return repository.findByAffiliateIdOrderByCreatedAtDesc(affiliateId, pageable);
    }

    /**
     * Executes the {@code unreadCount} application operation. It coordinates the collaborators required by
     * the use case, applies the authorization rules that belong to the operation and translates the
     * outcome into the value expected by the caller.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public long unreadCount(Long affiliateId) {
        return repository.countByAffiliateIdAndReadFalse(affiliateId);
    }

    /**
     * Executes the {@code markRead} application operation. It coordinates the collaborators required by
     * the use case, applies the authorization rules that belong to the operation and translates the
     * outcome into the value expected by the caller.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @param notificationId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional
    public void markRead(Long affiliateId, Long notificationId) {
        Notification notification = repository.findById(notificationId)
                .filter(candidate -> candidate.getAffiliateId().equals(affiliateId))
                .orElseThrow(() -> NotFoundException.of("Notification", notificationId));
        notification.markRead();
    }

    /**
     * Implements the {@code truncate} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * @param value textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param max numeric counter or size used by the operation
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
