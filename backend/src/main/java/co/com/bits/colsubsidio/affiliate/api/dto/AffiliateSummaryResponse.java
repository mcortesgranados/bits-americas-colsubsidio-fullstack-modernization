package co.com.bits.colsubsidio.affiliate.api.dto;

import co.com.bits.colsubsidio.affiliate.domain.Affiliate;
import co.com.bits.colsubsidio.common.privacy.MaskingUtils;

/**
 * Minimised projection used in back-office lists: no contact data, only identification and status.
 * Represents the AffiliateSummaryResponse component of the dto module. It participates in the
 * demonstration platform for affiliates, companies and subsidy requests, where the behaviour is driven
 * by explicit business rules rather than by implicit framework magic. The type keeps a single
 * responsibility so that it can be tested in isolation and replaced or extended without affecting
 * unrelated modules. This documentation is generated to the project standard: it states the contract
 * of the type, the layer it belongs to and the expectations that callers may rely on. Personal data is
 * never logged by this component, and any value returned to a client is masked by the DTO layer before
 * serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public record AffiliateSummaryResponse(
        Long id,
        String displayName,
        String documentNumber,
        String category,
        String status,
        Long companyId) {

    /**
     * Creates a new {@code AffiliateSummaryResponse} representation of the given source. The method is
     * static because the mapping is a pure function of its argument and must be usable from tests without
     * a Spring context.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * @param affiliate input value required by the operation (Affiliate); it is validated before the state
     * is modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static AffiliateSummaryResponse from(Affiliate affiliate) {
        return new AffiliateSummaryResponse(
                affiliate.getId(),
                initials(affiliate),
                MaskingUtils.document(affiliate.getDocumentNumber()),
                affiliate.getCategory(),
                affiliate.getStatus().name(),
                affiliate.getCompanyId());
    }

    /**
     * Implements the {@code initials} step of the flow. Inputs are expected to be validated at the
     * boundary; the method focuses on the decision it owns and returns a value that the caller can map
     * directly to the transport contract.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * @param affiliate input value required by the operation (Affiliate); it is validated before the state
     * is modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private static String initials(Affiliate affiliate) {
        String first = affiliate.getFirstName() == null || affiliate.getFirstName().isEmpty()
                ? "" : affiliate.getFirstName().substring(0, 1) + ".";
        String last = affiliate.getLastName() == null || affiliate.getLastName().isEmpty()
                ? "" : affiliate.getLastName().substring(0, 1) + ".";
        return (first + " " + last).trim();
    }
}
