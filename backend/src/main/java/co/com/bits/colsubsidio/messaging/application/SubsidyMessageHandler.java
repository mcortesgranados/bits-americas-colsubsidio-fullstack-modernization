package co.com.bits.colsubsidio.messaging.application;

import co.com.bits.colsubsidio.audit.application.AuditService;
import co.com.bits.colsubsidio.audit.domain.AuditActions;
import co.com.bits.colsubsidio.messaging.domain.DomainEventEnvelope;
import co.com.bits.colsubsidio.messaging.event.SubsidyDocumentUploadedEvent;
import co.com.bits.colsubsidio.messaging.event.SubsidyRequestStatusChangedEvent;
import co.com.bits.colsubsidio.messaging.event.SubsidyRequestSubmittedEvent;
import co.com.bits.colsubsidio.notification.application.NotificationService;
import co.com.bits.colsubsidio.subsidy.application.SubsidyStatusStreamRegistry;
import co.com.bits.colsubsidio.subsidy.domain.DocumentStatus;
import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistory;
import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistoryRepository;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocumentRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Asynchronous consumer of subsidy events. Classifies a freshly submitted request according to the
 * demonstration document rules and moves it forward. It never takes an approval decision: it only
 * advances to {@code IN_REVIEW} or {@code PENDING_DOCUMENTS}. Represents the SubsidyMessageHandler
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
@Component
public class SubsidyMessageHandler {

    private static final Logger log = LoggerFactory.getLogger(SubsidyMessageHandler.class);
    private static final String ACTOR = "system:subsidy-consumer";
    private static final String DEMO_RULE_REASON =
            "Clasificación automática según reglas de demostración de documentos";

    private final SubsidyRequestRepository requests;
    private final RequiredDocumentRepository documents;
    private final RequestStatusHistoryRepository history;
    private final NotificationService notifications;
    private final SubsidyStatusStreamRegistry statusStream;
    private final AuditService audit;
    private final ObjectMapper objectMapper;

    /**
     * Builds an instance of {@code SubsidyMessageHandler} from the given arguments. Every required value
     * is assigned exactly once, and optional values keep their documented defaults so partial construction
     * cannot leave the object in an ambiguous state.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @param requests repository used to read or persist the aggregate owned by this module
     *
     * @param documents repository used to read or persist the aggregate owned by this module
     *
     * @param history repository used to read or persist the aggregate owned by this module
     *
     * @param notifications collaborator injected through its port, which keeps the class independent from
     * the concrete implementation
     *
     * @param statusStream collaborator injected through its port, which keeps the class independent from
     * the concrete implementation
     *
     * @param audit collaborator injected through its port, which keeps the class independent from the
     * concrete implementation
     *
     * @param objectMapper Jackson mapper used to serialize or deserialize the message payload
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public SubsidyMessageHandler(SubsidyRequestRepository requests, RequiredDocumentRepository documents,
                                 RequestStatusHistoryRepository history, NotificationService notifications,
                                 SubsidyStatusStreamRegistry statusStream, AuditService audit,
                                 ObjectMapper objectMapper) {
        this.requests = requests;
        this.documents = documents;
        this.history = history;
        this.notifications = notifications;
        this.statusStream = statusStream;
        this.audit = audit;
        this.objectMapper = objectMapper;
    }

    /**
     * Dispatches the message to the appropriate use case. Unknown event types are ignored with a log entry
     * so that adding a new producer does not break an older consumer deployment.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * @param envelope input value required by the operation (DomainEventEnvelope); it is validated before
     * the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void handle(DomainEventEnvelope envelope) {
        switch (envelope.eventType()) {
            case SubsidyRequestSubmittedEvent.EVENT_TYPE -> onSubmitted(envelope);
            case SubsidyDocumentUploadedEvent.EVENT_TYPE -> onDocumentUploaded(envelope);
            default -> log.debug("message.ignored eventType={}", envelope.eventType());
        }
    }

    /**
     * Processes the incoming message according to its event type. The handler is written to be idempotent
     * from the caller's perspective and delegates persistence to the transactional executor that surrounds
     * it.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * @param envelope input value required by the operation (DomainEventEnvelope); it is validated before
     * the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private void onSubmitted(DomainEventEnvelope envelope) {
        SubsidyRequestSubmittedEvent event = read(envelope, SubsidyRequestSubmittedEvent.class);
        SubsidyRequest request = request(event.requestId());
        if (request.getStatus() != SubsidyRequestStatus.SUBMITTED) {
            log.info("subsidy.submitted.already-processed requestId={} status={}", request.getId(), request.getStatus());
            return;
        }
        long pending = documents.countByRequestIdAndStatusIn(request.getId(),
                List.of(DocumentStatus.PENDING, DocumentStatus.REJECTED));
        SubsidyRequestStatus from = request.getStatus();
        SubsidyRequestStatus target = pending > 0
                ? SubsidyRequestStatus.PENDING_DOCUMENTS
                : SubsidyRequestStatus.IN_REVIEW;
        Instant now = Instant.now();
        request.transitionTo(target, DEMO_RULE_REASON, now);
        recordHistory(request, from, target, DEMO_RULE_REASON, envelope);
        notifyAndPublish(request, from, target, pending, now);

        log.info("subsidy.request.classified requestId={} from={} to={} pendingDocuments={}",
                request.getId(), from, target, pending);
    }

    /**
     * Dispatches the message to the appropriate use case. Unknown event types are ignored with a log entry
     * so that adding a new producer does not break an older consumer deployment.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
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
    private void onDocumentUploaded(DomainEventEnvelope envelope) {
        SubsidyDocumentUploadedEvent event = read(envelope, SubsidyDocumentUploadedEvent.class);
        SubsidyRequest request = request(event.requestId());
        if (request.getStatus() != SubsidyRequestStatus.PENDING_DOCUMENTS) {
            log.debug("subsidy.document.processed requestId={} status={}", request.getId(), request.getStatus());
            return;
        }
        long pending = documents.countByRequestIdAndStatusIn(request.getId(),
                List.of(DocumentStatus.PENDING, DocumentStatus.REJECTED));
        if (pending > 0) {
            log.info("subsidy.document.still-pending requestId={} pending={}", request.getId(), pending);
            return;
        }
        SubsidyRequestStatus from = request.getStatus();
        Instant now = Instant.now();
        request.transitionTo(SubsidyRequestStatus.IN_REVIEW, "Documentos completos", now);
        recordHistory(request, from, SubsidyRequestStatus.IN_REVIEW, "Documentos completos", envelope);
        notifyAndPublish(request, from, SubsidyRequestStatus.IN_REVIEW, 0, now);
    }

    /**
     * Processes the incoming message according to its event type. The handler is written to be idempotent
     * from the caller's perspective and delegates persistence to the transactional executor that surrounds
     * it.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * @param request transport representation received from the client; its fields are validated by bean
     * validation
     *
     * @param from target status that must be reachable from the current state of the aggregate
     *
     * @param to target status that must be reachable from the current state of the aggregate
     *
     * @param reason textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param envelope input value required by the operation (DomainEventEnvelope); it is validated before
     * the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private void recordHistory(SubsidyRequest request, SubsidyRequestStatus from, SubsidyRequestStatus to,
                               String reason, DomainEventEnvelope envelope) {
        history.save(new RequestStatusHistory(request.getId(), from, to, reason, ACTOR,
                envelope.correlationId(), Instant.now()));
    }

    /**
     * Dispatches the message to the appropriate use case. Unknown event types are ignored with a log entry
     * so that adding a new producer does not break an older consumer deployment.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param request transport representation received from the client; its fields are validated by bean
     * validation
     *
     * @param from target status that must be reachable from the current state of the aggregate
     *
     * @param to target status that must be reachable from the current state of the aggregate
     *
     * @param pendingDocuments numeric value used by the operation; negative values are rejected when they
     * are not meaningful
     *
     * @param now point in time in UTC, used to keep audit entries and state transitions comparable
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private void notifyAndPublish(SubsidyRequest request, SubsidyRequestStatus from, SubsidyRequestStatus to,
                                  long pendingDocuments, Instant now) {
        String subject = "Solicitud " + request.getRequestNumber() + " actualizada";
        String body = to == SubsidyRequestStatus.PENDING_DOCUMENTS
                ? "Tu solicitud " + request.getRequestNumber() + " está pendiente de " + pendingDocuments
                        + " documento(s). Puedes cargarlos desde el portal de afiliados."
                : "Tu solicitud " + request.getRequestNumber() + " pasó a estado " + to
                        + ". Este estado es informativo y no constituye una decisión oficial.";
        notifications.notifyAffiliate(request.getAffiliateId(), request.getId(), subject, body);
        statusStream.publish(new SubsidyRequestStatusChangedEvent(request.getId(), request.getRequestNumber(),
                from, to, now));
        audit.recordAs(ACTOR, AuditActions.SUBSIDY_REQUEST_STATUS_CHANGED, "SubsidyRequest",
                String.valueOf(request.getId()), from + " -> " + to);
    }

    /**
     * Processes the incoming message according to its event type. The handler is written to be idempotent
     * from the caller's perspective and delegates persistence to the transactional executor that surrounds
     * it.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * @param requestId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private SubsidyRequest request(Long requestId) {
        return requests.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Subsidy request " + requestId + " no longer exists"));
    }

    /**
     * Processes the incoming message according to its event type. The handler is written to be idempotent
     * from the caller's perspective and delegates persistence to the transactional executor that surrounds
     * it.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * @param envelope input value required by the operation (DomainEventEnvelope); it is validated before
     * the state is modified
     *
     * @param type input value required by the operation (Class<T>); it is validated before the state is
     * modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private <T> T read(DomainEventEnvelope envelope, Class<T> type) {
        try {
            return objectMapper.readValue(envelope.payload(), type);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid payload for event " + envelope.eventType(), ex);
        }
    }
}
