package co.com.bits.colsubsidio.subsidy.application;

import co.com.bits.colsubsidio.affiliate.domain.Affiliate;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateRepository;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateStatus;
import co.com.bits.colsubsidio.affiliate.domain.BeneficiaryRepository;
import co.com.bits.colsubsidio.common.api.PageResponse;
import co.com.bits.colsubsidio.common.correlation.CorrelationIds;
import co.com.bits.colsubsidio.common.error.BusinessRuleException;
import co.com.bits.colsubsidio.common.error.ConflictException;
import co.com.bits.colsubsidio.common.error.NotFoundException;
import co.com.bits.colsubsidio.messaging.application.OutboxService;
import co.com.bits.colsubsidio.messaging.event.SubsidyRequestSubmittedEvent;
import co.com.bits.colsubsidio.security.AccessGuard;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestCreateRequest;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestCreatedResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestSummaryResponse;
import co.com.bits.colsubsidio.subsidy.domain.DocumentType;
import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistory;
import co.com.bits.colsubsidio.subsidy.domain.RequestStatusHistoryRepository;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocument;
import co.com.bits.colsubsidio.subsidy.domain.RequiredDocumentRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequest;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestRepository;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestSpecifications;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyType;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service of the subsidy requests: it registers a new request (UC-03) and answers the read
 * queries over the requests of one affiliate.
 *
 * <p>The service owns the decisions that belong neither to the controller nor to the repository: which
 * affiliate is acting, in which order the demonstration rules are applied, which rows become visible given
 * the caller's identity, and how the aggregate becomes a transport record. The controller stays a thin
 * adapter and the repository stays a persistence concern.</p>
 *
 * <p>Registration is the only write path here. It runs in a single transaction that persists the request,
 * its document catalogue, the first status-history row and the outbox event together: if any of them fails,
 * nothing is committed and the affiliate is told to retry. The class holds no mutable state beyond its
 * collaborators, so a single instance is safe to use from concurrent requests.</p>
 *
 * <p>Personal data is never selected by the read queries: the projection only reads the radicado, the type,
 * the status, the amount and the dates. The affiliate identifier is used as a filter and is never returned,
 * which keeps the response free of identifiers that could be enumerated by a client.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Service
public class SubsidyRequestService {

    private static final String REGISTRATION_REASON = "Solicitud registrada por el afiliado";

    private final SubsidyRequestRepository requests;
    private final AffiliateRepository affiliates;
    private final BeneficiaryRepository beneficiaries;
    private final RequiredDocumentRepository documents;
    private final RequestStatusHistoryRepository history;
    private final OutboxService outbox;
    private final SubsidyDemoRules rules;
    private final RequestNumberGenerator numbers;
    private final AccessGuard accessGuard;

    /**
     * Creates the service with the collaborators required by the two operations it exposes.
     *
     * <p>Every collaborator is received as an interface or a stateless component, so the service can be
     * instantiated in a plain unit test with doubles, without starting a Spring context or a database. The
     * references are assigned once and never reassigned, which makes the instance effectively immutable.
     *
     * @param requests      repository of the aggregate
     * @param affiliates    repository used to read the acting affiliate and its category
     * @param beneficiaries repository used by the education rule
     * @param documents     repository of the required documents created with the request
     * @param history       repository of the status timeline
     * @param outbox        transactional outbox used to publish the submitted event
     * @param rules         demonstration limits, eligibility and document catalogue
     * @param numbers       generator of the readable radicado
     * @param accessGuard   object-level authorization and access to the authenticated principal
     */
    public SubsidyRequestService(SubsidyRequestRepository requests, AffiliateRepository affiliates,
                                 BeneficiaryRepository beneficiaries, RequiredDocumentRepository documents,
                                 RequestStatusHistoryRepository history, OutboxService outbox,
                                 SubsidyDemoRules rules, RequestNumberGenerator numbers, AccessGuard accessGuard) {
        this.requests = requests;
        this.affiliates = affiliates;
        this.beneficiaries = beneficiaries;
        this.documents = documents;
        this.history = history;
        this.outbox = outbox;
        this.rules = rules;
        this.numbers = numbers;
        this.accessGuard = accessGuard;
    }

    /**
     * Registers a subsidy request for the authenticated affiliate (UC-03).
     *
     * <p>The affiliate is resolved from the token, never from the body or the URL, so a client cannot file a
     * request on behalf of somebody else. The checks run in a deliberate order: the affiliate must exist and
     * be active, the amount must be positive and within the demonstration ceiling, a credit request must
     * come from category A or B, an education request needs at least one registered beneficiary, and only one
     * open request per type is allowed. Each failure raises a stable code that the portal can translate.
     *
     * <p>When the checks pass, the aggregate is created in {@code DRAFT} and immediately submitted, which is
     * the only transition the aggregate allows from that state. The request, its document catalogue, the
     * first timeline entry and the outbox event are written in the same transaction: the consumer that
     * classifies the request can only see it after the commit, and a failure anywhere rolls everything back.
     *
     * @param request values chosen by the affiliate: type, amount and optional purpose
     * @return the radicado, the initial status and the documents still required
     * @throws NotFoundException     when the token points to an affiliate that no longer exists
     * @throws BusinessRuleException when a demonstration rule rejects the request
     * @throws ConflictException     when the affiliate already has an open request of the same type
     */
    @Transactional
    public SubsidyRequestCreatedResponse create(SubsidyRequestCreateRequest request) {
        Long affiliateId = accessGuard.requireAffiliateId();
        Affiliate affiliate = affiliates.findById(affiliateId)
                .orElseThrow(() -> NotFoundException.of("Affiliate", affiliateId));

        if (affiliate.getStatus() != AffiliateStatus.ACTIVE) {
            throw new BusinessRuleException("AFFILIATE_NOT_ACTIVE",
                    "Only an active affiliate can register a subsidy request");
        }
        rules.validateSubmission(affiliate.getCategory(), request.subsidyType(), request.requestedAmount());

        if (request.subsidyType() == SubsidyType.EDUCACION && beneficiaries.countByAffiliateId(affiliateId) == 0) {
            throw new BusinessRuleException("NO_BENEFICIARIES",
                    "Demonstration rule: education subsidies require at least one registered beneficiary");
        }
        if (requests.existsByAffiliateIdAndSubsidyTypeAndStatusIn(
                affiliateId, request.subsidyType(), SubsidyRequestStatus.openStatuses())) {
            throw new ConflictException("DUPLICATE_OPEN_REQUEST",
                    "There is already an open request of the same type for this affiliate");
        }

        Instant now = Instant.now();
        SubsidyRequest entity = new SubsidyRequest(numbers.next(), affiliateId, affiliate.getCompanyId(),
                request.subsidyType(), request.requestedAmount().setScale(2, RoundingMode.HALF_UP),
                normalise(request.purpose()));
        entity.submit(now);
        SubsidyRequest saved = requests.save(entity);

        List<DocumentType> required = rules.requiredDocuments(request.subsidyType()).stream()
                .sorted(Comparator.comparing(Enum::name))
                .toList();
        required.forEach(type -> documents.save(new RequiredDocument(saved.getId(), type)));

        history.save(new RequestStatusHistory(saved.getId(), SubsidyRequestStatus.DRAFT,
                SubsidyRequestStatus.SUBMITTED, REGISTRATION_REASON, accessGuard.currentUser().subject(),
                CorrelationIds.current(), now));

        outbox.append(SubsidyRequestSubmittedEvent.EVENT_TYPE, "SubsidyRequest", String.valueOf(saved.getId()),
                new SubsidyRequestSubmittedEvent(saved.getId(), saved.getRequestNumber(), affiliateId,
                        affiliate.getCompanyId(), saved.getSubsidyType(), saved.getRequestedAmount(),
                        saved.getSubmittedAt()));

        return SubsidyRequestCreatedResponse.from(saved, required);
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
     * @param affiliateId owner of the requests, already authorised by the caller; must not be {@code null}
     * @param status      optional filter; when {@code null} every status of the affiliate is included
     * @param pageable    pagination and sorting instructions resolved by Spring MVC from the query string
     * @return a page of summaries together with the metadata required by the client to paginate
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

    /**
     * Trims the optional purpose and turns a blank value into {@code null}, so the column stores absence
     * instead of an empty string and the portal does not show a stray empty paragraph.
     *
     * @param purpose free text sent by the affiliate; it may be {@code null}
     * @return the normalised purpose or {@code null}
     */
    private String normalise(String purpose) {
        if (purpose == null) {
            return null;
        }
        String trimmed = purpose.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
