package co.com.bits.colsubsidio.storage;

import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

/**
 * Storage port for subsidy evidence. Local filesystem for the demo, Blob Storage in Azure. Represents
 * the DocumentStorage component of the storage module. It participates in the demonstration platform
 * for affiliates, companies and subsidy requests, where the behaviour is driven by explicit business
 * rules rather than by implicit framework magic. The type keeps a single responsibility so that it can
 * be tested in isolation and replaced or extended without affecting unrelated modules. This
 * documentation is generated to the project standard: it states the contract of the type, the layer it
 * belongs to and the expectations that callers may rely on. Personal data is never logged by this
 * component, and any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public interface DocumentStorage {

    /**
     * Performs the {@code store} operation described by its signature. The method keeps a single
     * responsibility and relies on the surrounding layer for validation, transaction management and error
     * translation.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
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
    StoredDocument store(Long requestId, DocumentType documentType, MultipartFile file);

    /**
     * Implements the {@code load} step of the flow. Inputs are expected to be validated at the boundary;
     * the method focuses on the decision it owns and returns a value that the caller can map directly to
     * the transport contract.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * @param storageKey textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    InputStream load(String storageKey);

    /**
     * Performs the {@code delete} operation described by its signature. The method keeps a single
     * responsibility and relies on the surrounding layer for validation, transaction management and error
     * translation.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * @param storageKey textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    void delete(String storageKey);
}
