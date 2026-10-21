package co.com.bits.colsubsidio.auth.application;

import co.com.bits.colsubsidio.auth.api.dto.DemoTokenRequest;
import co.com.bits.colsubsidio.auth.api.dto.DemoTokenResponse;
import co.com.bits.colsubsidio.auth.domain.AppUser;
import co.com.bits.colsubsidio.auth.domain.AppUserRepository;
import co.com.bits.colsubsidio.config.AppSecurityProperties;
import java.time.Instant;
import java.util.List;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authenticates the demonstration accounts and issues the signed token that the portals use.
 *
 * <p>The prototype signs its own tokens with HS256 instead of delegating to an identity provider. That
 * decision keeps the exercise runnable without an Azure subscription while preserving the shape of a
 * real OAuth2 resource server: the API validates a bearer token, reads the authorities from the
 * {@code roles} claim and never trusts an identifier sent by the client. Swapping this service for an
 * external issuer later only changes where the token comes from, not how it is consumed.</p>
 *
 * <p>Two rules are deliberate. First, a missing account and a wrong password produce the same error, so
 * the response cannot be used to discover which usernames exist. Second, a disabled account is rejected
 * even when the password is correct, because the flag is part of the authorization decision and not a
 * cosmetic field.</p>
 *
 * <p>The class performs no logging of credentials and no comparison of hashes in memory beyond the
 * constant time check delegated to the encoder. It holds no mutable state and is safe to share across
 * concurrent requests.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@Service
public class AuthService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final AppSecurityProperties properties;

    /**
     * Creates the service with the collaborators it needs to verify a credential and sign a token.
     *
     * <p>Every dependency is received as an interface or a configuration record, so the class can be
     * instantiated in a plain unit test with doubles for the repository, the password encoder and the
     * encoder of tokens. The references are assigned once and never reassigned, which makes the instance
     * effectively immutable.</p>
     *
     * <p>The constructor performs no I/O: it does not open a transaction, does not read the database and
     * does not validate the configuration beyond what Spring has already bound. A missing bean fails at
     * context startup rather than at the first login attempt.</p>
     *
     * @param users           repository of demonstration accounts; never {@code null}
     * @param passwordEncoder BCrypt encoder used to compare the supplied password with the stored digest
     * @param jwtEncoder      encoder used to sign the resulting token with the configured secret
     * @param properties      typed security configuration providing issuer, time to live and demo users
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    public AuthService(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            AppSecurityProperties properties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    /**
     * Verifies a username and password and returns a signed token with the account context.
     *
     * <p>The lookup is by username, then the account must be enabled, then the password is compared with
     * the stored BCrypt digest. The three checks fail with the same exception and the same message, so a
     * caller cannot tell an unknown user from a wrong password or from a disabled account. That property
     * is what prevents the endpoint from becoming a directory of valid usernames.</p>
     *
     * <p>The token carries the subject, the authorities in the {@code roles} claim and the optional
     * affiliate and company identifiers. The claims are only added when they have a value, because a JSON
     * {@code null} would be read by the resource server as an absent claim and would produce confusing
     * behaviour in the expressions that check ownership.</p>
     *
     * <p>The method is read only: it never updates the account, never records the last login and never
     * stores the token. Expiration is computed from the configured time to live and reported to the
     * client in seconds, so the portal can renew before the token stops being accepted.</p>
     *
     * @param request credentials supplied by the client; already validated at the HTTP boundary
     * @return the signed token together with its type, remaining validity and account context
     * @throws BadCredentialsException when the account does not exist, is disabled or the password does
     *                                 not match; the message is intentionally identical in the three cases
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Transactional(readOnly = true)
    public DemoTokenResponse issue(DemoTokenRequest request) {
        AppUser user = users.findByUsername(request.username())
                .filter(AppUser::isEnabled)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.jwt().ttl());

        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer(properties.jwt().issuer())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getUsername())
                .claim("roles", List.of(user.getRole().name()));

        if (user.getAffiliateId() != null) {
            claims.claim("affiliateId", user.getAffiliateId());
        }
        if (user.getCompanyId() != null) {
            claims.claim("companyId", user.getCompanyId());
        }

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();

        return new DemoTokenResponse(
                token,
                "Bearer",
                properties.jwt().ttl().toSeconds(),
                user.getRole().name(),
                user.getAffiliateId(),
                user.getCompanyId());
    }
}
