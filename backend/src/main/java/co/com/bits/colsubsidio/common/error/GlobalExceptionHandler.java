package co.com.bits.colsubsidio.common.error;

import co.com.bits.colsubsidio.common.correlation.CorrelationIds;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Central translation of exceptions into RFC 7807 problem details.
 *
 * <p>Controllers and application services throw exceptions with meaning (not found, business rule,
 * conflict, access denied) and this advice decides the HTTP status, the stable machine-readable code and
 * the message that reaches the client. Keeping the translation in one place guarantees that every
 * endpoint answers with the same envelope, and it prevents internal details such as SQL fragments,
 * package names or stack traces from leaking through an error response.
 *
 * <p>Every response carries the correlation identifier of the request, so a user can quote it in a
 * support ticket and an engineer can locate the matching log entries without asking for reproduction
 * steps. Unexpected failures are logged with the full stack trace; expected failures are logged at debug
 * level to avoid noise.
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Translates a missing resource into a 404 problem detail.
     *
     * <p>The message comes from the exception because it is written by the application service and
     * never contains SQL or internal identifiers; the code is fixed so the portal can decide whether to
     * show an empty state or a navigation error. The handler does not log at warn level: a request for a
     * resource that no longer exists is a normal outcome in a portal and would otherwise flood the logs
     * with entries that do not require action.
     *
     * @param ex exception raised by the application service when the resource cannot be found
     * @return problem detail with status 404 and the {@code RESOURCE_NOT_FOUND} code
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage(), "RESOURCE_NOT_FOUND");
    }

    /**
     * Translates a violated business rule into a 422 problem detail.
     *
     * <p>Unprocessable entity is used instead of a generic 400 because the payload is syntactically
     * valid and the failure belongs to the state of the resource or to a demonstration rule, such as an
     * amount above the configured limit or an invalid status transition. The exception carries a stable
     * code that the portal can map to a specific message without parsing the text.
     *
     * @param ex exception raised by the domain or by the rules component
     * @return problem detail with status 422 and the code declared by the exception
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated", ex.getMessage(), ex.getCode());
    }

    /**
     * Translates a conflict into a 409 problem detail.
     *
     * <p>Conflicts cover duplicate keys, an open request of the same type and optimistic locking
     * failures. The client receives the stable code declared by the exception so it can distinguish a
     * duplicate from a concurrent modification and offer a different recovery action in each case. The
     * technical cause, including the constraint name, stays in the server log.
     *
     * @param ex exception raised when the operation conflicts with the current state
     * @return problem detail with status 409 and the code declared by the exception
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex) {
        return problem(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), ex.getCode());
    }

    /**
     * Translates an authorization failure into a 403 problem detail.
     *
     * <p>The handler covers both the role checks declared on the endpoints and the ownership checks
     * performed by the application layer. The message is deliberately generic: telling the caller
     * whether the resource exists would leak information about other affiliates, which is exactly the
     * object-level authorization risk the platform is trying to avoid.
     *
     * @param ex exception raised when the authenticated principal is not allowed to proceed
     * @return problem detail with status 403 and the {@code ACCESS_DENIED} code
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "Access denied",
                "You are not allowed to perform this operation", "ACCESS_DENIED");
    }

    /**
     * Translates an authentication failure into a 401 problem detail.
     *
     * <p>The message never distinguishes an unknown user from a wrong password, and it never includes
     * the token or the reason why the signature was rejected. Both cases produce the same response so an
     * attacker cannot use the error to enumerate accounts, while the real cause remains available in the
     * security log for the operations team.
     *
     * @param ex exception raised when credentials or the token cannot be validated
     * @return problem detail with status 401 and the {@code UNAUTHENTICATED} code
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication failed",
                "Invalid credentials or expired token", "UNAUTHENTICATED");
    }

    /**
     * Translates a bean validation failure into a 400 problem detail with per-field errors.
     *
     * <p>The response includes a map of field name to message, which allows the Angular forms to mark
     * the exact input that failed instead of showing a single generic error. When the same field
     * produces several violations the messages are concatenated, so the client receives all the reasons
     * in one response and the user does not have to submit the form repeatedly to discover them.
     *
     * @param ex exception raised by Spring MVC when the request body violates its constraints
     * @return problem detail with status 400, the {@code VALIDATION_ERROR} code and the field errors
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.merge(fieldError.getField(), fieldError.getDefaultMessage(), (a, b) -> a + "; " + b);
        }
        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more fields are invalid", "VALIDATION_ERROR");
        detail.setProperty("errors", errors);
        return detail;
    }

    /**
     * Translates a method-level validation failure into a 400 problem detail.
     *
     * <p>Spring 6 validates constraints declared directly on handler parameters, such as a path
     * variable with a pattern or a query parameter with a maximum size. Those failures do not populate a
     * binding result, so they need their own handler; without it they would fall through to the generic
     * branch and be reported as an internal error, which would confuse the portal and hide a client
     * mistake behind a 500.
     *
     * @param ex exception raised when a handler parameter violates its constraint
     * @return problem detail with status 400 and the {@code VALIDATION_ERROR} code
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidation(HandlerMethodValidationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more parameters are invalid", "VALIDATION_ERROR");
    }

    /**
     * Translates a constraint violation raised outside the request body into a 400 problem detail.
     *
     * <p>The handler covers validations executed by the service layer, for example when a pagination
     * parameter or a document type is validated before touching the database. The message is preserved
     * because it is written by the validation layer and does not contain internal details, while the
     * stable code keeps the contract identical to the one used for body validation.
     *
     * @param ex exception raised when a constraint is violated outside the MVC binding process
     * @return problem detail with status 400 and the {@code VALIDATION_ERROR} code
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(ConstraintViolationException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", ex.getMessage(), "VALIDATION_ERROR");
    }

    /**
     * Translates an oversized upload into a 413 problem detail.
     *
     * <p>The limit is enforced twice: by the servlet container and by the storage adapter. This handler
     * covers the container-level rejection, which happens before the controller runs, and returns a
     * code the portal can use to show a specific message about the maximum size instead of a generic
     * failure. The partial upload is discarded by the container and never reaches the storage adapter.
     *
     * @param ex exception raised when the multipart request exceeds the configured size
     * @return problem detail with status 413 and the {@code FILE_TOO_LARGE} code
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUpload(MaxUploadSizeExceededException ex) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "File too large",
                "The uploaded file exceeds the allowed size", "FILE_TOO_LARGE");
    }

    /**
     * Translates the application-level size check into the same 413 the container produces.
     *
     * <p>Two limits exist: the multipart cap of the servlet container and the business ceiling configured in
     * {@code app.storage.max-size-bytes}. When the second is lower, or when the container cap is raised for
     * another endpoint, the service raises this exception and the client must still receive 413 with the same
     * code, so the portal has a single case to handle.
     *
     * @param ex exception raised by the application service when the file exceeds the business ceiling
     * @return problem detail with status 413 and the code declared by the exception
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-21 11:20 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(PayloadTooLargeException.class)
    public ProblemDetail handlePayloadTooLarge(PayloadTooLargeException ex) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "File too large", ex.getMessage(), ex.getCode());
    }

    /**
     * Translates a database integrity failure into a 409 problem detail.
     *
     * <p>The exception is logged at warn level with the correlation identifier, but the response does
     * not reproduce the driver message: it may contain the table name, the constraint and even a
     * fragment of the offending value, which would leak schema information and personal data. The
     * client receives a stable conflict code and a generic explanation, and support can use the
     * correlation identifier to inspect the log.
     *
     * @param ex exception raised when a database constraint is violated
     * @return problem detail with status 409 and the {@code DATA_CONFLICT} code
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation correlationId={}", CorrelationIds.current(), ex);
        return problem(HttpStatus.CONFLICT, "Conflict",
                "The operation conflicts with existing data", "DATA_CONFLICT");
    }

    /**
     * Translates any unhandled exception into a 500 problem detail.
     *
     * <p>The full stack trace is written to the log with the correlation identifier, while the client
     * receives a neutral message that asks for that identifier. This is the only branch that produces a
     * 500, which keeps the operational alerts meaningful: a spike in this counter indicates a defect or
     * a dependency outage, not a validation problem caused by a user.
     *
     * @param ex unexpected exception that reached the boundary of the application
     * @return problem detail with status 500 and the {@code INTERNAL_ERROR} code
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception correlationId={}", CorrelationIds.current(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error",
                "An unexpected error occurred. Quote the correlation id when contacting support.", "INTERNAL_ERROR");
    }

    /**
     * Builds the problem detail shared by every handler in this advice.
     *
     * <p>The helper centralises the three properties that must be present in every error: the title,
     * the stable code and the correlation identifier. Keeping the construction in one method avoids the
     * situation where a new handler forgets the correlation identifier and makes an incident harder to
     * investigate. The instance is created per call because {@link ProblemDetail} is mutable, and the
     * method never returns {@code null}.
     *
     * @param status HTTP status to return
     * @param title  short human-readable summary of the problem
     * @param detail explanation that is safe to show to the client
     * @param code   stable machine-readable code consumed by the portals
     * @return the populated problem detail, never {@code null}
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    private ProblemDetail problem(HttpStatus status, String title, String detail, String code) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("code", code);
        problem.setProperty("correlationId", CorrelationIds.current());
        return problem;
    }
}
