package co.com.bits.colsubsidio.subsidy.api.dto;

import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Result of registering a subsidy request (UC-03).
 *
 * <p>The response repeats the values the client just sent —type and amount— because the server may have
 * normalised the amount, and returns the radicado, the initial status and the catalogue of documents the
 * affiliate still has to upload. That catalogue is what lets the portal move the user straight to the next
 * step (UC-04) without a second round trip. No personal data and no affiliate identifier are included.
 *
 * @param id                surrogate identifier used by the follow-up endpoints
 * @param requestNumber     readable and unique radicado, format {@code SOL-<year>-<sequence>}
 * @param status            status after registration, always {@code SUBMITTED}
 * @param subsidyType       type of subsidy requested
 * @param requestedAmount   amount accepted by the server
 * @param currency          currency of the amount, always {@code COP}
 * @param submittedAt       instant of registration
 * @param requiredDocuments document types the affiliate must upload, ordered by enum name
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 10:40 GMT-5 Bogotá D.C. Colombia
 */
public record SubsidyRequestCreatedResponse(
        Long id,
        String requestNumber,
        String status,
        String subsidyType,
        BigDecimal requestedAmount,
        String currency,
        Instant submittedAt,
        List<DocumentType> requiredDocuments) {

    /**
     * Maps the persisted aggregate and the document catalogue to the transport record.
     *
     * @param request           persisted request, already in {@code SUBMITTED}
     * @param requiredDocuments document types created for the request
     * @return the representation returned to the portal with a 201
     */
    public static SubsidyRequestCreatedResponse from(SubsidyRequest request, List<DocumentType> requiredDocuments) {
        return new SubsidyRequestCreatedResponse(
                request.getId(),
                request.getRequestNumber(),
                request.getStatus().name(),
                request.getSubsidyType().name(),
                request.getRequestedAmount(),
                request.getCurrency(),
                request.getSubmittedAt(),
                List.copyOf(requiredDocuments));
    }
}
