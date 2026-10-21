package co.com.bits.colsubsidio.subsidy.api.dto;

import co.com.bits.colsubsidio.subsidy.domain.SubsidyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Transport representation of a new subsidy request (UC-03).
 *
 * <p>Only the three values the affiliate actually chooses travel in the body: type, amount and purpose.
 * The affiliate, the company and the request number are resolved by the server, so a tampered client
 * cannot register a request on behalf of somebody else or pick its own radicado. Bean validation rejects
 * malformed values with a 400 before the application service runs; the demonstration rules that depend on
 * the affiliate (limit per type, category, open requests) are enforced later and answer with 422 or 409.
 *
 * @param subsidyType     type of subsidy requested; required and restricted to the enum values
 * @param requestedAmount amount in Colombian pesos; must be positive and have at most two decimals
 * @param purpose         optional free-text reason, truncated by the column to 400 characters
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 10:40 GMT-5 Bogotá D.C. Colombia
 */
public record SubsidyRequestCreateRequest(
        @NotNull(message = "The subsidy type is required")
        SubsidyType subsidyType,

        @NotNull(message = "The requested amount is required")
        @DecimalMin(value = "0.01", message = "The requested amount must be greater than zero")
        @Digits(integer = 12, fraction = 2, message = "The requested amount supports at most two decimals")
        BigDecimal requestedAmount,

        @Size(max = 400, message = "The purpose supports at most 400 characters")
        String purpose) {
}
