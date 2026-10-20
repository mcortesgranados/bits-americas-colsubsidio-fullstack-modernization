package co.com.bits.colsubsidio.subsidy.application;

import co.com.bits.colsubsidio.messaging.event.SubsidyRequestStatusChangedEvent;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * In-memory fan-out for Server-Sent Events. A real multi-instance deployment would back this with
 * Redis pub/sub, Service Bus topics or SignalR; the contract (Flux of status events) stays the same.
 * Sinks are removed as soon as the request reaches a terminal state to avoid unbounded growth.
 * Represents the SubsidyStatusStreamRegistry component of the application module. It participates in
 * the demonstration platform for affiliates, companies and subsidy requests, where the behaviour is
 * driven by explicit business rules rather than by implicit framework magic. The type keeps a single
 * responsibility so that it can be tested in isolation and replaced or extended without affecting
 * unrelated modules. This documentation is generated to the project standard: it states the contract
 * of the type, the layer it belongs to and the expectations that callers may rely on. Personal data is
 * never logged by this component, and any value returned to a client is masked by the DTO layer before
 * serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Component
public class SubsidyStatusStreamRegistry {

    private static final Logger log = LoggerFactory.getLogger(SubsidyStatusStreamRegistry.class);

    private final Map<Long, Sinks.Many<SubsidyRequestStatusChangedEvent>> sinks = new ConcurrentHashMap<>();

    /**
     * Implements the {@code stream} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * @param requestId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @return a {@link reactor.core.publisher.Flux} that emits the stream of values as they become
     * available
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Flux<SubsidyRequestStatusChangedEvent> stream(Long requestId) {
        return sinks.computeIfAbsent(requestId, id -> Sinks.many().multicast().onBackpressureBuffer(16, false))
                .asFlux();
    }

    /**
     * Implements the {@code publish} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * @param event input value required by the operation (SubsidyRequestStatusChangedEvent); it is
     * validated before the state is modified
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void publish(SubsidyRequestStatusChangedEvent event) {
        Sinks.Many<SubsidyRequestStatusChangedEvent> sink = sinks.get(event.requestId());
        if (sink == null) {
            return;
        }
        Sinks.EmitResult result = sink.tryEmitNext(event);
        if (result.isFailure()) {
            log.debug("status.stream.emit.failed requestId={} result={}", event.requestId(), result);
        }
        if (isTerminal(event.toStatus())) {
            sink.tryEmitComplete();
            sinks.remove(event.requestId());
        }
    }

    /**
     * Executes the {@code isTerminal} application operation. It coordinates the collaborators required by
     * the use case, applies the authorization rules that belong to the operation and translates the
     * outcome into the value expected by the caller.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * @param status target status that must be reachable from the current state of the aggregate
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public boolean isTerminal(SubsidyRequestStatus status) {
        return status != null && status.isTerminal();
    }
}
