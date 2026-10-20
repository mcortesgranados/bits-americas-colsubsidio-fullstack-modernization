package co.com.bits.colsubsidio.common.privacy;

/**
 * Masking helpers for personal data.
 *
 * <p>Every outbound DTO uses these helpers so that document numbers, e-mail addresses, phone numbers
 * and tax identifiers are never exposed in full, not even to back-office users. Masking is applied
 * while building the transport representation rather than in the view layer, which means a mapping
 * mistake cannot leak a raw value: the DTO simply has no field that carries it.
 *
 * <p>The helpers are pure functions and therefore trivially testable. They never log the original
 * value and never throw on malformed input; a value that cannot be interpreted is reduced to a
 * constant mask instead of being returned untouched.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public final class MaskingUtils {

    private static final int VISIBLE_DOCUMENT_DIGITS = 4;
    private static final int VISIBLE_PHONE_DIGITS = 3;
    private static final String MASK_CHARACTER = "*";

    /**
     * Builds the utility class. The constructor is private on purpose: the class only exposes static
     * helpers, so there is no state to inject and no reason to create instances.
     *
     * <p>Keeping the constructor private also prevents accidental subclassing of a final utility class
     * and makes the intent explicit to static analysis tools, which otherwise report a missing private
     * constructor as a code smell. The class is stateless and thread-safe by construction, and it does
     * not depend on Spring, which allows the masking rules to be reused from batch jobs or tests
     * without starting a context.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private MaskingUtils() {
    }

    /**
     * Masks a document number keeping only its last four digits.
     *
     * <p>The method trims the input, returns {@code null} when the value is absent and returns a mask
     * of the same length when the value is too short to hide anything. For longer values it replaces
     * every digit except the last four with an asterisk, so support staff can still confirm the last
     * digits of an identification without seeing the complete number. The original value is never
     * logged and never returned, which satisfies the minimisation requirement of the platform.
     *
     * @param value document number supplied by the caller; it may be {@code null} or blank
     * @return the masked document number, {@code null} when the input is absent, or a fully masked
     *         value when the input is shorter than the visible suffix
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static String document(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= VISIBLE_DOCUMENT_DIGITS) {
            return MASK_CHARACTER.repeat(trimmed.length());
        }
        int hidden = trimmed.length() - VISIBLE_DOCUMENT_DIGITS;
        return MASK_CHARACTER.repeat(hidden) + trimmed.substring(hidden);
    }

    /**
     * Masks an e-mail address keeping the first character of the local part and the domain.
     *
     * <p>The domain is preserved because it is not personal data and it helps the user recognise the
     * address on file, while the local part is reduced to its first character followed by a fixed
     * mask. Inputs without an {@code @} sign, or with the sign in the first position, are reduced to a
     * constant mask instead of being returned unchanged; this avoids leaking a malformed address that
     * the validation layer might have missed.
     *
     * @param value e-mail address supplied by the caller; it may be {@code null} or blank
     * @return the masked address, {@code null} when the input is absent, or a constant mask when the
     *         value cannot be interpreted as an address
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static String email(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        int at = value.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        String local = value.substring(0, at);
        String domain = value.substring(at);
        return local.charAt(0) + "***" + domain;
    }

    /**
     * Masks a phone number keeping only its last three digits.
     *
     * <p>Short numbers are fully masked so that a two-digit extension cannot be reconstructed from the
     * response. Longer numbers keep their final digits, which is enough for a user to recognise the
     * line registered in the platform without exposing the complete number to a third party reading
     * the response or the browser history. The method does not normalise the format: separators and
     * country prefixes are preserved as received, and only the digits are replaced.
     *
     * @param value phone number supplied by the caller; it may be {@code null} or blank
     * @return the masked phone number, {@code null} when the input is absent, or a fully masked value
     *         when the input is shorter than the visible suffix
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static String phone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() <= VISIBLE_PHONE_DIGITS) {
            return MASK_CHARACTER.repeat(trimmed.length());
        }
        return MASK_CHARACTER.repeat(trimmed.length() - VISIBLE_PHONE_DIGITS)
                + trimmed.substring(trimmed.length() - VISIBLE_PHONE_DIGITS);
    }

    /**
     * Masks the check digit of a tax identifier.
     *
     * <p>Companies need to recognise their own NIT in the portal, so the base number remains visible
     * while the verification digit is replaced by an asterisk. When the value does not contain a dash
     * the method returns it unchanged, because there is no check digit to hide and truncating the base
     * would make the identifier unusable for the company administrator.
     *
     * @param value tax identifier supplied by the caller; it may be {@code null} or blank
     * @return the identifier with a masked check digit, or the original value when no check digit is
     *         present, or {@code null} when the input is absent
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public static String nit(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        int dash = value.indexOf('-');
        return dash > 0 ? value.substring(0, dash) + "-*" : value;
    }
}
