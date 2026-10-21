package co.com.bits.colsubsidio.subsidy.application;

import co.com.bits.colsubsidio.common.error.NotFoundException;
import co.com.bits.colsubsidio.security.AccessGuard;
import co.com.bits.colsubsidio.subsidy.api.dto.DocumentSummaryResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.StatusHistoryResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestDetailResponse;
import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistoryRepository;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocumentRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read side of a subsidy request: detail, timeline and the replay used when an SSE client reconnects (UC-05).
 *
 * <p>Every method resolves the request and asks {@link AccessGuard} whether the caller may see it, so the
 * authorization is the same for the REST endpoints and for the stream. Staff roles pass the guard and can
 * read across affiliates; an affiliate only reaches its own requests, and a request of somebody else answers
 * 403 without revealing whether it exists.
 *
 * <p>The detail is assembled from three indexed reads —the aggregate, its documents and its timeline— instead
 * of a single join, because each collection is small and this keeps the mapping explicit. The counts of
 * pending and rejected documents are derived from the same list that is returned, which guarantees that the
 * banner the portal shows and the table it renders can never disagree.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 12:10 GMT-5 Bogotá D.C. Colombia
 */
@Service
public class SubsidyRequestQueryService {

    private final SubsidyRequestRepository requests;
    private final RequiredDocumentRepository documents;
    private final RequestStatusHistoryRepository history;
    private final AccessGuard accessGuard;

    /**
     * Creates the service with the repositories it reads and the guard that authorizes them.
     *
     * @param requests    repository of the requests
     * @param documents   repository of the required documents
     * @param history     repository of the status timeline
     * @param accessGuard object-level authorization
     */
    public SubsidyRequestQueryService(SubsidyRequestRepository requests, RequiredDocumentRepository documents,
                                      RequestStatusHistoryRepository history, AccessGuard accessGuard) {
        this.requests = requests;
        this.documents = documents;
        this.history = history;
        this.accessGuard = accessGuard;
    }

    /**
     * Returns the detail of one request with its evidence and its timeline.
     *
     * @param requestId identifier supplied by the client
     * @return the detail representation
     * @throws NotFoundException when the request does not exist
     */
    @Transactional(readOnly = true)
    public SubsidyRequestDetailResponse detail(Long requestId) {
        SubsidyRequest request = requireAccess(requestId);
        List<DocumentSummaryResponse> requiredDocuments =
                documents.findByRequestIdOrderByDocumentTypeAsc(requestId).stream()
                        .map(DocumentSummaryResponse::from)
                        .toList();
        List<StatusHistoryResponse> timeline = timelineOf(requestId);
        return SubsidyRequestDetailResponse.of(request, requiredDocuments, timeline);
    }

    /**
     * Returns the ordered timeline of one request.
     *
     * @param requestId identifier supplied by the client
     * @return transitions from oldest to newest
     */
    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> timeline(Long requestId) {
        requireAccess(requestId);
        return timelineOf(requestId);
    }

    private List<StatusHistoryResponse> timelineOf(Long requestId) {
        return history.findByRequestIdOrderByOccurredAtAscIdAsc(requestId).stream()
                .map(StatusHistoryResponse::from)
                .toList();
    }

    /**
     * Returns the transitions recorded after a given history identifier.
     *
     * <p>It feeds the replay of the SSE stream: when the browser reconnects it sends {@code Last-Event-ID} and
     * receives only what it missed, instead of the whole timeline. A missing identifier means "no previous
     * event", in which case the timeline is returned from the beginning; that is also what a client that opens
     * the stream for the first time receives before the live events start.
     *
     * @param requestId   request whose timeline is replayed
     * @param lastEventId last history identifier received by the client, may be {@code null}
     * @return transitions with an identifier greater than {@code lastEventId}
     */
    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> replay(Long requestId, Long lastEventId) {
        requireAccess(requestId);
        List<StatusHistoryResponse> timeline = lastEventId == null
                ? history.findByRequestIdOrderByOccurredAtAscIdAsc(requestId).stream()
                        .map(StatusHistoryResponse::from).toList()
                : history.findByRequestIdAndIdGreaterThanOrderByIdAsc(requestId, lastEventId).stream()
                        .map(StatusHistoryResponse::from).toList();
        return timeline;
    }

    /**
     * Authorizes access to the request without building its representation.
     *
     * <p>The SSE endpoint calls it before subscribing: once the stream is open there is no HTTP status left to
     * send, so the 403 has to happen while the response is still a normal one.
     *
     * @param requestId request the caller intends to follow
     */
    @Transactional(readOnly = true)
    public void authorize(Long requestId) {
        requireAccess(requestId);
    }

    /**
     * Loads the request and applies the object-level authorization.
     *
     * @param requestId identifier supplied by the client
     * @return the persisted request
     */
    private SubsidyRequest requireAccess(Long requestId) {
        SubsidyRequest request = requests.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("Subsidy request", requestId));
        accessGuard.requireAffiliateAccess(request.getAffiliateId());
        return request;
    }
}
