package co.com.bits.colsubsidio.subsidy.application;

import co.com.bits.colsubsidio.audit.application.AuditService;
import co.com.bits.colsubsidio.audit.domain.AuditActions;
import co.com.bits.colsubsidio.common.error.BusinessRuleException;
import co.com.bits.colsubsidio.common.error.NotFoundException;
import co.com.bits.colsubsidio.common.error.PayloadTooLargeException;
import co.com.bits.colsubsidio.config.StorageProperties;
import co.com.bits.colsubsidio.messaging.application.OutboxService;
import co.com.bits.colsubsidio.messaging.event.SubsidyDocumentUploadedEvent;
import co.com.bits.colsubsidio.security.AccessGuard;
import co.com.bits.colsubsidio.storage.DocumentStorage;
import co.com.bits.colsubsidio.storage.StoredDocument;
import co.com.bits.colsubsidio.subsidy.api.dto.DocumentSummaryResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.DocumentUploadResponse;
import co.com.bits.colsubsidio.subsidy.domain.DocumentStatus;
import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocument;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocumentRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Application service of the subsidy evidence: it lists, receives and serves the documents of one request
 * (UC-04).
 *
 * <p>Every method resolves the request first and delegates the ownership decision to {@link AccessGuard}, so
 * an affiliate can only see and upload documents of its own requests while staff can read across affiliates.
 * On top of that, uploads check the request is still open, the document type belongs to the catalogue of the
 * subsidy type, and the file matches the accepted MIME list and the configured size ceiling. Each rejection
 * carries a stable code the portal translates; nothing is written when a check fails.
 *
 * <p>The file is written before the row is updated, because the adapter streams the content and computes the
 * checksum while copying. If the transaction then fails, the stored file is deleted in the catch block so a
 * rollback does not leave an orphan object behind. The opposite order would leave a row pointing to a file
 * that does not exist, which is harder to detect and breaks the download.
 *
 * <p>The upload also appends {@code subsidy.document.uploaded} to the transactional outbox. The consumer
 * recalculates completeness and moves the request to {@code IN_REVIEW} when no evidence is missing, which is
 * why the upload itself never changes the request status.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 11:20 GMT-5 Bogotá D.C. Colombia
 */
@Service
public class SubsidyDocumentService {

    private final SubsidyRequestRepository requests;
    private final RequiredDocumentRepository documents;
    private final DocumentStorage storage;
    private final StorageProperties storageProperties;
    private final OutboxService outbox;
    private final AuditService audit;
    private final SubsidyDemoRules rules;
    private final AccessGuard accessGuard;

    /**
     * Creates the service with the collaborators it needs.
     *
     * @param requests          repository of the subsidy requests
     * @param documents         repository of the required documents
     * @param storage           port of the file storage (local or Azure)
     * @param storageProperties accepted MIME types and size ceiling
     * @param outbox            transactional outbox for the uploaded event
     * @param audit             audit trail
     * @param rules             catalogue of documents per subsidy type
     * @param accessGuard       object-level authorization and access to the principal
     */
    public SubsidyDocumentService(SubsidyRequestRepository requests, RequiredDocumentRepository documents,
                                  DocumentStorage storage, StorageProperties storageProperties, OutboxService outbox,
                                  AuditService audit, SubsidyDemoRules rules, AccessGuard accessGuard) {
        this.requests = requests;
        this.documents = documents;
        this.storage = storage;
        this.storageProperties = storageProperties;
        this.outbox = outbox;
        this.audit = audit;
        this.rules = rules;
        this.accessGuard = accessGuard;
    }

    /**
     * Lists the documents required for a request, ordered by type.
     *
     * @param requestId request whose documents are listed
     * @return every document of the catalogue with its current state
     */
    @Transactional(readOnly = true)
    public List<DocumentSummaryResponse> list(Long requestId) {
        SubsidyRequest request = requireRequest(requestId);
        accessGuard.requireAffiliateAccess(request.getAffiliateId());
        return documents.findByRequestIdOrderByDocumentTypeAsc(requestId).stream()
                .map(DocumentSummaryResponse::from)
                .toList();
    }

    /**
     * Stores one document of the request and marks it as received.
     *
     * <p>The method never changes the status of the request: completeness is decided by the asynchronous
     * consumer after the event, so two uploads in parallel cannot both move the request forward.
     *
     * @param requestId    request that receives the document
     * @param documentType type chosen by the affiliate
     * @param file         uploaded file, already parsed by Spring
     * @return metadata of the stored file and whether it replaced a previous one
     * @throws NotFoundException        when the request or the catalogue row does not exist
     * @throws BusinessRuleException    when the request is closed, the type is not required or the MIME is
     *                                  not accepted
     * @throws PayloadTooLargeException when the file exceeds the configured ceiling
     */
    @Transactional
    public DocumentUploadResponse upload(Long requestId, DocumentType documentType, MultipartFile file) {
        SubsidyRequest request = requireRequest(requestId);
        accessGuard.requireAffiliateAccess(request.getAffiliateId());

        if (!request.getStatus().isOpen()) {
            throw new BusinessRuleException("INVALID_STATUS_TRANSITION",
                    "Documents cannot be uploaded to a request in status " + request.getStatus());
        }
        if (!rules.requiredDocuments(request.getSubsidyType()).contains(documentType)) {
            throw new BusinessRuleException("DOCUMENT_NOT_REQUIRED",
                    "Document " + documentType + " is not required for " + request.getSubsidyType());
        }
        validate(file);

        RequiredDocument document = documents.findByRequestIdAndDocumentType(requestId, documentType)
                .orElseThrow(() -> NotFoundException.of("Required document", requestId));
        String previousChecksum = document.getChecksum();

        StoredDocument stored = storage.store(requestId, documentType, file);
        try {
            document.markReceived(stored.fileName(), stored.storageKey(), stored.contentType(), stored.sizeBytes(),
                    stored.checksum(), Instant.now());
            RequiredDocument saved = documents.save(document);

            audit.record(AuditActions.SUBSIDY_DOCUMENT_UPLOADED, "SubsidyRequest", String.valueOf(requestId),
                    auditDetail(documentType, stored, previousChecksum));

            outbox.append(SubsidyDocumentUploadedEvent.EVENT_TYPE, "SubsidyRequest", String.valueOf(requestId),
                    new SubsidyDocumentUploadedEvent(requestId, saved.getId(), documentType, saved.getUploadedAt()));

            return DocumentUploadResponse.from(saved, previousChecksum != null);
        } catch (RuntimeException ex) {
            storage.delete(stored.storageKey());
            throw ex;
        }
    }

    /**
     * Opens the stored content of one document for download.
     *
     * @param requestId    request that owns the document
     * @param documentType type to download
     * @return the stream and the metadata required to build the response
     */
    @Transactional(readOnly = true)
    public DocumentContent download(Long requestId, DocumentType documentType) {
        SubsidyRequest request = requireRequest(requestId);
        accessGuard.requireAffiliateAccess(request.getAffiliateId());

        RequiredDocument document = documents.findByRequestIdAndDocumentType(requestId, documentType)
                .orElseThrow(() -> NotFoundException.of("Required document", requestId));
        if (document.getStatus() == DocumentStatus.PENDING || document.getStorageKey() == null) {
            throw new BusinessRuleException("DOCUMENT_NOT_UPLOADED",
                    "Document " + documentType + " has not been uploaded yet");
        }
        return new DocumentContent(storage.load(document.getStorageKey()), document.getFileName(),
                document.getContentType());
    }

    /**
     * Validates the uploaded file against the configured MIME list and size ceiling.
     *
     * @param file uploaded file
     */
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("EMPTY_FILE", "The uploaded file is empty");
        }
        if (file.getSize() > storageProperties.maxSizeBytes()) {
            throw new PayloadTooLargeException("FILE_TOO_LARGE",
                    "The maximum accepted size is " + storageProperties.maxSizeBytes() + " bytes");
        }
        String contentType = baseContentType(file.getContentType());
        boolean accepted = storageProperties.allowedContentTypes().stream()
                .map(this::baseContentType)
                .anyMatch(allowed -> allowed.equals(contentType));
        if (!accepted) {
            throw new BusinessRuleException("FILE_TYPE_NOT_ALLOWED",
                    "Accepted types are " + String.join(", ", storageProperties.allowedContentTypes()));
        }
    }

    /**
     * Normalises a MIME type: lowercase and without parameters such as {@code ; charset=...}.
     *
     * @param contentType raw header value; it may be {@code null}
     * @return the normalised media type, or an empty string when absent
     */
    private String baseContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int separator = contentType.indexOf(';');
        String base = separator >= 0 ? contentType.substring(0, separator) : contentType;
        return base.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Builds the audit detail without the file name, which may contain personal data.
     *
     * @param type             document type received
     * @param stored           metadata of the stored file
     * @param previousChecksum checksum of the replaced file, {@code null} on a first upload
     * @return a detail line suitable for the audit trail
     */
    private String auditDetail(DocumentType type, StoredDocument stored, String previousChecksum) {
        String detail = "Document " + type + " received (" + stored.sizeBytes() + " bytes, sha256="
                + stored.checksum() + ")";
        return previousChecksum == null ? detail : detail + "; replaced sha256=" + previousChecksum;
    }

    /**
     * Loads the request or fails with a 404.
     *
     * @param requestId identifier supplied by the client
     * @return the persisted request
     */
    private SubsidyRequest requireRequest(Long requestId) {
        return requests.findById(requestId)
                .orElseThrow(() -> NotFoundException.of("Subsidy request", requestId));
    }
}
