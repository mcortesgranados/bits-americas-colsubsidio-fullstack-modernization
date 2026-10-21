package co.com.bits.colsubsidio.subsidy.api.dto;

import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistory;
import java.time.Instant;

/**
 * One transition of the status timeline of a subsidy request (UC-05).
 *
 * <p>It exposes who changed the status, from which state to which, why and when, together with the
 * correlation identifier that ties the transition to the logs. The actor is the subject of the token and never
 * the person's name, so the timeline can be shown without leaking personal data.
 *
 * @param id            identifier of the history row, also used as the SSE event id for replay
 * @param fromStatus    previous status, {@code null} for the first entry
 * @param toStatus      status after the transition
 * @param reason        human-readable explanation recorded by the actor
 * @param actor         subject of the token that performed the transition
 * @param occurredAt    instant of the transition
 * @param correlationId correlation identifier of the request that produced the change
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 12:10 GMT-5 Bogotá D.C. Colombia
 */
public record StatusHistoryResponse(
        Long id,
        String fromStatus,
        String toStatus,
        String reason,
        String actor,
        Instant occurredAt,
        String correlationId) {

    /**
     * Maps the persisted row to the transport record.
     *
     * @param history persisted transition
     * @return the representation used by the detail and history endpoints
     */
    public static StatusHistoryResponse from(RequestStatusHistory history) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getFromStatus() == null ? null : history.getFromStatus().name(),
                history.getToStatus().name(),
                history.getReason(),
                history.getActor(),
                history.getOccurredAt(),
                history.getCorrelationId());
    }
}
