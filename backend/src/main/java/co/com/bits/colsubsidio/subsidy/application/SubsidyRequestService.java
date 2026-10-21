package co.com.bits.colsubsidio.subsidy.application;

import co.com.bits.colsubsidio.common.api.PageResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestSummaryResponse;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestSpecifications;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service that answers the read queries over subsidy requests belonging to one affiliate.
 *
 * <p>The service sits between the controller and the repository and owns three decisions that do not
 * belong to either of them: which rows are visible given the caller's identity, how the optional status
 * filter is composed, and how the page of entities becomes a page of transport records. Keeping those
 * decisions here means the controller stays a thin adapter and the repository stays a persistence
 * concern.</p>
 *
 * <p>Every method runs in a read-only transaction. That hint lets Hibernate skip dirty checking and
 * tells the driver the connection will not write, which matters when several portal instances share the
 * same database. The class holds no mutable state, so a single instance is safe to use from concurrent
 * requests.</p>
 *
 * <p>Personal data is never selected by these queries: the projection only reads the radicado, the type,
 * the status, the amount and the dates. The affiliate identifier is used as a filter and is never
 * returned, which keeps the response free of identifiers that could be enumerated by a client.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Service
public class SubsidyRequestService {

    private final SubsidyRequestRepository requests;

    /**
     * Creates the service with the repository it delegates persistence to.
     *
     * <p>The collaborator is received as an interface rather than a concrete class so the service can be
     * instantiated in a plain unit test with a double, without starting a Spring context or a database.
     * The reference is assigned once and never reassigned, which makes the instance effectively immutable
     * and safe to publish to other threads.</p>
     *
     * <p>Spring injects the dependency declared here; if the bean is missing the context fails at startup
     * instead of throwing a null pointer during the first request. The constructor performs no I/O and
     * opens no transaction, so building the object is cheap and free of side effects.</p>
     *
     * @param requests repository used to read the aggregate; never {@code null}
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public SubsidyRequestService(SubsidyRequestRepository requests) {
        this.requests = requests;
    }

    /**
     * Lists the subsidy requests of one affiliate, newest first when the caller sorts by submitted date.
     *
     * <p>The affiliate identifier is supplied by the caller after it has been resolved from the security
     * context, never from a path variable, so a client cannot read another person's requests by changing
     * an identifier in the URL. The optional status narrows the result set without changing the shape of
     * the response, which keeps the portal's table stable when a filter is applied.</p>
     *
     * <p>Filtering and pagination are delegated to the database: the specification is translated into a
     * {@code where} clause and the pageable into {@code limit} and {@code offset}, so the method never
     * loads an unbounded result set into memory. The projection to the transport record happens after the
     * page has been read, which keeps the persistence context small.</p>
     *
     * <p>The returned envelope carries the total number of elements and the page metadata, so the client
     * can render pagination without issuing a second count query. An empty result is a valid answer and
     * is returned as an empty page rather than as an error.</p>
     *
     * @param affiliateId owner of the requests, already authorised by the caller; must not be {@code null}
     * @param status      optional filter; when {@code null} every status of the affiliate is included
     * @param pageable    pagination and sorting instructions resolved by Spring MVC from the query string
     * @return a page of summaries together with the metadata required by the client to paginate
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public PageResponse<SubsidyRequestSummaryResponse> listForAffiliate(
            Long affiliateId, SubsidyRequestStatus status, Pageable pageable) {
        Specification<SubsidyRequest> specification = SubsidyRequestSpecifications.byAffiliate(affiliateId);
        if (status != null) {
            specification = specification.and(SubsidyRequestSpecifications.byStatus(status));
        }
        Page<SubsidyRequest> page = requests.findAll(specification, pageable);
        return PageResponse.from(page, SubsidyRequestSummaryResponse::from);
    }
}
