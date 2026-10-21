package co.com.bits.colsubsidio.subsidy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.com.bits.colsubsidio.affiliate.domain.Affiliate;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateRepository;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateStatus;
import co.com.bits.colsubsidio.affiliate.domain.BeneficiaryRepository;
import co.com.bits.colsubsidio.common.error.BusinessRuleException;
import co.com.bits.colsubsidio.common.error.ConflictException;
import co.com.bits.colsubsidio.messaging.application.OutboxService;
import co.com.bits.colsubsidio.messaging.event.SubsidyRequestSubmittedEvent;
import co.com.bits.colsubsidio.security.AccessGuard;
import co.com.bits.colsubsidio.security.CurrentUser;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestCreateRequest;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestCreatedResponse;
import co.com.bits.colsubsidio.subsidy.domain.DocumentStatus;
import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistory;
import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistoryRepository;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocument;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocumentRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for UC-03 (registrar una solicitud de subsidio) on {@link SubsidyRequestService}.
 *
 * <p>The repositories and the outbox are doubles, but the demonstration rules are the real component: the
 * point of these tests is to prove that the stable codes the portal translates ({@code
 * AMOUNT_ABOVE_DEMO_LIMIT}, {@code NO_BENEFICIARIES}, {@code CATEGORY_NOT_ELIGIBLE},
 * {@code DUPLICATE_OPEN_REQUEST}, {@code AFFILIATE_NOT_ACTIVE}) come out of the real catalogue, not out of a
 * mock that would agree with any assertion.
 *
 * <p>The happy path verifies the four writes the transaction must produce together: the request in
 * {@code SUBMITTED}, one row per required document, the first timeline entry and the outbox event. A
 * regression in any of them breaks the asynchronous classification that follows.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 10:40 GMT-5 Bogotá D.C. Colombia
 */
@ExtendWith(MockitoExtension.class)
class SubsidyRequestCreationTest {

    @Mock
    private SubsidyRequestRepository requests;

    @Mock
    private AffiliateRepository affiliates;

    @Mock
    private BeneficiaryRepository beneficiaries;

    @Mock
    private RequiredDocumentRepository documents;

    @Mock
    private RequestStatusHistoryRepository history;

    @Mock
    private OutboxService outbox;

    @Mock
    private RequestNumberGenerator numbers;

    @Mock
    private AccessGuard accessGuard;

    private SubsidyRequestService service;

    @BeforeEach
    void setUp() {
        service = new SubsidyRequestService(requests, affiliates, beneficiaries, documents, history, outbox,
                new SubsidyDemoRules(), numbers, accessGuard);
    }

    @Test
    @DisplayName("persists the request, its documents, the first timeline entry and the outbox event")
    void registersRequest() {
        Affiliate affiliate = givenAffiliate(1L, AffiliateStatus.ACTIVE, "A");
        when(affiliate.getCompanyId()).thenReturn(1L);
        when(numbers.next()).thenReturn("SOL-2026-0007");
        when(requests.existsByAffiliateIdAndSubsidyTypeAndStatusIn(eq(1L), eq(SubsidyType.VIVIENDA), any()))
                .thenReturn(false);
        when(requests.save(any(SubsidyRequest.class))).thenAnswer(invocation -> {
            SubsidyRequest saved = invocation.getArgument(0);
            org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 42L);
            return saved;
        });
        when(accessGuard.currentUser()).thenReturn(
                new CurrentUser("afiliado.demo", List.of("ROLE_AFFILIATE"), 1L, null));

        SubsidyRequestCreatedResponse response = service.create(new SubsidyRequestCreateRequest(
                SubsidyType.VIVIENDA, new BigDecimal("4000000.5"), "  Arriendo  "));

        assertThat(response.id()).isEqualTo(42L);
        assertThat(response.requestNumber()).isEqualTo("SOL-2026-0007");
        assertThat(response.status()).isEqualTo("SUBMITTED");
        assertThat(response.requestedAmount()).isEqualByComparingTo("4000000.50");
        assertThat(response.currency()).isEqualTo("COP");
        assertThat(response.submittedAt()).isNotNull();
        assertThat(response.requiredDocuments()).containsExactly(
                DocumentType.BANK_CERTIFICATE, DocumentType.ID_DOCUMENT, DocumentType.INCOME_CERTIFICATE);

        ArgumentCaptor<SubsidyRequest> saved = ArgumentCaptor.forClass(SubsidyRequest.class);
        verify(requests).save(saved.capture());
        assertThat(saved.getValue().getStatus()).isEqualTo(SubsidyRequestStatus.SUBMITTED);
        assertThat(saved.getValue().getPurpose()).isEqualTo("Arriendo");
        assertThat(saved.getValue().getAffiliateId()).isEqualTo(1L);
        assertThat(saved.getValue().getSubmittedAt()).isNotNull();

        ArgumentCaptor<RequiredDocument> rows = ArgumentCaptor.forClass(RequiredDocument.class);
        verify(documents, org.mockito.Mockito.times(3)).save(rows.capture());
        assertThat(rows.getAllValues()).extracting(RequiredDocument::getDocumentType)
                .containsExactly(DocumentType.BANK_CERTIFICATE, DocumentType.ID_DOCUMENT,
                        DocumentType.INCOME_CERTIFICATE);
        assertThat(rows.getAllValues()).extracting(RequiredDocument::getStatus)
                .containsOnly(DocumentStatus.PENDING);
        assertThat(rows.getAllValues()).extracting(RequiredDocument::getRequestId).containsOnly(42L);

        ArgumentCaptor<RequestStatusHistory> timeline = ArgumentCaptor.forClass(RequestStatusHistory.class);
        verify(history).save(timeline.capture());
        assertThat(timeline.getValue().getFromStatus()).isEqualTo(SubsidyRequestStatus.DRAFT);
        assertThat(timeline.getValue().getToStatus()).isEqualTo(SubsidyRequestStatus.SUBMITTED);
        assertThat(timeline.getValue().getActor()).isEqualTo("afiliado.demo");

        verify(outbox).append(eq(SubsidyRequestSubmittedEvent.EVENT_TYPE), eq("SubsidyRequest"), eq("42"),
                any(SubsidyRequestSubmittedEvent.class));
    }

    @Test
    @DisplayName("rejects an amount above the demonstration limit without touching the database")
    void rejectsAmountAboveLimit() {
        givenAffiliate(1L, AffiliateStatus.ACTIVE, "A");

        assertThatThrownBy(() -> service.create(new SubsidyRequestCreateRequest(
                SubsidyType.VIVIENDA, new BigDecimal("4500001"), null)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getCode())
                        .isEqualTo("AMOUNT_ABOVE_DEMO_LIMIT"));

        verifyNoInteractions(documents, history, outbox);
        verify(requests, never()).save(any());
    }

    @Test
    @DisplayName("rejects education without a registered beneficiary")
    void rejectsEducationWithoutBeneficiaries() {
        givenAffiliate(1L, AffiliateStatus.ACTIVE, "A");
        when(beneficiaries.countByAffiliateId(1L)).thenReturn(0L);

        assertThatThrownBy(() -> service.create(new SubsidyRequestCreateRequest(
                SubsidyType.EDUCACION, new BigDecimal("1000000"), null)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getCode())
                        .isEqualTo("NO_BENEFICIARIES"));

        verify(requests, never()).save(any());
    }

    @Test
    @DisplayName("rejects credit for a category below B")
    void rejectsCreditForCategoryC() {
        givenAffiliate(1L, AffiliateStatus.ACTIVE, "C");

        assertThatThrownBy(() -> service.create(new SubsidyRequestCreateRequest(
                SubsidyType.CREDITO, new BigDecimal("2000000"), null)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getCode())
                        .isEqualTo("CATEGORY_NOT_ELIGIBLE"));

        verify(requests, never()).save(any());
    }

    @Test
    @DisplayName("rejects a second open request of the same type")
    void rejectsDuplicateOpenRequest() {
        givenAffiliate(1L, AffiliateStatus.ACTIVE, "A");
        when(requests.existsByAffiliateIdAndSubsidyTypeAndStatusIn(eq(1L), eq(SubsidyType.RECREACION), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(new SubsidyRequestCreateRequest(
                SubsidyType.RECREACION, new BigDecimal("500000"), null)))
                .isInstanceOf(ConflictException.class)
                .satisfies(error -> assertThat(((ConflictException) error).getCode())
                        .isEqualTo("DUPLICATE_OPEN_REQUEST"));

        verify(requests, never()).save(any());
        verifyNoInteractions(outbox);
    }

    @Test
    @DisplayName("rejects a suspended affiliate")
    void rejectsInactiveAffiliate() {
        givenAffiliate(1L, AffiliateStatus.SUSPENDED, "A");

        assertThatThrownBy(() -> service.create(new SubsidyRequestCreateRequest(
                SubsidyType.RECREACION, new BigDecimal("500000"), null)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getCode())
                        .isEqualTo("AFFILIATE_NOT_ACTIVE"));

        verifyNoInteractions(outbox);
    }

    private Affiliate givenAffiliate(Long affiliateId, AffiliateStatus status, String category) {
        when(accessGuard.requireAffiliateId()).thenReturn(affiliateId);
        Affiliate affiliate = mock(Affiliate.class);
        // Lenient because a rule that fails first never reads the category, and the inactive-affiliate test
        // stops before the demonstration rules run at all.
        org.mockito.Mockito.lenient().when(affiliate.getStatus()).thenReturn(status);
        org.mockito.Mockito.lenient().when(affiliate.getCategory()).thenReturn(category);
        when(affiliates.findById(affiliateId)).thenReturn(Optional.of(affiliate));
        return affiliate;
    }
}
