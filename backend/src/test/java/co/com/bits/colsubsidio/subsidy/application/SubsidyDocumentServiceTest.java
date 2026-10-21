package co.com.bits.colsubsidio.subsidy.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import co.com.bits.colsubsidio.audit.application.AuditService;
import co.com.bits.colsubsidio.audit.domain.AuditActions;
import co.com.bits.colsubsidio.common.error.BusinessRuleException;
import co.com.bits.colsubsidio.common.error.PayloadTooLargeException;
import co.com.bits.colsubsidio.config.StorageProperties;
import co.com.bits.colsubsidio.messaging.application.OutboxService;
import co.com.bits.colsubsidio.messaging.event.SubsidyDocumentUploadedEvent;
import co.com.bits.colsubsidio.security.AccessGuard;
import co.com.bits.colsubsidio.storage.DocumentStorage;
import co.com.bits.colsubsidio.storage.StoredDocument;
import co.com.bits.colsubsidio.subsidy.api.dto.DocumentUploadResponse;
import co.com.bits.colsubsidio.subsidy.domain.DocumentStatus;
import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocument;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocumentRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyType;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Unit tests for UC-04 (cargar un documento requerido) on {@link SubsidyDocumentService}.
 *
 * <p>The storage port is a double, so the tests describe the decisions of the service and never touch the
 * filesystem. The demonstration catalogue is the real component, which is what makes the
 * {@code DOCUMENT_NOT_REQUIRED} assertion meaningful. The failure cases are the interesting ones here: a
 * rejected upload must not write a file, and a failure after the file was written must delete it.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 11:20 GMT-5 Bogotá D.C. Colombia
 */
@ExtendWith(MockitoExtension.class)
class SubsidyDocumentServiceTest {

    private static final long MAX_SIZE = 5_242_880L;

    @Mock
    private SubsidyRequestRepository requests;

    @Mock
    private RequiredDocumentRepository documents;

    @Mock
    private DocumentStorage storage;

    @Mock
    private OutboxService outbox;

    @Mock
    private AuditService audit;

    @Mock
    private AccessGuard accessGuard;

    private SubsidyDocumentService service;

    @BeforeEach
    void setUp() {
        StorageProperties properties = new StorageProperties("local", MAX_SIZE,
                List.of("application/pdf", "image/png", "image/jpeg"),
                new StorageProperties.Local("./target/document-storage"),
                new StorageProperties.Azure(null, null));
        service = new SubsidyDocumentService(requests, documents, storage, properties, outbox, audit,
                new SubsidyDemoRules(), accessGuard);
    }

    @Test
    @DisplayName("stores the file, marks the document as received and publishes the event")
    void uploadsDocument() {
        SubsidyRequest request = request(SubsidyRequestStatus.PENDING_DOCUMENTS, SubsidyType.VIVIENDA);
        when(requests.findById(42L)).thenReturn(Optional.of(request));
        RequiredDocument document = document(null);
        when(documents.findByRequestIdAndDocumentType(42L, DocumentType.INCOME_CERTIFICATE))
                .thenReturn(Optional.of(document));
        StoredDocument stored = new StoredDocument("42/INCOME_CERTIFICATE-abc.pdf", "certificado.pdf",
                "application/pdf", 120L, "checksum-abc");
        when(storage.store(eq(42L), eq(DocumentType.INCOME_CERTIFICATE), any())).thenReturn(stored);
        when(documents.save(document)).thenReturn(document);
        MultipartFile file = file("certificado.pdf", "application/pdf", "contenido");

        DocumentUploadResponse response = service.upload(42L, DocumentType.INCOME_CERTIFICATE, file);

        verify(accessGuard).requireAffiliateAccess(1L);
        verify(document).markReceived(eq("certificado.pdf"), eq("42/INCOME_CERTIFICATE-abc.pdf"),
                eq("application/pdf"), eq(120L), eq("checksum-abc"), any(Instant.class));
        verify(outbox).append(eq(SubsidyDocumentUploadedEvent.EVENT_TYPE), eq("SubsidyRequest"), eq("42"),
                any(SubsidyDocumentUploadedEvent.class));
        verify(audit).record(eq(AuditActions.SUBSIDY_DOCUMENT_UPLOADED), eq("SubsidyRequest"), eq("42"),
                contains("sha256=checksum-abc"));
        assertThat(response.documentType()).isEqualTo("INCOME_CERTIFICATE");
        assertThat(response.replaced()).isFalse();
    }

    @Test
    @DisplayName("rejects a document type outside the catalogue without writing anything")
    void rejectsDocumentOutsideCatalogue() {
        givenRequest(SubsidyRequestStatus.PENDING_DOCUMENTS, SubsidyType.RECREACION);

        assertThatThrownBy(() -> service.upload(42L, DocumentType.BANK_CERTIFICATE,
                file("banco.pdf", "application/pdf", "x")))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getCode())
                        .isEqualTo("DOCUMENT_NOT_REQUIRED"));

        verifyNoInteractions(storage, outbox);
    }

    @Test
    @DisplayName("rejects a MIME type outside the accepted list")
    void rejectsMimeType() {
        givenRequest(SubsidyRequestStatus.PENDING_DOCUMENTS, SubsidyType.VIVIENDA);

        assertThatThrownBy(() -> service.upload(42L, DocumentType.INCOME_CERTIFICATE,
                file("notas.txt", "text/plain", "x")))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getCode())
                        .isEqualTo("FILE_TYPE_NOT_ALLOWED"));

        verifyNoInteractions(storage);
    }

    @Test
    @DisplayName("rejects a file above the configured ceiling with a 413 code")
    void rejectsTooLargeFile() {
        givenRequest(SubsidyRequestStatus.PENDING_DOCUMENTS, SubsidyType.VIVIENDA);
        MultipartFile big = mock(MultipartFile.class);
        when(big.isEmpty()).thenReturn(false);
        when(big.getSize()).thenReturn(MAX_SIZE + 1);
        lenient().when(big.getContentType()).thenReturn("application/pdf");

        assertThatThrownBy(() -> service.upload(42L, DocumentType.INCOME_CERTIFICATE, big))
                .isInstanceOf(PayloadTooLargeException.class)
                .satisfies(error -> assertThat(((PayloadTooLargeException) error).getCode())
                        .isEqualTo("FILE_TOO_LARGE"));

        verifyNoInteractions(storage);
    }

    @Test
    @DisplayName("rejects an upload to a request in a terminal status")
    void rejectsTerminalRequest() {
        givenRequest(SubsidyRequestStatus.APPROVED_DEMO, SubsidyType.VIVIENDA);

        assertThatThrownBy(() -> service.upload(42L, DocumentType.INCOME_CERTIFICATE,
                file("certificado.pdf", "application/pdf", "x")))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getCode())
                        .isEqualTo("INVALID_STATUS_TRANSITION"));

        verifyNoInteractions(storage);
    }

    @Test
    @DisplayName("keeps the previous checksum in the audit trail when a file is replaced")
    void auditsReplacement() {
        givenRequest(SubsidyRequestStatus.PENDING_DOCUMENTS, SubsidyType.VIVIENDA);
        RequiredDocument document = document("checksum-old");
        when(documents.findByRequestIdAndDocumentType(42L, DocumentType.INCOME_CERTIFICATE))
                .thenReturn(Optional.of(document));
        when(storage.store(eq(42L), eq(DocumentType.INCOME_CERTIFICATE), any()))
                .thenReturn(new StoredDocument("k", "n.pdf", "application/pdf", 10L, "checksum-new"));
        when(documents.save(document)).thenReturn(document);

        DocumentUploadResponse response = service.upload(42L, DocumentType.INCOME_CERTIFICATE,
                file("n.pdf", "application/pdf", "x"));

        assertThat(response.replaced()).isTrue();
        ArgumentCaptor<String> detail = ArgumentCaptor.forClass(String.class);
        verify(audit).record(eq(AuditActions.SUBSIDY_DOCUMENT_UPLOADED), anyString(), anyString(),
                detail.capture());
        assertThat(detail.getValue()).contains("replaced sha256=checksum-old");
    }

    @Test
    @DisplayName("deletes the stored file when the transaction body fails afterwards")
    void deletesFileOnFailure() {
        givenRequest(SubsidyRequestStatus.PENDING_DOCUMENTS, SubsidyType.VIVIENDA);
        RequiredDocument document = document(null);
        when(documents.findByRequestIdAndDocumentType(42L, DocumentType.INCOME_CERTIFICATE))
                .thenReturn(Optional.of(document));
        when(storage.store(eq(42L), eq(DocumentType.INCOME_CERTIFICATE), any()))
                .thenReturn(new StoredDocument("orphan.pdf", "n.pdf", "application/pdf", 10L, "c"));
        when(documents.save(document)).thenReturn(document);
        doThrow(new IllegalStateException("outbox down")).when(outbox)
                .append(anyString(), anyString(), anyString(), any());

        assertThatThrownBy(() -> service.upload(42L, DocumentType.INCOME_CERTIFICATE,
                file("n.pdf", "application/pdf", "x")))
                .isInstanceOf(IllegalStateException.class);

        verify(storage).delete("orphan.pdf");
    }

    @Test
    @DisplayName("does not serve the content of another affiliate's document")
    void deniesDownloadOfForeignDocument() {
        givenRequest(SubsidyRequestStatus.IN_REVIEW, SubsidyType.VIVIENDA);
        doThrow(new AccessDeniedException("not the holder")).when(accessGuard).requireAffiliateAccess(1L);

        assertThatThrownBy(() -> service.download(42L, DocumentType.ID_DOCUMENT))
                .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(storage);
    }

    @Test
    @DisplayName("serves the stored content with its metadata")
    void downloadsDocument() {
        givenRequest(SubsidyRequestStatus.IN_REVIEW, SubsidyType.VIVIENDA);
        RequiredDocument document = document("checksum");
        lenient().when(document.getStatus()).thenReturn(DocumentStatus.RECEIVED);
        when(document.getStorageKey()).thenReturn("42/ID_DOCUMENT-abc.pdf");
        lenient().when(document.getFileName()).thenReturn("documento.pdf");
        when(document.getContentType()).thenReturn("application/pdf");
        when(documents.findByRequestIdAndDocumentType(42L, DocumentType.ID_DOCUMENT))
                .thenReturn(Optional.of(document));
        when(storage.load("42/ID_DOCUMENT-abc.pdf"))
                .thenReturn(new ByteArrayInputStream("hola".getBytes(StandardCharsets.UTF_8)));

        DocumentContent content = service.download(42L, DocumentType.ID_DOCUMENT);

        assertThat(content.fileName()).isEqualTo("documento.pdf");
        assertThat(content.contentType()).isEqualTo("application/pdf");
        assertThat(content.stream()).hasContent("hola");
    }

    private SubsidyRequest givenRequest(SubsidyRequestStatus status, SubsidyType type) {
        SubsidyRequest request = request(status, type);
        when(requests.findById(42L)).thenReturn(Optional.of(request));
        return request;
    }

    private SubsidyRequest request(SubsidyRequestStatus status, SubsidyType type) {
        SubsidyRequest request = mock(SubsidyRequest.class);
        lenient().when(request.getId()).thenReturn(42L);
        lenient().when(request.getAffiliateId()).thenReturn(1L);
        lenient().when(request.getStatus()).thenReturn(status);
        lenient().when(request.getSubsidyType()).thenReturn(type);
        return request;
    }

    private RequiredDocument document(String checksum) {
        RequiredDocument document = mock(RequiredDocument.class);
        lenient().when(document.getId()).thenReturn(7L);
        lenient().when(document.getDocumentType()).thenReturn(DocumentType.INCOME_CERTIFICATE);
        lenient().when(document.getStatus()).thenReturn(DocumentStatus.RECEIVED);
        lenient().when(document.getChecksum()).thenReturn(checksum);
        lenient().when(document.getFileName()).thenReturn("certificado.pdf");
        lenient().when(document.getSizeBytes()).thenReturn(120L);
        lenient().when(document.getUploadedAt()).thenReturn(Instant.parse("2026-10-21T15:30:00Z"));
        return document;
    }

    private MultipartFile file(String name, String contentType, String content) {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getSize()).thenReturn((long) content.length());
        lenient().when(file.getContentType()).thenReturn(contentType);
        lenient().when(file.getOriginalFilename()).thenReturn(name);
        return file;
    }
}
