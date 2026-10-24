package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserQuery;
import pe.edu.galaxy.training.ia.ca.application.dto.CurrentUserResult;
import pe.edu.galaxy.training.ia.ca.application.dto.LoginCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.LogoutCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.RefreshTokenCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.TokenPairResult;
import pe.edu.galaxy.training.ia.ca.application.exception.InvalidCredentialsException;
import pe.edu.galaxy.training.ia.ca.application.exception.UserNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.in.AuthenticateUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetCurrentUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.LogoutUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.RefreshTokenUseCase;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.InvalidRefreshTokenException;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.UserId;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityFilterChainConfig;

@WebMvcTest(AuthController.class)
@Import({AuthApiMapper.class, SecurityFilterChainConfig.class})
class AuthControllerTest {

    private static final Instant NOW = Instant.parse("2026-10-16T10:00:00Z");
    private static final String LOGIN = "/api/v1/auth/login";
    private static final String REFRESH = "/api/v1/auth/refresh";
    private static final String LOGOUT = "/api/v1/auth/logout";
    private static final String ME = "/api/v1/auth/me";
    private static final String LOGIN_BODY = """
            {"username": "admin", "password": "a-long-enough-password"}
            """;
    private static final String REFRESH_BODY = """
            {"refreshToken": "opaque-refresh-token"}
            """;
    private static final TokenPairResult TOKENS = new TokenPairResult("access.jwt.value",
            NOW.plus(Duration.ofMinutes(15)), "new-refresh-token", NOW.plus(Duration.ofDays(7)));

    @TestConfiguration
    static class FixedClock {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW, ZoneOffset.UTC);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticateUserUseCase authenticateUserUseCase;

    @MockitoBean
    private RefreshTokenUseCase refreshTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @MockitoBean
    private GetCurrentUserUseCase getCurrentUserUseCase;

    // --- login ---

    @Test
    void shouldReturnTokensWithNoStoreWhenCredentialsAreValid() throws Exception {
        // Given
        when(authenticateUserUseCase.authenticate(any())).thenReturn(TOKENS);

        // When / Then
        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.accessToken").value("access.jwt.value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"))
                .andExpect(jsonPath("$.refreshExpiresIn").value(604800));
        verify(authenticateUserUseCase).authenticate(new LoginCommand("admin", "a-long-enough-password"));
    }

    @Test
    void shouldReturnBadRequestWhenLoginFieldsAreMissing() throws Exception {
        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:invalid-request"))
                .andExpect(jsonPath("$.errors.length()").value(2));
        verifyNoInteractions(authenticateUserUseCase);
    }

    @Test
    void shouldReturnBadRequestWhenLoginBodyIsMalformed() throws Exception {
        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content("{not-json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
        verifyNoInteractions(authenticateUserUseCase);
    }

    @Test
    void shouldReturnUnauthorizedProblemWhenCredentialsAreInvalid() throws Exception {
        // Given
        when(authenticateUserUseCase.authenticate(any())).thenThrow(new InvalidCredentialsException());

        // When / Then
        mockMvc.perform(post(LOGIN).contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:invalid-credentials"))
                .andExpect(jsonPath("$.title").value("Invalid credentials"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.detail").value("Invalid username or password."))
                .andExpect(jsonPath("$.code").value("AUTH-002"));
    }

    @Test
    void shouldIgnoreInvalidBearerWhenLoginIsCalled() throws Exception {
        // Given: la ruta es pública; un token caducado o basura no debe impedir el login
        when(authenticateUserUseCase.authenticate(any())).thenReturn(TOKENS);

        // When / Then
        mockMvc.perform(post(LOGIN).header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt")
                        .contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk());
    }

    // --- refresh ---

    @Test
    void shouldReturnRotatedTokensWithNoStoreWhenRefreshTokenIsValid() throws Exception {
        // Given
        when(refreshTokenUseCase.refresh(any())).thenReturn(TOKENS);

        // When / Then
        mockMvc.perform(post(REFRESH).contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.accessToken").value("access.jwt.value"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
        verify(refreshTokenUseCase).refresh(new RefreshTokenCommand("opaque-refresh-token"));
    }

    @Test
    void shouldReturnBadRequestWhenRefreshTokenIsMissing() throws Exception {
        mockMvc.perform(post(REFRESH).contentType(MediaType.APPLICATION_JSON).content("{\"refreshToken\": \" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("refreshToken"));
        verifyNoInteractions(refreshTokenUseCase);
    }

    @Test
    void shouldReturnUnauthorizedProblemWhenRefreshTokenIsInvalid() throws Exception {
        // Given
        when(refreshTokenUseCase.refresh(any())).thenThrow(new InvalidRefreshTokenException());

        // When / Then
        mockMvc.perform(post(REFRESH).contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:invalid-refresh-token"))
                .andExpect(jsonPath("$.code").value("AUTH-005"));
    }

    // --- logout ---

    @Test
    void shouldRevokeWithUserFromSubjectWhenLogoutIsCalled() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();

        // When / Then
        mockMvc.perform(post(LOGOUT).with(jwt().jwt(token -> token.subject(userId.toString())))
                        .contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isNoContent());
        verify(logoutUseCase).logout(new LogoutCommand(userId, "opaque-refresh-token"));
    }

    @Test
    void shouldReturnBadRequestWhenLogoutBodyIsMissingToken() throws Exception {
        mockMvc.perform(post(LOGOUT).with(jwt().jwt(token -> token.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(logoutUseCase);
    }

    @Test
    void shouldReturnUnauthorizedWhenLogoutHasNoToken() throws Exception {
        mockMvc.perform(post(LOGOUT).contentType(MediaType.APPLICATION_JSON).content(REFRESH_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
        verifyNoInteractions(logoutUseCase);
    }

    // --- me ---

    @Test
    void shouldReturnCurrentUserWhenTokenIsValid() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        when(getCurrentUserUseCase.get(any())).thenReturn(new CurrentUserResult(userId, "admin",
                Set.of("ADMIN"), Set.of("user:manage", "employee:create")));

        // When / Then
        mockMvc.perform(get(ME).with(jwt().jwt(token -> token.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.toString()))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.roles").value(Matchers.contains("ADMIN")))
                .andExpect(jsonPath("$.permissions").value(Matchers.contains("employee:create", "user:manage")));
        ArgumentCaptor<CurrentUserQuery> query = ArgumentCaptor.forClass(CurrentUserQuery.class);
        verify(getCurrentUserUseCase).get(query.capture());
        assertThat(query.getValue().userId()).isEqualTo(userId);
    }

    @Test
    void shouldReturnUnauthorizedWhenMeHasNoToken() throws Exception {
        mockMvc.perform(get(ME))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
        verifyNoInteractions(getCurrentUserUseCase);
    }

    @Test
    void shouldReturnNotFoundWhenUserOfTokenNoLongerExists() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        when(getCurrentUserUseCase.get(any())).thenThrow(new UserNotFoundException(UserId.of(userId)));

        // When / Then
        mockMvc.perform(get(ME).with(jwt().jwt(token -> token.subject(userId.toString()))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:user-not-found"))
                .andExpect(jsonPath("$.code").value("AUTH-404"));
    }
}
