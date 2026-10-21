package co.com.bits.colsubsidio.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import co.com.bits.colsubsidio.auth.api.dto.DemoTokenRequest;
import co.com.bits.colsubsidio.auth.api.dto.DemoTokenResponse;
import co.com.bits.colsubsidio.auth.domain.AppUser;
import co.com.bits.colsubsidio.auth.domain.AppUserRepository;
import co.com.bits.colsubsidio.auth.domain.Role;
import co.com.bits.colsubsidio.config.AppSecurityProperties;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>The encoder of passwords is the real implementation, because the comparison with a BCrypt digest is
 * the behaviour under test. The repository and the encoder of tokens are doubles, so the tests do not
 * need a database nor the configured secret.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String PASSWORD = "Demo123!";

    @Mock
    private AppUserRepository users;

    @Mock
    private JwtEncoder jwtEncoder;

    private PasswordEncoder passwordEncoder;
    private AuthService service;

    /**
     * Builds the service with a real BCrypt encoder and a two hour token lifetime.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(4);
        AppSecurityProperties properties = new AppSecurityProperties(
                new AppSecurityProperties.Jwt("colsubsidio-demo-platform", "secret-with-at-least-32-bytes-length", Duration.ofHours(2)),
                List.of());
        service = new AuthService(users, passwordEncoder, jwtEncoder, properties);
    }

    /**
     * Verifies that a valid credential returns a bearer token with the account context.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("returns a token for a valid credential")
    void issuesTokenForValidCredential() {
        AppUser user = new AppUser("afiliado.demo", passwordEncoder.encode(PASSWORD), Role.ROLE_AFFILIATE, 1L, null);
        when(users.findByUsername("afiliado.demo")).thenReturn(Optional.of(user));
        Jwt jwt = org.mockito.Mockito.mock(Jwt.class);
        when(jwt.getTokenValue()).thenReturn("signed-token");
        when(jwtEncoder.encode(any())).thenReturn(jwt);

        DemoTokenResponse response = service.issue(new DemoTokenRequest("afiliado.demo", PASSWORD));

        assertThat(response.accessToken()).isEqualTo("signed-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.role()).isEqualTo("ROLE_AFFILIATE");
        assertThat(response.affiliateId()).isEqualTo(1L);
        assertThat(response.expiresInSeconds()).isEqualTo(7200L);
    }

    /**
     * Verifies that a wrong password is rejected with an authentication failure.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("rejects a wrong password")
    void rejectsWrongPassword() {
        AppUser user = new AppUser("afiliado.demo", passwordEncoder.encode(PASSWORD), Role.ROLE_AFFILIATE, 1L, null);
        when(users.findByUsername("afiliado.demo")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.issue(new DemoTokenRequest("afiliado.demo", "Otra123!")))
                .isInstanceOf(BadCredentialsException.class);
    }

    /**
     * Verifies that an unknown user produces the same failure as a wrong password.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("rejects an unknown user without revealing it")
    void rejectsUnknownUser() {
        when(users.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.issue(new DemoTokenRequest("nadie", PASSWORD)))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    /**
     * Verifies that a disabled account is rejected even with the correct password.
     *
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("rejects a disabled account")
    void rejectsDisabledAccount() {
        AppUser user = new AppUser("afiliado.demo", passwordEncoder.encode(PASSWORD), Role.ROLE_AFFILIATE, 1L, null);
        user.setEnabled(false);
        when(users.findByUsername("afiliado.demo")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.issue(new DemoTokenRequest("afiliado.demo", PASSWORD)))
                .isInstanceOf(BadCredentialsException.class);
    }
}
