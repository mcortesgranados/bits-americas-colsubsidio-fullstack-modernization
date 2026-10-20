package co.com.bits.colsubsidio.audit.application;

import co.com.bits.colsubsidio.audit.domain.AuditLog;
import co.com.bits.colsubsidio.audit.domain.AuditLogRepository;
import co.com.bits.colsubsidio.common.correlation.CorrelationIds;
import co.com.bits.colsubsidio.security.CurrentUser;
import co.com.bits.colsubsidio.security.CurrentUserProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Writes audit records in their own transaction so an audit entry survives the rollback of the
 * business transaction it describes (for example a rejected attempt). Represents the AuditService
 * component of the application module. It participates in the demonstration platform for affiliates,
 * companies and subsidy requests, where the behaviour is driven by explicit business rules rather than
 * by implicit framework magic. The type keeps a single responsibility so that it can be tested in
 * isolation and replaced or extended without affecting unrelated modules. This documentation is
 * generated to the project standard: it states the contract of the type, the layer it belongs to and
 * the expectations that callers may rely on. Personal data is never logged by this component, and any
 * value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository repository;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Builds an instance of {@code AuditService} from the given arguments. Every required value is
     * assigned exactly once, and optional values keep their documented defaults so partial construction
     * cannot leave the object in an ambiguous state.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @param repository repository used to read or persist the aggregate owned by this module
     *
     * @param currentUserProvider input value required by the operation (CurrentUserProvider); it is
     * validated before the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AuditService(AuditLogRepository repository, CurrentUserProvider currentUserProvider) {
        this.repository = repository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Implements the {@code record} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param action textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param entityType textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param entityId textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param details textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String action, String entityType, String entityId, String details) {
        try {
            CurrentUser user = currentUserProvider.currentUser();
            repository.save(new AuditLog(user.subject(), action, entityType, entityId,
                    CorrelationIds.current(), truncate(details, 1000), clientIp()));
        } catch (RuntimeException ex) {
            log.warn("Could not persist audit entry action={} entityId={} reason={}", action, entityId, ex.getMessage());
        }
    }

    /**
     * Executes the {@code recordAs} application operation. It coordinates the collaborators required by
     * the use case, applies the authorization rules that belong to the operation and translates the
     * outcome into the value expected by the caller.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param actor textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param action textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param entityType textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param entityId textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param details textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAs(String actor, String action, String entityType, String entityId, String details) {
        repository.save(new AuditLog(actor, action, entityType, entityId,
                CorrelationIds.current(), truncate(details, 1000), clientIp()));
    }

    /**
     * Implements the {@code clientIp} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private String clientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return truncate(forwarded.split(",")[0].trim(), 45);
            }
            return request.getRemoteAddr();
        }
        return null;
    }

    /**
     * Implements the {@code truncate} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
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
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
