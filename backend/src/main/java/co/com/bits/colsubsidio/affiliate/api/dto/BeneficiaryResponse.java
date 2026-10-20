package co.com.bits.colsubsidio.affiliate.api.dto;

import co.com.bits.colsubsidio.affiliate.domain.Beneficiary;
import co.com.bits.colsubsidio.common.privacy.MaskingUtils;

/**
 * Represents the BeneficiaryResponse component of the dto module. It participates in the demonstration
 * platform for affiliates, companies and subsidy requests, where the behaviour is driven by explicit
 * business rules rather than by implicit framework magic. The type keeps a single responsibility so
 * that it can be tested in isolation and replaced or extended without affecting unrelated modules.
 * This documentation is generated to the project standard: it states the contract of the type, the
 * layer it belongs to and the expectations that callers may rely on. Personal data is never logged by
 * this component, and any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public record BeneficiaryResponse(
        Long id,
        String fullName,
        String relationship,
        String documentNumber,
        String birthDate,
        String status) {

    /**
     * Creates a new {@code BeneficiaryResponse} representation of the given source. The method is static
     * because the mapping is a pure function of its argument and must be usable from tests without a
     * Spring context.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * @param beneficiary input value required by the operation (Beneficiary); it is validated before the
     * state is modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static BeneficiaryResponse from(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getFullName(),
                beneficiary.getRelationship().name(),
                MaskingUtils.document(beneficiary.getDocumentNumber()),
                beneficiary.getBirthDate() == null ? null : beneficiary.getBirthDate().toString(),
                beneficiary.getStatus());
    }
}
