package co.com.bits.colsubsidio.subsidy.domain;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Represents the SubsidyRequestRepository component of the domain module. It participates in the
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
public interface SubsidyRequestRepository
        extends JpaRepository<SubsidyRequest, Long>, JpaSpecificationExecutor<SubsidyRequest> {

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
     * @param requestNumber textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @return an {@link java.util.Optional} with the value when it exists, or an empty optional when it
     * does not
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    Optional<SubsidyRequest> findByRequestNumber(String requestNumber);

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
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * The query is a projection over the table: it does not load entities into the persistence context,
     * which keeps the check inexpensive even when the surrounding aggregate is large.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @param subsidyType subsidy type that drives the applicable demonstration rules
     *
     * @param statuses input value required by the operation (Collection<SubsidyRequestStatus>); it is
     * validated before the state is modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    boolean existsByAffiliateIdAndSubsidyTypeAndStatusIn(
            Long affiliateId, SubsidyType subsidyType, Collection<SubsidyRequestStatus> statuses);

    /**
     * Exposes a derived query over the aggregate managed by this repository. Spring Data validates the
     * property path when the context starts, which turns mapping mistakes into immediate startup failures.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * The query is a projection over the table: it does not load entities into the persistence context,
     * which keeps the check inexpensive even when the surrounding aggregate is large.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @param statuses input value required by the operation (Collection<SubsidyRequestStatus>); it is
     * validated before the state is modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    long countByAffiliateIdAndStatusIn(Long affiliateId, Collection<SubsidyRequestStatus> statuses);

    /**
     * Exposes a derived query over the aggregate managed by this repository. Spring Data validates the
     * property path when the context starts, which turns mapping mistakes into immediate startup failures.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * The query is a projection over the table: it does not load entities into the persistence context,
     * which keeps the check inexpensive even when the surrounding aggregate is large.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    long countByAffiliateId(Long affiliateId);

    /**
     * Declares a persistence query executed by Spring Data JPA. The method name is translated into a query
     * at startup, so a typo in a property fails fast during application bootstrap instead of at runtime.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
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
     * @param status target status that must be reachable from the current state of the aggregate
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    long countByCompanyIdAndStatus(Long companyId, SubsidyRequestStatus status);

    /**
     * Declares a persistence query executed by Spring Data JPA. The method name is translated into a query
     * at startup, so a typo in a property fails fast during application bootstrap instead of at runtime.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * The query is a projection over the table: it does not load entities into the persistence context,
     * which keeps the check inexpensive even when the surrounding aggregate is large.
     *
     * @param companyId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    long countByCompanyId(Long companyId);

    /**
     * Declares a persistence query executed by Spring Data JPA. The method name is translated into a query
     * at startup, so a typo in a property fails fast during application bootstrap instead of at runtime.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * The query is a projection over the table: it does not load entities into the persistence context,
     * which keeps the check inexpensive even when the surrounding aggregate is large.
     *
     * @param status target status that must be reachable from the current state of the aggregate
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    long countByStatus(SubsidyRequestStatus status);
}
