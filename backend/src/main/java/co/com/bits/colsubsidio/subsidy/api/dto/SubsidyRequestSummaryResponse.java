package co.com.bits.colsubsidio.subsidy.api.dto;

import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyType;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents the compact representation of a subsidy request that the portals list.
 *
 * <p>The record is the contract of the collection endpoints: it exposes the radicado, the type, the
 * current status, the requested amount and the two dates a user cares about, and deliberately omits the
 * database identifier, the applicant's personal data and the internal version column. Keeping the
 * summary narrow means a listing can never leak a document number or an email even if the underlying
 * query joins more tables than it needs.</p>
 *
 * <p>The type belongs to the API layer and is built from the domain aggregate through the static
 * factory method, so the mapping lives in one place and the entity is never serialised directly by
 * Jackson. Personal data is not part of this record by construction, which is the masking strategy of
 * the prototype: a field that does not exist cannot be leaked by a mapping mistake.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public record SubsidyRequestSummaryResponse(
        Long id,
        String requestNumber,
        SubsidyType subsidyType,
        SubsidyRequestStatus status,
        BigDecimal requestedAmount,
        String currency,
        Instant submittedAt,
        Instant decidedAt) {

    /**
     * Builds the transport representation from the domain aggregate.
     *
     * <p>The conversion is intentionally explicit instead of reflective or convention based: every field
     * of the record is assigned from a named accessor of the entity, so renaming a column or changing a
     * type breaks the compilation instead of silently producing a null in the response. That property is
     * what allows the DTO layer to be treated as the place where the exposure decision is made.</p>
     *
     * <p>The method performs no I/O and does not touch lazy associations; it only reads values that the
     * caller has already loaded, which keeps it usable inside a read-only transaction and in a plain unit
     * test without a persistence context. A {@code null} argument is not expected and is not translated
     * into an empty representation, because that would hide a programming error behind an empty response
     * that looks valid to the client.</p>
     *
     * @param request aggregate loaded by the application service; its scalar values are read once and
     *                copied into the record without retaining a reference to the managed entity
     * @return a new immutable representation with the fields that the portal needs to render a row
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static SubsidyRequestSummaryResponse from(SubsidyRequest request) {
        return new SubsidyRequestSummaryResponse(
                request.getId(),
                request.getRequestNumber(),
                request.getSubsidyType(),
                request.getStatus(),
                request.getRequestedAmount(),
                request.getCurrency(),
                request.getSubmittedAt(),
                request.getDecidedAt());
    }
}
