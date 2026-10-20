package co.com.bits.colsubsidio.affiliate.api.dto;

import co.com.bits.colsubsidio.affiliate.domain.Affiliate;
import co.com.bits.colsubsidio.common.privacy.MaskingUtils;
import java.time.Instant;

/**
 * Affiliate profile. PII is masked by construction: the record cannot carry the raw document number,
 * so a mapping mistake cannot leak it. Represents the AffiliateProfileResponse component of the dto
 * module. It participates in the demonstration platform for affiliates, companies and subsidy
 * requests, where the behaviour is driven by explicit business rules rather than by implicit framework
 * magic. The type keeps a single responsibility so that it can be tested in isolation and replaced or
 * extended without affecting unrelated modules. This documentation is generated to the project
 * standard: it states the contract of the type, the layer it belongs to and the expectations that
 * callers may rely on. Personal data is never logged by this component, and any value returned to a
 * client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public record AffiliateProfileResponse(
        Long id,
        String fullName,
        String documentType,
        String documentNumber,
        String email,
        String phone,
        String category,
        String status,
        Long companyId,
        String companyName,
        Instant createdAt,
        Instant updatedAt) {

    /**
     * Builds a new {@code AffiliateProfileResponse} from the supplied source object. The conversion
     * centralises the mapping rules, which keeps controllers and services free of repetitive
     * field-by-field copying and guarantees that masking is applied in a single place.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * @param affiliate input value required by the operation (Affiliate); it is validated before the state
     * is modified
     *
     * @param companyName textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param maskContact flag that selects the behaviour of the operation
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static AffiliateProfileResponse from(Affiliate affiliate, String companyName, boolean maskContact) {
        return new AffiliateProfileResponse(
                affiliate.getId(),
                affiliate.fullName(),
                affiliate.getDocumentType(),
                MaskingUtils.document(affiliate.getDocumentNumber()),
                maskContact ? MaskingUtils.email(affiliate.getEmail()) : affiliate.getEmail(),
                maskContact ? MaskingUtils.phone(affiliate.getPhone()) : affiliate.getPhone(),
                affiliate.getCategory(),
                affiliate.getStatus().name(),
                affiliate.getCompanyId(),
                companyName,
                affiliate.getCreatedAt(),
                affiliate.getUpdatedAt());
    }
}
