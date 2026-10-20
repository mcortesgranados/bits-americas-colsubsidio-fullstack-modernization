package co.com.bits.colsubsidio.messaging.domain;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Represents the OutboxEventRepository component of the domain module. It participates in the
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
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    /**
     * Exposes a derived query over the aggregate managed by this repository. Spring Data validates the
     * property path when the context starts, which turns mapping mistakes into immediate startup failures.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * @param status input value required by the operation (OutboxStatus); it is validated before the state
     * is modified
     *
     * @param pageable pagination and sorting instructions resolved by Spring MVC from the query string
     *
     * @return an immutable list with the matching elements, ordered as documented by the query
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);

    /**
     * Exposes a derived query over the aggregate managed by this repository. Spring Data validates the
     * property path when the context starts, which turns mapping mistakes into immediate startup failures.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * The query is a projection over the table: it does not load entities into the persistence context,
     * which keeps the check inexpensive even when the surrounding aggregate is large.
     *
     * @param status input value required by the operation (OutboxStatus); it is validated before the state
     * is modified
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    long countByStatus(OutboxStatus status);
}
