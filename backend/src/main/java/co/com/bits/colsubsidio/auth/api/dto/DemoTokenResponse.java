package co.com.bits.colsubsidio.auth.api.dto;

/**
 * Result of a successful authentication: the signed token and the context needed by the client to decide
 * which portal to render.
 *
 * <p>The record carries no refresh token and no password material. The prototype issues short lived
 * access tokens signed with HS256 by the same application that validates them, which is enough for a
 * demonstration and avoids depending on an external identity provider. The {@code role} and the optional
 * identifiers let the shell route the user without decoding the token on the client side, while the
 * authoritative check always happens on the server.</p>
 *
 * <p>The expiration is reported both inside the token and as a plain number of seconds, so a client can
 * schedule a renewal without parsing the JWT. The token itself is a bearer credential: anyone who reads
 * it can use it until it expires, which is why it is returned in the body only, is never written to a log
 * and is expected to be held in memory by the portal rather than in a persistent store.</p>
 *
 * @param accessToken      signed JWT in compact serialisation; bearer credential for the API
 * @param tokenType        scheme to use in the {@code Authorization} header, always {@code Bearer}
 * @param expiresInSeconds remaining validity of the token, counted from the moment it was issued
 * @param role             authority granted to the account, useful to route the user to the right portal
 * @param affiliateId      affiliate linked to the account, or {@code null} when the role has no affiliate
 * @param companyId        company linked to the account, or {@code null} when the role has no company
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
public record DemoTokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String role,
        Long affiliateId,
        Long companyId) {
}
