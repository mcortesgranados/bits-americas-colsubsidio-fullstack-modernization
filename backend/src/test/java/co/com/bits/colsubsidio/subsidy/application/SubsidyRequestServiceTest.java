package co.com.bits.colsubsidio.subsidy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import co.com.bits.colsubsidio.affiliate.domain.AffiliateRepository;
import co.com.bits.colsubsidio.affiliate.domain.BeneficiaryRepository;
import co.com.bits.colsubsidio.common.api.PageResponse;
import co.com.bits.colsubsidio.messaging.application.OutboxService;
import co.com.bits.colsubsidio.security.AccessGuard;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestSummaryResponse;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistoryRepository;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocumentRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Unit tests for {@link SubsidyRequestService}.
 *
 * <p>The tests exercise the two responsibilities of the service without a database: the composition of
 * the specification and the translation of the page of entities into the transport envelope. The
 * repository is replaced by a double, so a failure points at the service and never at the persistence
 * layer.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@ExtendWith(MockitoExtension.class)
class SubsidyRequestServiceTest {

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

    @org.mockito.Spy
    private SubsidyDemoRules rules = new SubsidyDemoRules();

    @InjectMocks
    private SubsidyRequestService service;

    /**
     * Verifies that a page of entities becomes a page of summaries with the same metadata.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("maps the page of entities into the transport envelope")
    void mapsPageOfEntities() {
        SubsidyRequest request = org.mockito.Mockito.mock(SubsidyRequest.class);
        when(request.getRequestNumber()).thenReturn("SOL-2026-0001");
        when(request.getSubsidyType()).thenReturn(SubsidyType.EDUCACION);
        when(request.getStatus()).thenReturn(SubsidyRequestStatus.IN_REVIEW);
        when(request.getRequestedAmount()).thenReturn(new BigDecimal("2500000.00"));
        when(request.getCurrency()).thenReturn("COP");
        when(request.getSubmittedAt()).thenReturn(Instant.parse("2026-10-02T19:00:00Z"));

        Pageable pageable = PageRequest.of(0, 20);
        when(requests.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(request), pageable, 1));

        PageResponse<SubsidyRequestSummaryResponse> page =
                service.listForAffiliate(1L, SubsidyRequestStatus.IN_REVIEW, pageable);

        assertThat(page.content()).hasSize(1);
        assertThat(page.content().get(0).requestNumber()).isEqualTo("SOL-2026-0001");
        assertThat(page.content().get(0).status()).isEqualTo(SubsidyRequestStatus.IN_REVIEW);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    /**
     * Verifies that an empty result is returned as an empty page and not as an error.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("returns an empty page when the affiliate has no requests")
    void returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(requests.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        PageResponse<SubsidyRequestSummaryResponse> page = service.listForAffiliate(99L, null, pageable);

        assertThat(page.content()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }
}
