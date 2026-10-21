package co.com.bits.colsubsidio.affiliate.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.com.bits.colsubsidio.affiliate.api.dto.BeneficiaryResponse;
import co.com.bits.colsubsidio.affiliate.domain.Affiliate;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateRepository;
import co.com.bits.colsubsidio.affiliate.domain.Beneficiary;
import co.com.bits.colsubsidio.affiliate.domain.BeneficiaryRepository;
import co.com.bits.colsubsidio.affiliate.domain.Relationship;
import co.com.bits.colsubsidio.audit.application.AuditService;
import co.com.bits.colsubsidio.audit.domain.AuditActions;
import co.com.bits.colsubsidio.common.error.NotFoundException;
import co.com.bits.colsubsidio.company.domain.CompanyRepository;
import co.com.bits.colsubsidio.notification.application.NotificationService;
import co.com.bits.colsubsidio.security.AccessGuard;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

/**
 * Unit tests for UC-02 (consultar beneficiarios) on {@link AffiliateService}.
 *
 * <p>The repository is replaced by a double, so a failure points at the service and never at the
 * persistence layer. The audit collaborator is verified explicitly because the trail is part of the
 * contract: a read of third-party personal data must leave a record, and a rejected attempt must leave a
 * different one. No test asserts on the raw document number, which is the value the endpoint must never
 * expose.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 09:10 GMT-5 Bogotá D.C. Colombia
 */
@ExtendWith(MockitoExtension.class)
class AffiliateServiceBeneficiariesTest {

    @Mock
    private AffiliateRepository affiliates;

    @Mock
    private BeneficiaryRepository beneficiaries;

    @Mock
    private CompanyRepository companies;

    @Mock
    private NotificationService notifications;

    @Mock
    private AccessGuard accessGuard;

    @Mock
    private AuditService audit;

    @InjectMocks
    private AffiliateService service;

    @Test
    @DisplayName("returns the beneficiaries sorted by name with masked documents")
    void returnsBeneficiariesWithMaskedDocuments() {
        when(affiliates.findById(1L)).thenReturn(Optional.of(mock(Affiliate.class)));
        Beneficiary julian = beneficiary(2L, "Julián Rojas Rojas", Relationship.HIJO, "1025304050",
                LocalDate.of(2018, 9, 30));
        Beneficiary sofia = beneficiary(1L, "Sofía Rojas Rojas", Relationship.HIJO, "1015203040",
                LocalDate.of(2015, 2, 18));
        when(beneficiaries.findByAffiliateIdOrderByFullNameAsc(1L)).thenReturn(List.of(julian, sofia));

        List<BeneficiaryResponse> result = service.beneficiaries(1L);

        assertThat(result).extracting(BeneficiaryResponse::fullName)
                .containsExactly("Julián Rojas Rojas", "Sofía Rojas Rojas");
        assertThat(result.get(0).documentNumber()).isEqualTo("******4050");
        assertThat(result.get(0).relationship()).isEqualTo("HIJO");
        assertThat(result.get(0).birthDate()).isEqualTo("2018-09-30");
        assertThat(result.get(0).status()).isEqualTo("ACTIVE");
        verify(audit).record(eq(AuditActions.PII_ACCESS), eq("Affiliate"), eq("1"),
                contains("2 record(s)"));
    }

    @Test
    @DisplayName("returns an empty list when the affiliate has no beneficiaries")
    void returnsEmptyList() {
        when(affiliates.findById(2L)).thenReturn(Optional.of(mock(Affiliate.class)));
        when(beneficiaries.findByAffiliateIdOrderByFullNameAsc(2L)).thenReturn(List.of());

        assertThat(service.beneficiaries(2L)).isEmpty();

        verify(audit).record(eq(AuditActions.PII_ACCESS), eq("Affiliate"), eq("2"),
                contains("0 record(s)"));
    }

    @Test
    @DisplayName("rejects a foreign affiliate and audits the denied attempt")
    void rejectsForeignAffiliate() {
        doThrow(new AccessDeniedException("not the holder"))
                .when(accessGuard).requireAffiliateAccess(9L);

        assertThatThrownBy(() -> service.beneficiaries(9L))
                .isInstanceOf(AccessDeniedException.class);

        verify(audit).record(eq(AuditActions.PII_ACCESS_DENIED), eq("Affiliate"), eq("9"),
                anyString());
        verifyNoInteractions(beneficiaries, affiliates);
    }

    @Test
    @DisplayName("resolves the holder from the token for the self-service endpoint")
    void resolvesHolderFromToken() {
        when(accessGuard.requireAffiliateId()).thenReturn(7L);
        when(affiliates.findById(7L)).thenReturn(Optional.of(mock(Affiliate.class)));
        when(beneficiaries.findByAffiliateIdOrderByFullNameAsc(7L)).thenReturn(List.of());

        assertThat(service.myBeneficiaries()).isEmpty();

        verify(accessGuard).requireAffiliateAccess(7L);
    }

    @Test
    @DisplayName("fails with not found when the affiliate does not exist")
    void failsWhenAffiliateDoesNotExist() {
        when(affiliates.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.beneficiaries(99L))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(beneficiaries);
    }

    private Beneficiary beneficiary(Long id, String fullName, Relationship relationship,
                                    String documentNumber, LocalDate birthDate) {
        Beneficiary beneficiary = mock(Beneficiary.class);
        when(beneficiary.getId()).thenReturn(id);
        when(beneficiary.getFullName()).thenReturn(fullName);
        when(beneficiary.getRelationship()).thenReturn(relationship);
        when(beneficiary.getDocumentNumber()).thenReturn(documentNumber);
        when(beneficiary.getBirthDate()).thenReturn(birthDate);
        when(beneficiary.getStatus()).thenReturn("ACTIVE");
        return beneficiary;
    }
}
