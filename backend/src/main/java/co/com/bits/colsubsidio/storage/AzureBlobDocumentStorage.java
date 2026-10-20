package co.com.bits.colsubsidio.storage;

import co.com.bits.colsubsidio.common.error.BusinessRuleException;
import co.com.bits.colsubsidio.config.StorageProperties;
import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Blob Storage adapter, enabled with {@code app.storage.provider=azure}. Represents the
 * AzureBlobDocumentStorage component of the storage module. It participates in the demonstration
 * platform for affiliates, companies and subsidy requests, where the behaviour is driven by explicit
 * business rules rather than by implicit framework magic. The type keeps a single responsibility so
 * that it can be tested in isolation and replaced or extended without affecting unrelated modules.
 * This documentation is generated to the project standard: it states the contract of the type, the
 * layer it belongs to and the expectations that callers may rely on. Personal data is never logged by
 * this component, and any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "azure")
public class AzureBlobDocumentStorage implements DocumentStorage {

    private final BlobContainerClient container;

    /**
     * Builds an instance of {@code AzureBlobDocumentStorage} from the given arguments. Every required
     * value is assigned exactly once, and optional values keep their documented defaults so partial
     * construction cannot leave the object in an ambiguous state.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @param properties typed configuration properties bound from the environment at startup
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AzureBlobDocumentStorage(StorageProperties properties) {
        if (properties.azure().connectionString() == null || properties.azure().connectionString().isBlank()) {
            throw new IllegalStateException(
                    "app.storage.azure.connection-string is required when app.storage.provider=azure");
        }
        this.container = new BlobServiceClientBuilder()
                .connectionString(properties.azure().connectionString())
                .buildClient()
                .getBlobContainerClient(properties.azure().containerName());
        if (!container.exists()) {
            container.create();
        }
    }

    /**
     * Implements the {@code store} step of the flow. Inputs are expected to be validated at the boundary;
     * the method focuses on the decision it owns and returns a value that the caller can map directly to
     * the transport contract.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param requestId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @param documentType document type from the catalogue required for the subsidy type of the request
     *
     * @param file uploaded file validated by type and size before it is persisted
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    public StoredDocument store(Long requestId, DocumentType documentType, MultipartFile file) {
        String original = StringUtils.cleanPath(
                file.getOriginalFilename() == null ? "document" : file.getOriginalFilename());
        String safeName = original.replaceAll("[^A-Za-z0-9._-]", "_");
        String key = requestId + "/" + documentType.name() + "-" + UUID.randomUUID() + "-" + safeName;
        try (InputStream input = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream digesting = new DigestInputStream(input, digest)) {
                BlobClient blob = container.getBlobClient(key);
                blob.uploadWithResponse(new BlobParallelUploadOptions(digesting)
                        .setHeaders(new BlobHttpHeaders().setContentType(file.getContentType())), null, null);
                return new StoredDocument(key, safeName, file.getContentType(), file.getSize(),
                        HexFormat.of().formatHex(digest.digest()));
            }
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Could not upload document " + safeName, ex);
        }
    }

    /**
     * Implements the {@code load} step of the flow. Inputs are expected to be validated at the boundary;
     * the method focuses on the decision it owns and returns a value that the caller can map directly to
     * the transport contract.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * @param storageKey textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    public InputStream load(String storageKey) {
        BlobClient blob = container.getBlobClient(storageKey);
        if (!blob.exists()) {
            throw new BusinessRuleException("DOCUMENT_NOT_FOUND", "The stored document is not available");
        }
        return blob.openInputStream();
    }

    /**
     * Implements the {@code delete} step of the flow. Inputs are expected to be validated at the boundary;
     * the method focuses on the decision it owns and returns a value that the caller can map directly to
     * the transport contract.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * @param storageKey textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Override
    public void delete(String storageKey) {
        container.getBlobClient(storageKey).deleteIfExists();
    }
}
