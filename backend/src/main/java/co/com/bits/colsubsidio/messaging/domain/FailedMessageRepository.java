package co.com.bits.colsubsidio.messaging.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Represents the FailedMessageRepository component of the domain module. It participates in the
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
public interface FailedMessageRepository extends JpaRepository<FailedMessage, Long> {

    /**
     * Declares a persistence query executed by Spring Data JPA. The method name is translated into a query
     * at startup, so a typo in a property fails fast during application bootstrap instead of at runtime.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * Count and existence queries are projections over the table: they avoid materialising entities and
     * keep the check inexpensive even for large aggregates.
     *
     * @return an immutable list with the matching elements, ordered as documented by the query
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    List<FailedMessage> findByResolvedFalseOrderByFailedAtDesc();
}
