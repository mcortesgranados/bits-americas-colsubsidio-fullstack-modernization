package co.com.bits.colsubsidio.auth.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.bits.colsubsidio.auth.api.dto.DemoTokenResponse;
import co.com.bits.colsubsidio.auth.application.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web layer tests for {@link AuthController}.
 *
 * <p>The slice loads only the controller and the exception advice; the application service is replaced by
 * a double and the security filters are disabled, so the tests describe the HTTP contract without
 * depending on the token infrastructure.</p>
 *
 * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
 * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    /**
     * Verifies that valid credentials produce a token in the response body.
     *
     * @throws Exception when the request cannot be executed by the test infrastructure
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("returns 200 with the token for valid credentials")
    void returnsToken() throws Exception {
        when(authService.issue(any())).thenReturn(
                new DemoTokenResponse("signed-token", "Bearer", 7200L, "ROLE_AFFILIATE", 1L, null));

        mockMvc.perform(post("/api/v1/auth/demo-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"afiliado.demo\",\"password\":\"Demo123!\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("signed-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.affiliateId").value(1));
    }

    /**
     * Verifies that a password shorter than the accepted minimum never reaches the service.
     *
     * @throws Exception when the request cannot be executed by the test infrastructure
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("returns 400 when the password does not meet the length rules")
    void rejectsShortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/demo-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"afiliado.demo\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    /**
     * Verifies that invalid credentials are translated into an unauthorised problem detail.
     *
     * @throws Exception when the request cannot be executed by the test infrastructure
     * @author Manuela Cortés Granados (manuelacortesgranados@gmail)
     * @since 2026-10-20 11:42 GMT-5 Bogotá D.C. Colombia
     */
    @Test
    @DisplayName("returns 401 when the credentials are rejected")
    void rejectsInvalidCredentials() throws Exception {
        when(authService.issue(any())).thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/demo-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"afiliado.demo\",\"password\":\"Demo123!\"}"))
                .andExpect(status().isUnauthorized());
    }
}
