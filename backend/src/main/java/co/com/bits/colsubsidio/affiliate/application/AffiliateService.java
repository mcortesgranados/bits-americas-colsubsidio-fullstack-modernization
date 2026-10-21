package co.com.bits.colsubsidio.affiliate.application;

import co.com.bits.colsubsidio.affiliate.api.dto.AffiliateProfileResponse;
import co.com.bits.colsubsidio.affiliate.api.dto.AffiliateSummaryResponse;
import co.com.bits.colsubsidio.affiliate.api.dto.BeneficiaryResponse;
import co.com.bits.colsubsidio.affiliate.api.dto.UpdateContactRequest;
import co.com.bits.colsubsidio.affiliate.domain.Affiliate;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateRepository;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateSpecifications;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateStatus;
import co.com.bits.colsubsidio.affiliate.domain.BeneficiaryRepository;
import co.com.bits.colsubsidio.audit.application.AuditService;
import co.com.bits.colsubsidio.audit.domain.AuditActions;
import co.com.bits.colsubsidio.common.api.PageResponse;
import co.com.bits.colsubsidio.common.error.NotFoundException;
import co.com.bits.colsubsidio.company.domain.CompanyRepository;
import co.com.bits.colsubsidio.notification.application.NotificationService;
import co.com.bits.colsubsidio.notification.api.dto.NotificationResponse;
import co.com.bits.colsubsidio.security.AccessGuard;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Represents the AffiliateService component of the application module. It participates in the
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
@Service
public class AffiliateService {

    private final AffiliateRepository affiliates;
    private final BeneficiaryRepository beneficiaries;
    private final CompanyRepository companies;
    private final NotificationService notifications;
    private final AccessGuard accessGuard;
    private final AuditService audit;

    /**
     * Creates a new instance of {@code AffiliateService} with the supplied collaborators. The constructor
     * keeps the object in a consistent initial state and does not perform I/O, which allows it to be built
     * inside a transaction or in a plain unit test with the same semantics.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * The constructor performs no I/O and no validation beyond assigning the references, which keeps
     * object creation cheap and predictable during application bootstrap.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * @param affiliates repository used to read or persist the aggregate owned by this module
     *
     * @param beneficiaries repository used to read or persist the aggregate owned by this module
     *
     * @param companies repository used to read or persist the aggregate owned by this module
     *
     * @param notifications collaborator injected through its port, which keeps the class independent from
     * the concrete implementation
     *
     * @param accessGuard input value required by the operation (AccessGuard); it is validated before the
     * state is modified
     *
     * @param audit collaborator injected through its port, which keeps the class independent from the
     * concrete implementation
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AffiliateService(AffiliateRepository affiliates, BeneficiaryRepository beneficiaries,
                            CompanyRepository companies, NotificationService notifications,
                            AccessGuard accessGuard, AuditService audit) {
        this.affiliates = affiliates;
        this.beneficiaries = beneficiaries;
        this.companies = companies;
        this.notifications = notifications;
        this.accessGuard = accessGuard;
        this.audit = audit;
    }

    /**
     * Executes the {@code ownProfile} application operation. It coordinates the collaborators required by
     * the use case, applies the authorization rules that belong to the operation and translates the
     * outcome into the value expected by the caller.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public AffiliateProfileResponse ownProfile() {
        return profile(accessGuard.requireAffiliateId());
    }

    /**
     * Implements the {@code profile} use case. The method coordinates the domain object, the repositories
     * and the outbound ports required by the operation, relies on the surrounding transaction when state
     * is modified, and leaves the HTTP concerns to the controller.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public AffiliateProfileResponse profile(Long affiliateId) {
        accessGuard.requireAffiliateAccess(affiliateId);
        Affiliate affiliate = load(affiliateId);
        boolean staffView = accessGuard.currentUser().isStaff();
        if (staffView) {
            audit.record(AuditActions.PII_ACCESS, "Affiliate", String.valueOf(affiliateId),
                    "Back-office read of masked affiliate profile");
        }
        String companyName = affiliate.getCompanyId() == null ? null
                : companies.findById(affiliate.getCompanyId()).map(company -> company.getLegalName()).orElse(null);
        return AffiliateProfileResponse.from(affiliate, companyName, staffView);
    }

    /**
     * Executes the {@code search} application operation. It coordinates the collaborators required by the
     * use case, applies the authorization rules that belong to the operation and translates the outcome
     * into the value expected by the caller.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param status affiliate status used to filter the query
     *
     * @param documentSuffix textual value supplied by the caller; it is validated at the boundary before
     * reaching this member
     *
     * @param pageable pagination and sorting instructions resolved by Spring MVC from the query string
     *
     * @return a page of results together with the metadata required by the client to paginate
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public PageResponse<AffiliateSummaryResponse> search(AffiliateStatus status, String documentSuffix,
                                                         Pageable pageable) {
        Specification<Affiliate> specification = Specification.allOf(
                AffiliateSpecifications.byStatus(status),
                AffiliateSpecifications.documentEndsWith(documentSuffix));
        var page = affiliates.findAll(specification, pageable);
        return PageResponse.from(page, AffiliateSummaryResponse::from);
    }

    /**
     * Returns the beneficiaries of the authenticated affiliate (UC-02).
     *
     * <p>The affiliate id is resolved from the token and never from the request, so the portal cannot ask
     * for another holder's list even if the client is tampered with. Both endpoints delegate here or to
     * {@link #beneficiaries(Long)}, which keeps a single implementation of the read and of its audit.
     *
     * @return the beneficiaries of the holder, sorted by full name
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> myBeneficiaries() {
        return beneficiaries(accessGuard.requireAffiliateId());
    }

    /**
     * Lists the beneficiaries of an affiliate, sorted by name and with every document number masked.
     *
     * <p>The ownership check runs before any read, so an affiliate that asks for another holder's
     * beneficiaries receives a 403 without touching the database. The rejected attempt is audited in its
     * own transaction, which means the record survives even though the request fails; the successful read
     * is audited too, because a list of third-party personal data must leave a trail.
     *
     * @param affiliateId affiliate whose beneficiaries are requested
     * @return the beneficiaries sorted by full name, never {@code null}
     */
    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> beneficiaries(Long affiliateId) {
        requireBeneficiaryAccess(affiliateId);
        load(affiliateId);
        List<BeneficiaryResponse> result = beneficiaries.findByAffiliateIdOrderByFullNameAsc(affiliateId).stream()
                .map(BeneficiaryResponse::from)
                .toList();
        audit.record(AuditActions.PII_ACCESS, "Affiliate", String.valueOf(affiliateId),
                "Beneficiary list read: " + result.size() + " record(s), documents masked");
        return result;
    }

    /**
     * Enforces object-level authorization for the beneficiary list and records a denied attempt.
     *
     * @param affiliateId affiliate the caller intends to read
     */
    private void requireBeneficiaryAccess(Long affiliateId) {
        try {
            accessGuard.requireAffiliateAccess(affiliateId);
        } catch (AccessDeniedException ex) {
            audit.record(AuditActions.PII_ACCESS_DENIED, "Affiliate", String.valueOf(affiliateId),
                    "Rejected beneficiary list read: the authenticated user is not the holder");
            throw ex;
        }
    }

    /**
     * Executes the {@code updateContact} application operation. It coordinates the collaborators required
     * by the use case, applies the authorization rules that belong to the operation and translates the
     * outcome into the value expected by the caller.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * The returned value is a snapshot of the current state. Later mutations of the entity are not
     * reflected in a previously returned instance, which avoids surprising aliasing in the controllers.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param request transport representation received from the client; its fields are validated by bean
     * validation
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional
    public AffiliateProfileResponse updateContact(UpdateContactRequest request) {
        Long affiliateId = accessGuard.requireAffiliateId();
        Affiliate affiliate = load(affiliateId);
        affiliate.setEmail(request.email().trim());
        affiliate.setPhone(request.phone() == null ? null : request.phone().trim());
        audit.record(AuditActions.AFFILIATE_CONTACT_UPDATED, "Affiliate", String.valueOf(affiliateId),
                "Contact data updated by the affiliate");
        return AffiliateProfileResponse.from(affiliate, null, false);
    }

    /**
     * Executes the {@code notifications} application operation. It coordinates the collaborators required
     * by the use case, applies the authorization rules that belong to the operation and translates the
     * outcome into the value expected by the caller.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * The implementation avoids hidden side effects, so the behaviour can be reasoned about from the
     * signature and from the transaction that surrounds the call.
     *
     * The method belongs to the application layer and respects the dependency rule of the architecture
     * baseline: dependencies point inwards and infrastructure details stay behind a port.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param pageable pagination and sorting instructions resolved by Spring MVC from the query string
     *
     * @return a page of results together with the metadata required by the client to paginate
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> notifications(Pageable pageable) {
        Long affiliateId = accessGuard.requireAffiliateId();
        return PageResponse.from(notifications.list(affiliateId, pageable), NotificationResponse::from);
    }

    /**
     * Executes the {@code unreadNotifications} application operation. It coordinates the collaborators
     * required by the use case, applies the authorization rules that belong to the operation and
     * translates the outcome into the value expected by the caller.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * Concurrency is handled by the container: beans are stateless, and any mutable state is confined to
     * the request or to a database constraint that makes the operation safe under parallel execution.
     *
     * The contract is intentionally narrow: callers should not depend on implementation details that are
     * not stated in this documentation, because they may change without a breaking announcement.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public long unreadNotifications() {
        return notifications.unreadCount(accessGuard.requireAffiliateId());
    }

    /**
     * Executes the {@code markNotificationRead} application operation. It coordinates the collaborators
     * required by the use case, applies the authorization rules that belong to the operation and
     * translates the outcome into the value expected by the caller.
     *
     * Names, literals and control flow follow the repository conventions so that new contributors can
     * predict where a behaviour lives without navigating the whole module.
     *
     * The method is covered by the tests listed in the traceability matrix, which exercise the happy path
     * and at least one failure mode relevant to the requirement it implements.
     *
     * Logging, when present, uses structured messages and never includes personal data, document numbers
     * or credentials.
     *
     * The transaction boundary is declared on this member: the unit of work is committed only when the
     * method returns normally, and any runtime exception triggers a rollback of the entities and of the
     * outbox rows written during the call.
     *
     * @param notificationId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional
    public void markNotificationRead(Long notificationId) {
        notifications.markRead(accessGuard.requireAffiliateId(), notificationId);
    }

    /**
     * Executes the {@code load} application operation. It coordinates the collaborators required by the
     * use case, applies the authorization rules that belong to the operation and translates the outcome
     * into the value expected by the caller.
     *
     * Its collaborators are supplied through the constructor, which keeps the class testable without a
     * running container and avoids hidden service lookups.
     *
     * Arguments are assumed to be validated at the boundary. The method still rejects clearly invalid
     * state early instead of propagating it to the persistence layer.
     *
     * Errors are reported with the project exceptions and translated into RFC 7807 problem details by the
     * central handler, preserving the correlation identifier of the request.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public Affiliate load(Long affiliateId) {
        return affiliates.findById(affiliateId)
                .orElseThrow(() -> NotFoundException.of("Affiliate", affiliateId));
    }
}
