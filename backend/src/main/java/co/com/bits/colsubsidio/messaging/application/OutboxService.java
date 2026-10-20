package co.com.bits.colsubsidio.messaging.application;

import co.com.bits.colsubsidio.common.correlation.CorrelationIds;
import co.com.bits.colsubsidio.messaging.domain.OutboxEvent;
import co.com.bits.colsubsidio.messaging.domain.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Appends events to the transactional outbox. Must be called inside the business transaction: the
 * outbox row and the business change commit together. Represents the OutboxService component of the
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
@Service
public class OutboxService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new instance of {@code OutboxService} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @param repository repository used to read or persist the aggregate owned by this module
     *
     * @param objectMapper Jackson mapper used to serialize or deserialize the message payload
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public OutboxService(OutboxEventRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes the {@code append} application operation. It coordinates the collaborators required by the
     * use case, applies the authorization rules that belong to the operation and translates the outcome
     * into the value expected by the caller.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param eventType textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param aggregateType textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param aggregateId textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param payload input value required by the operation (Object); it is validated before the state is
     * modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional
    public void append(String eventType, String aggregateType, String aggregateId, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            repository.save(new OutboxEvent(aggregateType, aggregateId, eventType, json, CorrelationIds.current()));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialise event " + eventType, ex);
        }
    }
}
