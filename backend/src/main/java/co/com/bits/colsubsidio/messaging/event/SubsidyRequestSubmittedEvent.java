package co.com.bits.colsubsidio.messaging.event;

import co.com.bits.colsubsidio.subsidy.domain.SubsidyType;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Published once the request is committed. Consumers must treat it as at-least-once delivery.
 * Represents the SubsidyRequestSubmittedEvent component of the event module. It participates in the
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
public record SubsidyRequestSubmittedEvent(
        Long requestId,
        String requestNumber,
        Long affiliateId,
        Long companyId,
        SubsidyType subsidyType,
        BigDecimal requestedAmount,
        Instant submittedAt) {

    public static final String EVENT_TYPE = "subsidy.request.submitted";
}
