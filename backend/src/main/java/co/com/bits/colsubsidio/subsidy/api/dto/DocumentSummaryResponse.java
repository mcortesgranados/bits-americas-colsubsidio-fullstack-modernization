package co.com.bits.colsubsidio.subsidy.api.dto;

import co.com.bits.colsubsidio.subsidy.domain.RequiredDocument;
import java.time.Instant;

/**
 * State of one required document of a subsidy request.
 *
 * <p>It carries the metadata the portal needs to decide what to show: whether the document is still pending,
 * the name and size of the uploaded file, and its checksum so the affiliate can prove afterwards which file
 * was sent. The storage key is deliberately excluded: it is an internal locator and exposing it would let a
 * client try to guess other files.
 *
 * @param id           identifier of the document row
 * @param documentType type from the catalogue, as the enum name
 * @param status       {@code PENDING}, {@code RECEIVED}, {@code VALIDATED} or {@code REJECTED}
 * @param fileName     original name, already sanitised when stored; {@code null} while pending
 * @param contentType  MIME type reported by the client; {@code null} while pending
 * @param sizeBytes    size in bytes; {@code null} while pending
 * @param checksum     SHA-256 of the stored file; {@code null} while pending
 * @param uploadedAt   instant of the last upload; {@code null} while pending
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 11:20 GMT-5 Bogotá D.C. Colombia
 */
public record DocumentSummaryResponse(
        Long id,
        String documentType,
        String status,
        String fileName,
        String contentType,
        Long sizeBytes,
        String checksum,
        Instant uploadedAt) {

    /**
     * Maps the persisted document to the transport record.
     *
     * @param document persisted document
     * @return the representation returned by the list and upload endpoints
     */
    public static DocumentSummaryResponse from(RequiredDocument document) {
        return new DocumentSummaryResponse(
                document.getId(),
                document.getDocumentType().name(),
                document.getStatus().name(),
                document.getFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getChecksum(),
                document.getUploadedAt());
    }
}
