package co.com.bits.colsubsidio.subsidy.application;

import co.com.bits.colsubsidio.common.error.BusinessRuleException;
import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyType;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * DEMONSTRATION RULES ONLY.
 *
 * <p>The limits, eligibility conditions and document catalogue declared here are invented for the
 * technical exercise. They are not Colsubsidio policies and must be replaced by the real rule engine or
 * by parameterised configuration before any production use. Keeping them in one component makes the
 * substitution explicit: the rest of the application asks for a limit or a document set and never
 * embeds a number or a business condition of its own.
 *
 * <p>The component is stateless and free of database access, so every rule can be exercised with a
 * plain unit test. The catalogues are immutable after the static initialiser, which makes the rules
 * safe to use from several request threads without synchronisation.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Component
public class SubsidyDemoRules {

    private static final Map<SubsidyType, BigDecimal> DEMO_MAX_AMOUNT = new EnumMap<>(SubsidyType.class);
    private static final Map<SubsidyType, Set<DocumentType>> DEMO_REQUIRED_DOCUMENTS =
            new EnumMap<>(SubsidyType.class);

    static {
        DEMO_MAX_AMOUNT.put(SubsidyType.VIVIENDA, new BigDecimal("4500000.00"));
        DEMO_MAX_AMOUNT.put(SubsidyType.EDUCACION, new BigDecimal("1800000.00"));
        DEMO_MAX_AMOUNT.put(SubsidyType.RECREACION, new BigDecimal("900000.00"));
        DEMO_MAX_AMOUNT.put(SubsidyType.CREDITO, new BigDecimal("3000000.00"));

        DEMO_REQUIRED_DOCUMENTS.put(SubsidyType.VIVIENDA, EnumSet.of(
                DocumentType.ID_DOCUMENT,
                DocumentType.INCOME_CERTIFICATE,
                DocumentType.BANK_CERTIFICATE));
        DEMO_REQUIRED_DOCUMENTS.put(SubsidyType.EDUCACION, EnumSet.of(
                DocumentType.ID_DOCUMENT,
                DocumentType.INCOME_CERTIFICATE,
                DocumentType.ENROLLMENT_CERTIFICATE));
        DEMO_REQUIRED_DOCUMENTS.put(SubsidyType.RECREACION, EnumSet.of(DocumentType.ID_DOCUMENT));
        DEMO_REQUIRED_DOCUMENTS.put(SubsidyType.CREDITO, EnumSet.of(
                DocumentType.ID_DOCUMENT,
                DocumentType.INCOME_CERTIFICATE,
                DocumentType.BANK_CERTIFICATE));
    }

    /**
     * Returns the maximum amount allowed for a subsidy type under the demonstration rules.
     *
     * <p>The lookup is a constant-time read over an {@link EnumMap}, so the method is cheap enough to
     * be called from validation and from the response mapper without caching. An unknown or missing
     * type yields {@link BigDecimal#ZERO} rather than an exception: the caller is expected to have
     * validated the enum at the boundary, and returning zero makes an eventual programming mistake
     * fail closed (the amount will exceed the limit) instead of granting an unlimited ceiling.
     *
     * @param type subsidy type whose limit is required; it may be {@code null}
     * @return the configured limit in Colombian pesos, or zero when no limit is defined
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public BigDecimal maxAmount(SubsidyType type) {
        return DEMO_MAX_AMOUNT.getOrDefault(type, BigDecimal.ZERO);
    }

    /**
     * Returns the documents required for a subsidy type, as an independent mutable copy.
     *
     * <p>The copy protects the catalogue from callers that add or remove elements while building the
     * persistence rows; mutating the returned set never changes the shared definition. When the type
     * has no catalogue the method returns an empty set, which the consumer interprets as "no evidence
     * required" and lets the request move forward to review without blocking the flow on a
     * configuration mistake.
     *
     * @param type subsidy type whose document catalogue is required; it may be {@code null}
     * @return a new set with the required document types, empty when the type is unknown
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Set<DocumentType> requiredDocuments(SubsidyType type) {
        return EnumSet.copyOf(DEMO_REQUIRED_DOCUMENTS.getOrDefault(type, EnumSet.noneOf(DocumentType.class)));
    }

    /**
     * Applies the demonstration eligibility rules that do not need database access.
     *
     * <p>The method validates that a type is present, that the amount is strictly positive and within
     * the configured ceiling, and that a credit request comes from an affiliate in category A or B.
     * Each violation raises a {@link BusinessRuleException} with a stable code so the portal can
     * explain the outcome without parsing the message. The method is side-effect free, which makes the
     * rules trivially unit-testable and safe to call before opening a transaction.
     *
     * @param affiliateCategory category of the affiliate, used only by the credit rule
     * @param type              subsidy type requested by the affiliate
     * @param amount            amount requested in Colombian pesos
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public void validateSubmission(String affiliateCategory, SubsidyType type, BigDecimal amount) {
        if (type == null) {
            throw new BusinessRuleException("SUBSIDY_TYPE_REQUIRED", "The subsidy type is required");
        }
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("INVALID_AMOUNT", "The requested amount must be greater than zero");
        }
        BigDecimal max = maxAmount(type);
        if (amount.compareTo(max) > 0) {
            throw new BusinessRuleException("AMOUNT_ABOVE_DEMO_LIMIT",
                    "The requested amount exceeds the demonstration limit of " + max + " COP for " + type);
        }
        if (type == SubsidyType.CREDITO && !isCategoryAtLeastB(affiliateCategory)) {
            throw new BusinessRuleException("CATEGORY_NOT_ELIGIBLE",
                    "Demonstration rule: credit subsidies require category A or B");
        }
    }

    /**
     * Evaluates whether a category is A or B under the demonstration catalogue.
     *
     * <p>Categories are compared ignoring case because the value comes from a legacy column that was
     * not normalised, and a difference in capitalisation must not change an eligibility decision. The
     * method is private because the predicate is an implementation detail of the credit rule; exposing
     * it would invite callers to duplicate the eligibility logic outside this component.
     *
     * @param category category code stored for the affiliate; it may be {@code null}
     * @return {@code true} when the category is A or B, {@code false} otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private boolean isCategoryAtLeastB(String category) {
        return "A".equalsIgnoreCase(category) || "B".equalsIgnoreCase(category);
    }
}
