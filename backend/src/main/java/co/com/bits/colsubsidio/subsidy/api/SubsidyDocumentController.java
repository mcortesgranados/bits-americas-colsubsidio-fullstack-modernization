package co.com.bits.colsubsidio.subsidy.api;

import co.com.bits.colsubsidio.subsidy.api.dto.DocumentSummaryResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.DocumentUploadResponse;
import co.com.bits.colsubsidio.subsidy.application.DocumentContent;
import co.com.bits.colsubsidio.subsidy.application.SubsidyDocumentService;
import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * HTTP entry point for the evidence of a subsidy request (UC-04).
 *
 * <p>Three operations share the path of the request they belong to: list the catalogue, upload one document
 * and download a stored one. Uploads are reserved for the affiliate role because the evidence is provided by
 * the holder; downloads are open to the staff roles as well, since the back-office needs to review the file.
 * In both cases the application service verifies that the caller is allowed to touch that specific request,
 * so the role check alone is never the only barrier.
 *
 * <p>The controller maps the service result to HTTP: a 201 with the stored metadata for an upload, a stream
 * with {@code Content-Disposition} for a download. Sizes and MIME types are validated in the service, which
 * keeps the multipart parsing concerns out of this class.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 11:20 GMT-5 Bogotá D.C. Colombia
 */
@RestController
@RequestMapping("/api/v1/subsidy-requests/{requestId}/documents")
@Tag(name = "Subsidy documents", description = "Evidence required to review a subsidy request")
@SecurityRequirement(name = "bearer-jwt")
public class SubsidyDocumentController {

    private final SubsidyDocumentService service;

    /**
     * Receives the application service that owns the upload rules.
     *
     * @param service application service; never {@code null}
     */
    public SubsidyDocumentController(SubsidyDocumentService service) {
        this.service = service;
    }

    /**
     * Lists the documents required for the request with their current state.
     *
     * @param requestId identifier of the request
     * @return one entry per document of the catalogue, ordered by type
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('AFFILIATE','BACKOFFICE','AUDITOR')")
    @Operation(summary = "List the documents of a subsidy request")
    public List<DocumentSummaryResponse> list(@PathVariable Long requestId) {
        return service.list(requestId);
    }

    /**
     * Stores one document of the request.
     *
     * @param requestId    identifier of the request
     * @param documentType type from the catalogue, sent as a form field
     * @param file         file part named {@code file}
     * @return 201 with the stored metadata and whether it replaced a previous file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('AFFILIATE')")
    @Operation(summary = "Upload or replace one required document")
    public ResponseEntity<DocumentUploadResponse> upload(@PathVariable Long requestId,
                                                         @RequestParam("documentType") DocumentType documentType,
                                                         @RequestPart("file") MultipartFile file) {
        DocumentUploadResponse response = service.upload(requestId, documentType, file);
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Downloads a stored document.
     *
     * @param requestId    identifier of the request
     * @param documentType type to download
     * @return the file stream with its content type and an attachment disposition
     */
    @GetMapping("/{documentType}/content")
    @PreAuthorize("hasAnyRole('AFFILIATE','BACKOFFICE','AUDITOR')")
    @Operation(summary = "Download a stored document")
    public ResponseEntity<Resource> content(@PathVariable Long requestId,
                                            @PathVariable DocumentType documentType) {
        DocumentContent content = service.download(requestId, documentType);
        String fileName = content.fileName() == null ? documentType.name() : content.fileName();
        MediaType mediaType = content.contentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(content.contentType());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new InputStreamResource(content.stream()));
    }
}
