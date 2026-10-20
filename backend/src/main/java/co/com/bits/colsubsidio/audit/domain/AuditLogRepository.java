package co.com.bits.colsubsidio.audit.domain;

import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Represents the AuditLogRepository component of the domain module. It participates in the
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
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Exposes a derived query over the aggregate managed by this repository. Spring Data validates the
     * property path when the context starts, which turns mapping mistakes into immediate startup failures.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * @param action textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @param entityId textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param from point in time in UTC, used to keep audit entries and state transitions comparable
     *
     * @param pageable pagination and sorting instructions resolved by Spring MVC from the query string
     *
     * @return a page of results together with the metadata required by the client to paginate
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Query("""
            select a from AuditLog a
            where (:action is null or a.action = :action)
              and (:entityId is null or a.entityId = :entityId)
              and a.occurredAt >= :from
            """)
    Page<AuditLog> search(@Param("action") String action,
                          @Param("entityId") String entityId,
                          @Param("from") Instant from,
                          Pageable pageable);
}
