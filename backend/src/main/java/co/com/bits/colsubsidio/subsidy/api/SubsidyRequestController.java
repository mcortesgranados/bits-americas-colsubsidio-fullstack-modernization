package co.com.bits.colsubsidio.subsidy.api;

import co.com.bits.colsubsidio.subsidy.api.dto.StatusHistoryResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestCreateRequest;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestCreatedResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestDetailResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyStatusEventResponse;
import co.com.bits.colsubsidio.subsidy.application.SubsidyRequestQueryService;
import co.com.bits.colsubsidio.subsidy.application.SubsidyRequestService;
import co.com.bits.colsubsidio.subsidy.application.SubsidyStatusStreamRegistry;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * HTTP entry point for the subsidy requests: creation, detail, timeline and live status stream.
 *
 * <p>The write stays in one method that delegates every decision to the application service. The reads are
 * separate endpoints because the portal uses them for different purposes: the detail renders the page once,
 * the timeline can be refreshed alone, and the stream keeps a tab updated without polling. All of them share
 * the same authorization through the query service, which resolves the request and asks the guard before
 * returning anything.
 *
 * <p>The stream is a {@link Flux} of {@link ServerSentEvent} produced from two sources: the replay of what the
 * client missed, taken from the persisted timeline when it reconnects with {@code Last-Event-ID}, and the live
 * fan-out published by the message handler. A comment-only heartbeat is merged every fifteen seconds so
 * proxies do not close an idle connection. The stream terminates when a terminal status is emitted, which
 * releases the connection and the subscriber.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 12:10 GMT-5 Bogotá D.C. Colombia
 */
@RestController
@RequestMapping("/api/v1/subsidy-requests")
@Tag(name = "Subsidy requests", description = "Registration and follow-up of subsidy requests")
@SecurityRequirement(name = "bearer-jwt")
public class SubsidyRequestController {

    private static final Duration HEARTBEAT = Duration.ofSeconds(15);
    private static final String STATUS_EVENT = "status";

    private final SubsidyRequestService service;
    private final SubsidyRequestQueryService queryService;
    private final SubsidyStatusStreamRegistry statusStream;

    /**
     * Receives the services that own the decisions and the in-process fan-out.
     *
     * @param service      creation service
     * @param queryService reads and authorization
     * @param statusStream fan-out of status changes
     */
    public SubsidyRequestController(SubsidyRequestService service, SubsidyRequestQueryService queryService,
                                    SubsidyStatusStreamRegistry statusStream) {
        this.service = service;
        this.queryService = queryService;
        this.statusStream = statusStream;
    }

    /**
     * Registers a new subsidy request for the authenticated affiliate.
     *
     * @param request type, amount and optional purpose chosen by the affiliate
     * @return 201 with the radicado, the initial status and the documents still required
     */
    @PostMapping
    @PreAuthorize("hasRole('AFFILIATE')")
    @Operation(summary = "Register a subsidy request (demonstration rules)")
    public ResponseEntity<SubsidyRequestCreatedResponse> create(
            @Valid @RequestBody SubsidyRequestCreateRequest request) {
        SubsidyRequestCreatedResponse created = service.create(request);
        return ResponseEntity.created(URI.create("/api/v1/subsidy-requests/" + created.id())).body(created);
    }

    /**
     * Returns the detail of one request with its evidence and its timeline.
     *
     * @param requestId identifier of the request
     * @return state, amounts, dates, documents and history
     */
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('AFFILIATE','BACKOFFICE','AUDITOR')")
    @Operation(summary = "Detail of a subsidy request")
    public SubsidyRequestDetailResponse detail(@PathVariable Long requestId) {
        return queryService.detail(requestId);
    }

    /**
     * Returns the status timeline of one request.
     *
     * @param requestId identifier of the request
     * @return transitions from oldest to newest
     */
    @GetMapping("/{requestId}/history")
    @PreAuthorize("hasAnyRole('AFFILIATE','BACKOFFICE','AUDITOR')")
    @Operation(summary = "Status history of a subsidy request")
    public List<StatusHistoryResponse> history(@PathVariable Long requestId) {
        return queryService.timeline(requestId);
    }

    /**
     * Streams the status changes of one request.
     *
     * @param requestId    identifier of the request
     * @param lastEventId  last history identifier the browser received, sent by the client on reconnection
     * @return a stream of status events plus a periodic heartbeat
     */
    @GetMapping(value = "/{requestId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAnyRole('AFFILIATE','BACKOFFICE','AUDITOR')")
    @Operation(summary = "Server-Sent Events with the status changes of a subsidy request")
    public Flux<ServerSentEvent<SubsidyStatusEventResponse>> events(
            @PathVariable Long requestId,
            @RequestHeader(name = "Last-Event-ID", required = false) String lastEventId) {
        queryService.authorize(requestId);

        Flux<ServerSentEvent<SubsidyStatusEventResponse>> replay =
                Flux.fromIterable(queryService.replay(requestId, parseEventId(lastEventId)))
                        .map(history -> ServerSentEvent.builder(SubsidyStatusEventResponse.from(requestId, history))
                                .id(String.valueOf(history.id()))
                                .event(STATUS_EVENT)
                                .build());

        Flux<ServerSentEvent<SubsidyStatusEventResponse>> live = statusStream.stream(requestId)
                .map(event -> ServerSentEvent.builder(SubsidyStatusEventResponse.from(event))
                        .event(STATUS_EVENT)
                        .build());

        Flux<ServerSentEvent<SubsidyStatusEventResponse>> heartbeat = Flux.interval(HEARTBEAT)
                .map(tick -> ServerSentEvent.<SubsidyStatusEventResponse>builder().comment("heartbeat").build());

        return Flux.merge(replay, live, heartbeat)
                .takeUntil(event -> event.data() != null && isTerminal(event.data().status()));
    }

    /**
     * Parses the {@code Last-Event-ID} header, ignoring values that are not numeric.
     *
     * @param lastEventId header value, may be {@code null}
     * @return the identifier or {@code null} when there is nothing to replay from
     */
    private Long parseEventId(String lastEventId) {
        if (lastEventId == null || lastEventId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(lastEventId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    /**
     * Tells whether a status name is terminal, tolerating an unknown value from a future version.
     *
     * @param status status name sent in the event
     * @return {@code true} when the stream should close
     */
    private boolean isTerminal(String status) {
        try {
            return SubsidyRequestStatus.valueOf(status).isTerminal();
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
