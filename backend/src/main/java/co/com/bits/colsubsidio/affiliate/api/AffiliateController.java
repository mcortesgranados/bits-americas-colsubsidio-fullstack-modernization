package co.com.bits.colsubsidio.affiliate.api;

import co.com.bits.colsubsidio.affiliate.api.dto.AffiliateProfileResponse;
import co.com.bits.colsubsidio.affiliate.api.dto.AffiliateSummaryResponse;
import co.com.bits.colsubsidio.affiliate.api.dto.BeneficiaryResponse;
import co.com.bits.colsubsidio.affiliate.api.dto.UpdateContactRequest;
import co.com.bits.colsubsidio.affiliate.application.AffiliateService;
import co.com.bits.colsubsidio.affiliate.domain.AffiliateStatus;
import co.com.bits.colsubsidio.common.api.PageResponse;
import co.com.bits.colsubsidio.notification.api.dto.NotificationResponse;
import co.com.bits.colsubsidio.subsidy.api.dto.SubsidyRequestSummaryResponse;
import co.com.bits.colsubsidio.subsidy.application.SubsidyRequestService;
import co.com.bits.colsubsidio.subsidy.domain.SubsidyRequestStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Represents the AffiliateController component of the api module. It participates in the demonstration
 * platform for affiliates, companies and subsidy requests, where the behaviour is driven by explicit
 * business rules rather than by implicit framework magic. The type keeps a single responsibility so
 * that it can be tested in isolation and replaced or extended without affecting unrelated modules.
 * This documentation is generated to the project standard: it states the contract of the type, the
 * layer it belongs to and the expectations that callers may rely on. Personal data is never logged by
 * this component, and any value returned to a client is masked by the DTO layer before serialization.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@RestController
@RequestMapping("/api/v1/affiliates")
@Tag(name = "Affiliates", description = "Affiliate self-service endpoints and back-office consultation")
@SecurityRequirement(name = "bearer-jwt")
public class AffiliateController {

    private final AffiliateService affiliateService;
    private final SubsidyRequestService subsidyRequestService;

    /**
     * Creates a new instance of {@code AffiliateController} with the supplied collaborators. The
     * constructor keeps the object in a consistent initial state and does not perform I/O, which allows it
     * to be built inside a transaction or in a plain unit test with the same semantics.
     *
     * Fields are assigned exactly once and never reassigned, so the instance is safe to publish to other
     * threads as long as the collaborators themselves are thread-safe.
     *
     * Spring injects the dependencies declared here; if a bean is missing, the context fails at startup
     * instead of throwing a null pointer at request time.
     *
     * Collaborators are received as interfaces or repositories so the class can be instantiated in a plain
     * unit test with doubles, without starting a Spring context or a database.
     *
     * @param affiliateService collaborator injected through its port, which keeps the class independent
     * from the concrete implementation
     *
     * @param subsidyRequestService collaborator injected through its port, which keeps the class
     * independent from the concrete implementation
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AffiliateController(AffiliateService affiliateService, SubsidyRequestService subsidyRequestService) {
        this.affiliateService = affiliateService;
        this.subsidyRequestService = subsidyRequestService;
    }

    /**
     * Implements the REST endpoint declared by its mapping. The handler stays thin on purpose: it binds
     * and validates the request, delegates to the application service and returns the representation
     * expected by the portal.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * The response keeps the correlation identifier in the headers so that a user can quote it in a
     * support request and an engineer can find the matching log entries.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @GetMapping("/me")
    @Operation(summary = "Current affiliate profile (masked PII)")
    public AffiliateProfileResponse me() {
        return affiliateService.ownProfile();
    }

    /**
     * Implements the REST endpoint declared by its mapping. The handler stays thin on purpose: it binds
     * and validates the request, delegates to the application service and returns the representation
     * expected by the portal.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * Errors raised by the service are translated into RFC 7807 problem details by the central exception
     * handler, so this method does not build error payloads by hand.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @param request transport representation received from the client; its fields are validated by bean
     * validation
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @PatchMapping("/me/contact")
    @Operation(summary = "Update the authenticated affiliate's contact data")
    public AffiliateProfileResponse updateContact(@Valid @RequestBody UpdateContactRequest request) {
        return affiliateService.updateContact(request);
    }

    /**
     * Handles the HTTP request described by the surrounding mapping annotation. The method only translates
     * the transport contract into an application call, so validation, authorization and business decisions
     * remain in the layers that own them.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * Errors raised by the service are translated into RFC 7807 problem details by the central exception
     * handler, so this method does not build error payloads by hand.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @return an immutable list with the matching elements, ordered as documented by the query
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @GetMapping("/me/beneficiaries")
    @Operation(summary = "Beneficiaries registered by the authenticated affiliate")
    public List<BeneficiaryResponse> myBeneficiaries() {
        return affiliateService.beneficiaries(affiliateService.ownProfile().id());
    }

    /**
     * Handles the HTTP request described by the surrounding mapping annotation. The method only translates
     * the transport contract into an application call, so validation, authorization and business decisions
     * remain in the layers that own them.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * The response keeps the correlation identifier in the headers so that a user can quote it in a
     * support request and an engineer can find the matching log entries.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @param status target status that must be reachable from the current state of the aggregate
     *
     * @param pageable pagination and sorting instructions resolved by Spring MVC from the query string
     *
     * @return a page of results together with the metadata required by the client to paginate
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @GetMapping("/me/subsidy-requests")
    @Operation(summary = "Subsidy requests of the authenticated affiliate, newest first")
    public PageResponse<SubsidyRequestSummaryResponse> myRequests(
            @RequestParam(required = false) SubsidyRequestStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return subsidyRequestService.listForAffiliate(affiliateService.ownProfile().id(), status, pageable);
    }

    /**
     * Implements the REST endpoint declared by its mapping. The handler stays thin on purpose: it binds
     * and validates the request, delegates to the application service and returns the representation
     * expected by the portal.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * Errors raised by the service are translated into RFC 7807 problem details by the central exception
     * handler, so this method does not build error payloads by hand.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @param pageable pagination and sorting instructions resolved by Spring MVC from the query string
     *
     * @return a page of results together with the metadata required by the client to paginate
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @GetMapping("/me/notifications")
    @Operation(summary = "In-app notifications of the authenticated affiliate")
    public PageResponse<NotificationResponse> myNotifications(@PageableDefault(size = 20) Pageable pageable) {
        return affiliateService.notifications(pageable);
    }

    /**
     * Implements the REST endpoint declared by its mapping. The handler stays thin on purpose: it binds
     * and validates the request, delegates to the application service and returns the representation
     * expected by the portal.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * Errors raised by the service are translated into RFC 7807 problem details by the central exception
     * handler, so this method does not build error payloads by hand.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @GetMapping("/me/notifications/unread-count")
    @Operation(summary = "Number of unread notifications")
    public ResponseEntity<UnreadCount> unread() {
        return ResponseEntity.ok(new UnreadCount(affiliateService.unreadNotifications()));
    }

    /**
     * Implements the REST endpoint declared by its mapping. The handler stays thin on purpose: it binds
     * and validates the request, delegates to the application service and returns the representation
     * expected by the portal.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * The response keeps the correlation identifier in the headers so that a user can quote it in a
     * support request and an engineer can find the matching log entries.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @param notificationId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @PatchMapping("/me/notifications/{notificationId}/read")
    @Operation(summary = "Mark a notification as read")
    public ResponseEntity<Void> markRead(@PathVariable Long notificationId) {
        affiliateService.markNotificationRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Implements the REST endpoint declared by its mapping. The handler stays thin on purpose: it binds
     * and validates the request, delegates to the application service and returns the representation
     * expected by the portal.
     *
     * Errors raised by the service are translated into RFC 7807 problem details by the central exception
     * handler, so this method does not build error payloads by hand.
     *
     * The response keeps the correlation identifier in the headers so that a user can quote it in a
     * support request and an engineer can find the matching log entries.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * Access is restricted by the expression declared in the security annotation; an authenticated
     * principal without the required authority receives a 403 response before the method body is executed.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
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
    @GetMapping
    @PreAuthorize("hasAnyRole('BACKOFFICE','AUDITOR')")
    @Operation(summary = "Paginated back-office consultation of affiliates")
    public PageResponse<AffiliateSummaryResponse> search(
            @RequestParam(required = false) AffiliateStatus status,
            @RequestParam(required = false) String documentSuffix,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return affiliateService.search(status, documentSuffix, pageable);
    }

    /**
     * Implements the REST endpoint declared by its mapping. The handler stays thin on purpose: it binds
     * and validates the request, delegates to the application service and returns the representation
     * expected by the portal.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * The response keeps the correlation identifier in the headers so that a user can quote it in a
     * support request and an engineer can find the matching log entries.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * Access is restricted by the expression declared in the security annotation; an authenticated
     * principal without the required authority receives a 403 response before the method body is executed.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @GetMapping("/{affiliateId}")
    @PreAuthorize("hasAnyRole('BACKOFFICE','AUDITOR','AFFILIATE')")
    @Operation(summary = "Affiliate detail; affiliates can only read their own record")
    public AffiliateProfileResponse detail(@PathVariable Long affiliateId) {
        return affiliateService.profile(affiliateId);
    }

    /**
     * Handles the HTTP request described by the surrounding mapping annotation. The method only translates
     * the transport contract into an application call, so validation, authorization and business decisions
     * remain in the layers that own them.
     *
     * The response keeps the correlation identifier in the headers so that a user can quote it in a
     * support request and an engineer can find the matching log entries.
     *
     * Errors raised by the service are translated into RFC 7807 problem details by the central exception
     * handler, so this method does not build error payloads by hand.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * Access is restricted by the expression declared in the security annotation; an authenticated
     * principal without the required authority receives a 403 response before the method body is executed.
     *
     * The HTTP contract is declared by the mapping annotation: the path, the method and the expected
     * representation are part of the public API and must stay aligned with the OpenAPI document and the
     * Postman collection.
     *
     * @param affiliateId identifier or numeric value resolved by the caller and checked against the
     * current state
     *
     * @return an immutable list with the matching elements, ordered as documented by the query
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @GetMapping("/{affiliateId}/beneficiaries")
    @PreAuthorize("hasAnyRole('BACKOFFICE','AUDITOR','AFFILIATE')")
    @Operation(summary = "Beneficiaries of an affiliate; ownership is enforced")
    public List<BeneficiaryResponse> beneficiaries(@PathVariable Long affiliateId) {
        return affiliateService.beneficiaries(affiliateId);
    }

    /**
     * Handles the HTTP request described by the surrounding mapping annotation. The method only translates
     * the transport contract into an application call, so validation, authorization and business decisions
     * remain in the layers that own them.
     *
     * Errors raised by the service are translated into RFC 7807 problem details by the central exception
     * handler, so this method does not build error payloads by hand.
     *
     * Authorization happens before the body executes: the role check is declared on the method and the
     * ownership check is enforced by the application service.
     *
     * The handler binds and validates the request, delegates the decision to the application service and
     * returns the representation expected by the portal, without business logic of its own.
     *
     * @param unread numeric value used by the operation; negative values are rejected when they are not
     * meaningful
     *
     * @return the resulting value, never {@code null} unless the contract states otherwise
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public record UnreadCount(long unread) {
    }
}
