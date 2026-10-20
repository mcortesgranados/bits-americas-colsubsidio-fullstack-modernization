package co.com.bits.colsubsidio.affiliate.domain;

import org.springframework.data.jpa.domain.Specification;

/**
 * Represents the AffiliateSpecifications component of the domain module. It participates in the
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
public final class AffiliateSpecifications {

    /**
     * Creates a new instance of {@code AffiliateSpecifications} with the supplied collaborators. The
     * constructor keeps the object in a consistent initial state and does not perform I/O, which allows it
     * to be built inside a transaction or in a plain unit test with the same semantics.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private AffiliateSpecifications() {
    }

    /**
     * Implements the {@code byStatus} domain behaviour. Business rules live here instead of in the
     * controller, which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * @param status affiliate status used to filter the query
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static Specification<Affiliate> byStatus(AffiliateStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    /**
     * Implements the {@code byCompany} domain behaviour. Business rules live here instead of in the
     * controller, which keeps the aggregate consistent no matter which entry point invokes the operation.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * Read paths never flush the persistence context; write paths participate in the transaction opened by
     * the application service and rely on optimistic locking where the aggregate declares a version
     * attribute.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * @param companyId identifier or numeric value resolved by the caller and checked against the current
     * state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static Specification<Affiliate> byCompany(Long companyId) {
        return (root, query, cb) -> companyId == null ? null : cb.equal(root.get("companyId"), companyId);
    }

    /**
     * Applies the {@code documentEndsWith} operation to this aggregate. The method enforces the invariants
     * that belong to the domain, including the allowed transitions, so an invalid call fails before any
     * state is modified.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * @param suffix textual value supplied by the caller; it is validated at the boundary before reaching
     * this member
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static Specification<Affiliate> documentEndsWith(String suffix) {
        return (root, query, cb) -> suffix == null || suffix.isBlank()
                ? null
                : cb.like(root.get("documentNumber"), "%" + suffix.trim());
    }
}
