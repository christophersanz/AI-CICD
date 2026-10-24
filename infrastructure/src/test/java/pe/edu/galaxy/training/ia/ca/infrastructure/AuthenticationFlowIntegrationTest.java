package pe.edu.galaxy.training.ia.ca.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto.RefreshTokenHasher;

/**
 * Flujo completo de la fase 1 de seguridad contra PostgreSQL, con las cadenas de filtros, el
 * {@code JwtDecoder} y los casos de uso reales (perfil {@code local}, clave efímera). Se omite si
 * Docker no está disponible.
 */
@SpringBootTest(properties = {
        "security.bootstrap.admin.username=" + AuthenticationFlowIntegrationTest.ADMIN,
        "security.bootstrap.admin.password=" + AuthenticationFlowIntegrationTest.ADMIN_PASSWORD
})
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class AuthenticationFlowIntegrationTest {

    static final String ADMIN = "flow.admin";
    static final String ADMIN_PASSWORD = "flow-admin-password-2026";
    private static final String USER_PASSWORD = "flow-user-password-2026";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private RefreshTokenHasher refreshTokenHasher;

    // --- helpers ---

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, Object body) throws Exception {
        return request.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(body));
    }

    private static MockHttpServletRequestBuilder bearer(MockHttpServletRequestBuilder request, String accessToken) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
    }

    private ResultActions login(String username, String password) throws Exception {
        return mockMvc.perform(json(post("/api/v1/auth/login"), Map.of("username", username, "password", password)));
    }

    private ResultActions refresh(String refreshToken) throws Exception {
        return mockMvc.perform(json(post("/api/v1/auth/refresh"), Map.of("refreshToken", refreshToken)));
    }

    private JsonNode body(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private JsonNode loginOk(String username, String password) throws Exception {
        return body(login(username, password).andExpect(status().isOk()));
    }

    private void createUser(String accessToken, String username, String role) throws Exception {
        mockMvc.perform(bearer(json(post("/api/v1/users"),
                        Map.of("username", username, "password", USER_PASSWORD, "roles", List.of(role))), accessToken))
                .andExpect(status().isCreated());
    }

    private List<Map<String, Object>> family(String rawRefreshToken) {
        return jdbc.queryForList("""
                SELECT revoked_at, revocation_reason FROM refresh_tokens
                WHERE family_id = (SELECT family_id FROM refresh_tokens WHERE token_hash = ?)
                """, refreshTokenHasher.hash(rawRefreshToken));
    }

    // --- tests ---

    @Test
    void shouldCompleteLoginRegisterRefreshReuseAndLogoutFlowWhenAdministratorUsesTheApi() throws Exception {
        // Given: login del administrador inicial (BR-USR-007)
        JsonNode first = loginOk(ADMIN, ADMIN_PASSWORD);
        assertThat(first.get("tokenType").asText()).isEqualTo("Bearer");
        assertThat(first.get("expiresIn").asLong()).isBetween(890L, 900L);

        // When: registra un empleado con el Bearer recibido
        mockMvc.perform(bearer(json(post("/api/v1/employees"), Map.of("documentType", "DNI",
                        "documentNumber", "45678901", "firstNames", "Ana", "lastNames", "Pérez",
                        "email", "ana.flow@example.com")), first.get("accessToken").asText()))
                .andExpect(status().isCreated());

        // When: rota el refresh token (BR-USR-006)
        String firstRefresh = first.get("refreshToken").asText();
        JsonNode second = body(refresh(firstRefresh).andExpect(status().isOk()));
        String secondRefresh = second.get("refreshToken").asText();
        assertThat(secondRefresh).isNotEqualTo(firstRefresh);
        mockMvc.perform(bearer(get("/api/v1/auth/me"), second.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(ADMIN));

        // When: se reutiliza el refresh ya usado → 401 y toda la familia queda revocada
        refresh(firstRefresh)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-005"));
        assertThat(family(firstRefresh)).hasSize(2)
                .allSatisfy(token -> assertThat(token.get("revoked_at")).isNotNull());
        assertThat(family(secondRefresh)).extracting(token -> token.get("revocation_reason"))
                .contains("REUSE_DETECTED");
        refresh(secondRefresh)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-005"));

        // Then: logout es idempotente aunque la familia ya esté revocada
        mockMvc.perform(bearer(json(post("/api/v1/auth/logout"), Map.of("refreshToken", secondRefresh)),
                        second.get("accessToken").asText()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRevokeFamilyWhenUserLogsOut() throws Exception {
        // Given
        JsonNode tokens = loginOk(ADMIN, ADMIN_PASSWORD);
        String refreshToken = tokens.get("refreshToken").asText();

        // When
        mockMvc.perform(bearer(json(post("/api/v1/auth/logout"), Map.of("refreshToken", refreshToken)),
                        tokens.get("accessToken").asText()))
                .andExpect(status().isNoContent());

        // Then
        assertThat(family(refreshToken)).extracting(token -> token.get("revocation_reason")).containsOnly("LOGOUT");
        refresh(refreshToken).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.code").value("AUTH-005"));
    }

    @Test
    void shouldRespondInvalidCredentialsWhenPasswordIsWrongOrUserDoesNotExist() throws Exception {
        login(ADMIN, "not-the-admin-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"));
        login("nobody.here", ADMIN_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"));
        login("x", ADMIN_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"));
    }

    @Test
    void shouldRejectLoginAndRefreshWhenUserIsDisabled() throws Exception {
        // Given: usuario creado por la API con una sesión abierta
        String adminToken = loginOk(ADMIN, ADMIN_PASSWORD).get("accessToken").asText();
        createUser(adminToken, "disabled.user", "EMPLOYEE");
        String refreshToken = loginOk("disabled.user", USER_PASSWORD).get("refreshToken").asText();

        // When: se deshabilita (aún no hay caso de uso; llega en el paso 2.2)
        jdbc.update("UPDATE users SET enabled = FALSE WHERE username = 'disabled.user'");

        // Then: mismo error que credenciales inválidas, y el refresh revoca la familia (BR-USR-004)
        login("disabled.user", USER_PASSWORD)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-002"));
        refresh(refreshToken)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-005"));
        assertThat(family(refreshToken)).extracting(token -> token.get("revocation_reason"))
                .containsOnly("USER_DISABLED");
    }

    @Test
    void shouldRejectDuplicateUsernameIgnoringCaseWhenUserIsCreated() throws Exception {
        // Given
        String adminToken = loginOk(ADMIN, ADMIN_PASSWORD).get("accessToken").asText();
        createUser(adminToken, "case.user", "HR");

        // When / Then (BR-USR-001)
        mockMvc.perform(bearer(json(post("/api/v1/users"),
                        Map.of("username", "Case.User", "password", USER_PASSWORD, "roles", List.of("HR"))), adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH-007"));
    }

    @Test
    void shouldCarryRolePermissionsInTokenAndForbidUserManagementWhenUserIsHr() throws Exception {
        // Given
        String adminToken = loginOk(ADMIN, ADMIN_PASSWORD).get("accessToken").asText();
        createUser(adminToken, "hr.user", "HR");
        String hrToken = loginOk("HR.User", USER_PASSWORD).get("accessToken").asText();

        // When / Then: los permisos del token salen de role_permissions (V3)
        mockMvc.perform(bearer(get("/api/v1/auth/me"), hrToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("HR"))
                .andExpect(jsonPath("$.permissions").value(org.hamcrest.Matchers.hasItem("employee:create")))
                .andExpect(jsonPath("$.permissions").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.hasItem("user:manage"))));
        mockMvc.perform(bearer(json(post("/api/v1/users"),
                        Map.of("username", "sneaky.admin", "password", USER_PASSWORD, "roles", List.of("ADMIN"))), hrToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void shouldRequireTokenWhenPrivateEndpointIsCalledWithoutIt() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    void shouldPublishBearerSchemeOnlyOnPrivateOperationsWhenOpenApiIsRequested() throws Exception {
        // When
        JsonNode docs = body(mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk()));

        // Then
        JsonNode scheme = docs.at("/components/securitySchemes/bearerAuth");
        assertThat(scheme.get("type").asText()).isEqualTo("http");
        assertThat(scheme.get("scheme").asText()).isEqualTo("bearer");
        assertThat(scheme.get("bearerFormat").asText()).isEqualTo("JWT");
        assertThat(docs.at("/paths/~1api~1v1~1auth~1me/get/security").toString()).contains("bearerAuth");
        assertThat(docs.at("/paths/~1api~1v1~1users/post/security").toString()).contains("bearerAuth");
        assertThat(docs.at("/paths/~1api~1v1~1employees/post/security").toString()).contains("bearerAuth");
        assertThat(docs.at("/paths/~1api~1v1~1auth~1login/post/security").isMissingNode()).isTrue();
        assertThat(docs.at("/paths/~1api~1v1~1auth~1refresh/post/security").isMissingNode()).isTrue();
    }
}
