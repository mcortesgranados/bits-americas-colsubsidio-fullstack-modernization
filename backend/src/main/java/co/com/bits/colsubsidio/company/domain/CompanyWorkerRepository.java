package co.com.bits.colsubsidio.company.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Represents the CompanyWorkerRepository component of the domain module. It participates in the
 * demonstration platform for affiliates, companies and subsidy requests, where the behaviour is driven
 * by explicit business rules rather than by implicit framework magic. The type keeps a single
 * responsibility so that it can be tested in isolation and replaced or extended without affecting
 * unrelated modules. This documentation is generated to the project standard: it states the contract
 * of the type, the layer it belongs to and the expectations that callers may rely on. Personal data is
 * never logged by this component, and any value returned to a client is masked by the DTO layer before
 * serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public interface CompanyWorkerRepository
        extends JpaRepository<CompanyWorker, Long>, JpaSpecificationExecutor<CompanyWorker> {

    /**
     * Exposes a derived query over the aggregate managed by this repository. Spring Data validates the
     * property path when the context starts, which turns mapping mistakes into immediate startup failures.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * The query is a projection over the table: it does not load entities into the persistence context,
     * which keeps the check inexpensive even when the surrounding aggregate is large.
     *
     * @param companyId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @param documentNumber textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    boolean existsByCompanyIdAndDocumentNumber(Long companyId, String documentNumber);

    /**
     * Exposes a derived query over the aggregate managed by this repository. Spring Data validates the
     * property path when the context starts, which turns mapping mistakes into immediate startup failures.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * @param id identifier or numeric value resolved by the caller and checked against the current state
     *
     * @param companyId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @return an {@link java.util.Optional} with the value when it exists, or an empty optional when it
     * does not
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    Optional<CompanyWorker> findByIdAndCompanyId(Long id, Long companyId);

    /**
     * Exposes a derived query over the aggregate managed by this repository. Spring Data validates the
     * property path when the context starts, which turns mapping mistakes into immediate startup failures.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * The query is a projection over the table: it does not load entities into the persistence context,
     * which keeps the check inexpensive even when the surrounding aggregate is large.
     *
     * @param companyId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @param status input value required by the operation (WorkerStatus); it is validated before the state
     * is modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    long countByCompanyIdAndStatus(Long companyId, WorkerStatus status);
}
