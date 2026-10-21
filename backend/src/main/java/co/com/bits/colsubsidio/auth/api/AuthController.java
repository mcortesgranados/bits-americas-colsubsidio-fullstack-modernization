package co.com.bits.colsubsidio.auth.api;

import co.com.bits.colsubsidio.auth.api.dto.DemoTokenRequest;
import co.com.bits.colsubsidio.auth.api.dto.DemoTokenResponse;
import co.com.bits.colsubsidio.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the only public endpoint of the API: the exchange of demonstration credentials for a token.
 *
 * <p>The controller is intentionally minimal. It binds and validates the body, delegates to the
 * application service and returns the representation, without deciding anything about credentials. The
 * path is declared as public in the security configuration, which is what allows a client to obtain its
 * first token without already holding one.</p>
 *
 * <p>Keeping this endpoint separate from the affiliate and company controllers makes the boundary
 * explicit: authentication is the only place where a password is accepted, and every other endpoint
 * works with a token that has already been verified. That separation is also what makes the security
 * configuration readable, because the list of public paths contains exactly one business route.</p>
 *
 * <p>The response never includes the password, the stored hash or the identifier of the account row. A
 * failed attempt is translated into an RFC 7807 problem detail by the central handler, so this class
 * does not build error payloads by hand.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Exchange of demonstration credentials for a bearer token")
public class AuthController {

    private final AuthService authService;

    /**
     * Creates the controller with the application service it delegates to.
     *
     * <p>The collaborator is injected by Spring and stored in a final field, so the instance is immutable
     * and safe to share across concurrent requests. No I/O happens here: the controller does not touch
     * the repository or the encoder directly, which keeps the transport layer free of security
     * decisions.</p>
     *
     * <p>The dependency is declared explicitly instead of resolved through a service locator, so a
     * missing bean stops the application at startup and a wiring mistake is detected before any request
     * reaches the endpoint.</p>
     *
     * @param authService application service that verifies credentials and signs the token
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Exchanges a username and password for a signed bearer token.
     *
     * <p>The body is validated before the service is invoked, so an empty username or a password outside
     * the accepted range is rejected with a {@code 400} without touching the database. When the
     * credentials are correct the service returns a token whose subject is the username and whose
     * {@code roles} claim carries the authority used by the resource server.</p>
     *
     * <p>The result is returned as {@code 200} with the token in the body, never in a header or a cookie.
     * That choice avoids the cross-site cookie rules that would otherwise apply between the portals and
     * the API, and keeps the credential under the control of the client, which can discard it when the
     * session ends.</p>
     *
     * @param request credentials supplied by the client; fields are validated by bean validation
     * @return the signed token together with its type, remaining validity and account context
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @PostMapping("/demo-token")
    @Operation(
            summary = "Issue a demonstration bearer token",
            description = "Validates a demonstration account and returns a signed HS256 token valid for the "
                    + "configured time to live. The same error is returned for an unknown user, a disabled "
                    + "account and a wrong password.")
    @ApiResponse(responseCode = "200", description = "Token issued")
    @ApiResponse(responseCode = "400", description = "Malformed or missing credentials")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    public ResponseEntity<DemoTokenResponse> demoToken(@Valid @RequestBody DemoTokenRequest request) {
        return ResponseEntity.ok(authService.issue(request));
    }
}
