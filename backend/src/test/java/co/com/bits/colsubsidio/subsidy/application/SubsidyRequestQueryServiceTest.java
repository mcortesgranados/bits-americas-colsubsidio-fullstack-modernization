package co.com.bits.colsubsidio.subsidy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.com.bits.colsubsidio.common.error.NotFoundException;
import co.com.bits.colsubsidio.security.AccessGuard;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestDetailResponse;
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
import java.time.Instant;
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
 * Unit tests for UC-05 (detalle, historial y replay del stream) on {@link SubsidyRequestQueryService}.
 *
 * <p>They pin the two properties the portal relies on: the counts of pending and rejected documents are
 * consistent with the list that is returned, and the forbidden case never reaches the repositories, so a 403
 * cannot leak whether the request exists.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 12:10 GMT-5 Bogotá D.C. Colombia
 */
@ExtendWith(MockitoExtension.class)
class SubsidyRequestQueryServiceTest {

    @Mock
    private SubsidyRequestRepository requests;

    @Mock
    private RequiredDocumentRepository documents;

    @Mock
    private RequestStatusHistoryRepository history;

    @Mock
    private AccessGuard accessGuard;

    @InjectMocks
    private SubsidyRequestQueryService service;

    @Test
    @DisplayName("returns the detail with documents, timeline and the pending counts")
    void returnsDetail() {
        SubsidyRequest request = request();
        RequiredDocument idDocument = document(DocumentType.ID_DOCUMENT, DocumentStatus.RECEIVED);
        RequiredDocument income = document(DocumentType.INCOME_CERTIFICATE, DocumentStatus.PENDING);
        RequiredDocument bank = document(DocumentType.BANK_CERTIFICATE, DocumentStatus.REJECTED);
        RequestStatusHistory first = entry(9L);
        when(requests.findById(42L)).thenReturn(Optional.of(request));
        when(documents.findByRequestIdOrderByDocumentTypeAsc(42L)).thenReturn(List.of(idDocument, income, bank));
        when(history.findByRequestIdOrderByOccurredAtAscIdAsc(42L)).thenReturn(List.of(first));

        SubsidyRequestDetailResponse detail = service.detail(42L);

        assertThat(detail.id()).isEqualTo(42L);
        assertThat(detail.requestNumber()).isEqualTo("SOL-2026-0005");
        assertThat(detail.status()).isEqualTo("IN_REVIEW");
        assertThat(detail.pendingDocuments()).isEqualTo(1);
        assertThat(detail.rejectedDocuments()).isEqualTo(1);
        assertThat(detail.documents()).extracting("documentType")
                .containsExactly("ID_DOCUMENT", "INCOME_CERTIFICATE", "BANK_CERTIFICATE");
        assertThat(detail.history()).hasSize(1);
        assertThat(detail.history().get(0).toStatus()).isEqualTo("SUBMITTED");
        verify(accessGuard).requireAffiliateAccess(1L);
    }

    @Test
    @DisplayName("fails with not found when the request does not exist")
    void failsWhenMissing() {
        when(requests.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.detail(99L)).isInstanceOf(NotFoundException.class);

        verifyNoInteractions(documents, history);
    }

    @Test
    @DisplayName("does not read anything when the caller has no access")
    void deniesForeignRequest() {
        SubsidyRequest request = request();
        when(requests.findById(42L)).thenReturn(Optional.of(request));
        doThrow(new AccessDeniedException("not the holder")).when(accessGuard).requireAffiliateAccess(1L);

        assertThatThrownBy(() -> service.detail(42L)).isInstanceOf(AccessDeniedException.class);

        verify(documents, never()).findByRequestIdOrderByDocumentTypeAsc(any());
        verifyNoInteractions(history);
    }

    @Test
    @DisplayName("replays the whole timeline when there is no Last-Event-ID")
    void replaysFromTheBeginning() {
        SubsidyRequest request = request();
        RequestStatusHistory first = entry(9L);
        when(requests.findById(42L)).thenReturn(Optional.of(request));
        when(history.findByRequestIdOrderByOccurredAtAscIdAsc(42L)).thenReturn(List.of(first));

        assertThat(service.replay(42L, null)).hasSize(1);
        verify(history, never()).findByRequestIdAndIdGreaterThanOrderByIdAsc(any(), any());
    }

    @Test
    @DisplayName("replays only the transitions after the last event received")
    void replaysAfterLastEvent() {
        SubsidyRequest request = request();
        RequestStatusHistory second = entry(10L);
        when(requests.findById(42L)).thenReturn(Optional.of(request));
        when(history.findByRequestIdAndIdGreaterThanOrderByIdAsc(42L, 9L)).thenReturn(List.of(second));

        assertThat(service.replay(42L, 9L)).hasSize(1);
    }

    private SubsidyRequest request() {
        SubsidyRequest request = mock(SubsidyRequest.class);
        lenient().when(request.getId()).thenReturn(42L);
        lenient().when(request.getAffiliateId()).thenReturn(1L);
        lenient().when(request.getRequestNumber()).thenReturn("SOL-2026-0005");
        lenient().when(request.getSubsidyType()).thenReturn(SubsidyType.VIVIENDA);
        lenient().when(request.getStatus()).thenReturn(SubsidyRequestStatus.IN_REVIEW);
        lenient().when(request.getRequestedAmount()).thenReturn(new BigDecimal("4000000.00"));
        lenient().when(request.getCurrency()).thenReturn("COP");
        lenient().when(request.getPurpose()).thenReturn("Arriendo");
        lenient().when(request.getSubmittedAt()).thenReturn(Instant.parse("2026-10-21T15:00:00Z"));
        return request;
    }

    private RequiredDocument document(DocumentType type, DocumentStatus status) {
        RequiredDocument document = mock(RequiredDocument.class);
        lenient().when(document.getId()).thenReturn((long) type.ordinal() + 1);
        lenient().when(document.getDocumentType()).thenReturn(type);
        lenient().when(document.getStatus()).thenReturn(status);
        lenient().when(document.getFileName()).thenReturn(type.name() + ".pdf");
        lenient().when(document.getSizeBytes()).thenReturn(1024L);
        lenient().when(document.getChecksum()).thenReturn("checksum");
        lenient().when(document.getUploadedAt()).thenReturn(Instant.parse("2026-10-21T15:30:00Z"));
        return document;
    }

    private RequestStatusHistory entry(Long id) {
        RequestStatusHistory entry = mock(RequestStatusHistory.class);
        lenient().when(entry.getId()).thenReturn(id);
        lenient().when(entry.getFromStatus()).thenReturn(SubsidyRequestStatus.DRAFT);
        lenient().when(entry.getToStatus()).thenReturn(SubsidyRequestStatus.SUBMITTED);
        lenient().when(entry.getReason()).thenReturn("Solicitud registrada por el afiliado");
        lenient().when(entry.getActor()).thenReturn("afiliado.demo");
        lenient().when(entry.getOccurredAt()).thenReturn(Instant.parse("2026-10-21T15:00:00Z"));
        lenient().when(entry.getCorrelationId()).thenReturn("corr-1");
        return entry;
    }
}
