package co.com.bits.colsubsidio.common.error;

/**
 * Raised when a request body exceeds the size accepted by the application.
 *
 * <p>It exists as a separate type because the HTTP answer is 413 and not 422: the payload is too big to be
 * read, so there is nothing to validate. The central handler maps it to a problem detail with the stable code
 * the portal translates ({@code FILE_TOO_LARGE}). Spring's own {@code MaxUploadSizeExceededException} is
 * raised before the controller when the multipart parser hits its limit; this exception covers the check the
 * application performs over the configured business limit, so both paths answer with the same status and
 * code.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-21 11:20 GMT-5 Bogotá D.C. Colombia
 */
public class PayloadTooLargeException extends RuntimeException {

    private final String code;

    /**
     * Builds the exception with the stable code returned to the client.
     *
     * @param code    machine-readable code, for example {@code FILE_TOO_LARGE}
     * @param message human-readable explanation, free of personal data
     */
    public PayloadTooLargeException(String code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * Returns the stable code used in the problem detail.
     *
     * @return the code, never {@code null}
     */
    public String getCode() {
        return code;
    }
}
