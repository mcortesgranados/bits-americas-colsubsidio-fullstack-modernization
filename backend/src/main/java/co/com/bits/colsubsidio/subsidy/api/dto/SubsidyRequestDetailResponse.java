package co.com.bits.colsubsidio.subsidy.api.dto;

import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Detail of one subsidy request: state, money, dates, evidence and timeline (UC-05).
 *
 * <p>The pending and rejected counts are computed here, from the same list that is returned, so the portal
 * never has to derive them and cannot disagree with the table it is rendering. That is what FR-AFI-06 asks
 * for: an explicit statement of how many documents are missing and which ones.
 *
 * <p>The affiliate identifier is not exposed. The caller already proved access to the resource, and repeating
 * the identifier in the body would give the client a value it has no use for.
 *
 * @param id                identifier of the request
 * @param requestNumber     readable radicado
 * @param subsidyType       type of subsidy requested
 * @param status            current status
 * @param requestedAmount   amount requested
 * @param currency          currency of the amount
 * @param purpose           free text sent by the affiliate, may be {@code null}
 * @param submittedAt       instant of registration
 * @param decidedAt         instant of the decision, {@code null} while open
 * @param decisionReason    reason recorded with the decision, {@code null} while open
 * @param pendingDocuments  number of documents still in {@code PENDING}
 * @param rejectedDocuments number of documents in {@code REJECTED}
 * @param documents         every required document with its state
 * @param history           transitions ordered from oldest to newest
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 12:10 GMT-5 Bogotá D.C. Colombia
 */
public record SubsidyRequestDetailResponse(
        Long id,
        String requestNumber,
        String subsidyType,
        String status,
        BigDecimal requestedAmount,
        String currency,
        String purpose,
        Instant submittedAt,
        Instant decidedAt,
        String decisionReason,
        long pendingDocuments,
        long rejectedDocuments,
        List<DocumentSummaryResponse> documents,
        List<StatusHistoryResponse> history) {

    /**
     * Builds the detail from the aggregate and the two collections already loaded by the service.
     *
     * @param request   persisted request
     * @param documents required documents ordered by type
     * @param history   transitions ordered from oldest to newest
     * @return the representation returned by the detail endpoint
     */
    public static SubsidyRequestDetailResponse of(SubsidyRequest request, List<DocumentSummaryResponse> documents,
                                                  List<StatusHistoryResponse> history) {
        long pending = documents.stream().filter(document -> "PENDING".equals(document.status())).count();
        long rejected = documents.stream().filter(document -> "REJECTED".equals(document.status())).count();
        return new SubsidyRequestDetailResponse(
                request.getId(),
                request.getRequestNumber(),
                request.getSubsidyType().name(),
                request.getStatus().name(),
                request.getRequestedAmount(),
                request.getCurrency(),
                request.getPurpose(),
                request.getSubmittedAt(),
                request.getDecidedAt(),
                request.getDecisionReason(),
                pending,
                rejected,
                List.copyOf(documents),
                List.copyOf(history));
    }
}
