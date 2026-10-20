package co.com.bits.colsubsidio.company.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Represents the CompanyRepository component of the domain module. It participates in the
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
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /**
     * Declares a persistence query executed by Spring Data JPA. The method name is translated into a query
     * at startup, so a typo in a property fails fast during application bootstrap instead of at runtime.
     *
     * The query runs inside the read-only transaction opened by the application service; no entity is
     * modified and the persistence context is never flushed by this call.
     *
     * Property names embedded in the method name are validated when the context starts, which turns a
     * refactoring mistake into an immediate startup failure instead of a runtime error.
     *
     * Filtering and pagination are supplied by the caller, so the method never loads an unbounded result
     * set into memory.
     *
     * @param nit textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @return an {@link java.util.Optional} with the value when it exists, or an empty optional when it
     * does not
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    Optional<Company> findByNit(String nit);
}
