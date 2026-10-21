package co.com.bits.colsubsidio.subsidy.api.dto;

import co.com.bits.colsubsidio.subsidy.domain.RequiredDocument;
import java.time.Instant;

/**
 * Result of uploading or replacing one required document.
 *
 * <p>It repeats the metadata of the stored file so the portal can confirm what was received without a second
 * request, and adds {@code replaced} to distinguish the first upload from a controlled replacement (alternate
 * flow A3). The previous checksum is not returned: it stays in the audit trail, where the operator can find
 * it, and not in the response, which the affiliate could confuse with the current one.
 *
 * @param id          identifier of the document row
 * @param documentType type from the catalogue
 * @param status      status after the upload, always {@code RECEIVED}
 * @param fileName    sanitised original name
 * @param contentType MIME type accepted by the server
 * @param sizeBytes   size in bytes
 * @param checksum    SHA-256 of the stored file
 * @param uploadedAt  instant of the upload
 * @param replaced    {@code true} when a previous file for the same document existed
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 11:20 GMT-5 Bogotá D.C. Colombia
 */
public record DocumentUploadResponse(
        Long id,
        String documentType,
        String status,
        String fileName,
        String contentType,
        Long sizeBytes,
        String checksum,
        Instant uploadedAt,
        boolean replaced) {

    /**
     * Maps the persisted document and the replacement flag to the transport record.
     *
     * @param document persisted document already marked as received
     * @param replaced whether a previous file existed for the same type
     * @return the representation returned with a 201
     */
    public static DocumentUploadResponse from(RequiredDocument document, boolean replaced) {
        return new DocumentUploadResponse(
                document.getId(),
                document.getDocumentType().name(),
                document.getStatus().name(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getChecksum(),
                document.getUploadedAt(),
                replaced);
    }
}
