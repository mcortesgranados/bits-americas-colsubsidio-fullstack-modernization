package co.com.bits.colsubsidio.subsidy.api.dto;

import co.com.bits.colsubsidio.messaging.event.SubsidyRequestStatusChangedEvent;
import java.time.Instant;

/**
 * Payload of one Server-Sent Event of the status stream (UC-05).
 *
 * <p>It is deliberately smaller than the detail response: the portal only needs to know that something
 * changed and to which status, and then refreshes the detail. Sending the whole aggregate on every transition
 * would duplicate the read model and multiply the chances of showing stale data.
 *
 * @param requestId  identifier of the request that changed
 * @param fromStatus previous status, {@code null} when the origin is unknown
 * @param status     status after the change
 * @param occurredAt instant of the change
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 12:10 GMT-5 Bogotá D.C. Colombia
 */
public record SubsidyStatusEventResponse(Long requestId, String fromStatus, String status, Instant occurredAt) {

    /**
     * Maps the internal fan-out event to the wire payload.
     *
     * @param event event published by the message handler after a transition
     * @return the payload sent to the browser
     */
    public static SubsidyStatusEventResponse from(SubsidyRequestStatusChangedEvent event) {
        return new SubsidyStatusEventResponse(event.requestId(),
                event.fromStatus() == null ? null : event.fromStatus().name(),
                event.toStatus().name(), event.occurredAt());
    }

    /**
     * Builds a payload from a persisted transition, used to replay what the client missed.
     *
     * @param requestId identifier of the request being replayed
     * @param history   persisted transition
     * @return the payload sent to the browser
     */
    public static SubsidyStatusEventResponse from(Long requestId, StatusHistoryResponse history) {
        return new SubsidyStatusEventResponse(requestId, history.fromStatus(), history.toStatus(), history.occurredAt());
    }
}
