package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pe.edu.galaxy.training.ia.ca.application.dto.CreateUserCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.UserResult;
import pe.edu.galaxy.training.ia.ca.application.exception.DuplicateUsernameException;
import pe.edu.galaxy.training.ia.ca.application.exception.RoleNotFoundException;
import pe.edu.galaxy.training.ia.ca.application.port.in.CreateUserUseCase;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.WeakPasswordException;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.RoleName;
import pe.edu.galaxy.training.ia.ca.domain.identity.vo.Username;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityFilterChainConfig;

@WebMvcTest(UserController.class)
@Import({UserApiMapperImpl.class, SecurityFilterChainConfig.class})
class UserControllerTest {

    private static final String URL = "/api/v1/users";
    private static final String VALID_BODY = """
            {"username": "ana.perez", "password": "a-long-enough-password", "roles": ["HR"]}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;

    private static JwtRequestPostProcessor manager() {
        return jwt().authorities(new SimpleGrantedAuthority("user:manage"));
    }

    private static MockHttpServletRequestBuilder create(String body) {
        return post(URL).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void shouldReturnCreatedWithLocationWhenRequestIsValid() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(createUserUseCase.create(any())).thenReturn(new UserResult(id, "ana.perez", Set.of("HR"), true));

        // When / Then
        mockMvc.perform(create(VALID_BODY).with(manager()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/users/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.username").value("ana.perez"))
                .andExpect(jsonPath("$.roles[0]").value("HR"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.password").doesNotExist());
        verify(createUserUseCase).create(new CreateUserCommand("ana.perez", "a-long-enough-password", Set.of("HR")));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredFieldsAreMissing() throws Exception {
        mockMvc.perform(create("{}").with(manager()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errors.length()").value(3));
        verifyNoInteractions(createUserUseCase);
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenIsMissing() throws Exception {
        mockMvc.perform(create(VALID_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
        verifyNoInteractions(createUserUseCase);
    }

    @Test
    void shouldReturnForbiddenWhenTokenLacksUserManagePermission() throws Exception {
        // Regla de URL temporal de la fase 1 (decisión del paso 0)
        mockMvc.perform(create(VALID_BODY).with(jwt().authorities(new SimpleGrantedAuthority("employee:create"))))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTH-001"));
        verifyNoInteractions(createUserUseCase);
    }

    @Test
    void shouldReturnConflictWhenUsernameAlreadyExists() throws Exception {
        // Given
        when(createUserUseCase.create(any())).thenThrow(new DuplicateUsernameException(new Username("ana.perez")));

        // When / Then
        mockMvc.perform(create(VALID_BODY).with(manager()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:duplicate-username"))
                .andExpect(jsonPath("$.code").value("AUTH-007"));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenPasswordIsWeak() throws Exception {
        // Given
        when(createUserUseCase.create(any()))
                .thenThrow(new WeakPasswordException("Password must have between 12 and 64 characters."));

        // When / Then
        mockMvc.perform(create(VALID_BODY).with(manager()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:weak-password"))
                .andExpect(jsonPath("$.code").value("AUTH-008"));
    }

    @Test
    void shouldReturnNotFoundWhenRoleDoesNotExist() throws Exception {
        // Given
        when(createUserUseCase.create(any())).thenThrow(new RoleNotFoundException(new RoleName("AUDITOR")));

        // When / Then
        mockMvc.perform(create(VALID_BODY).with(manager()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:role-not-found"))
                .andExpect(jsonPath("$.code").value("AUTH-009"));
    }

    @Test
    void shouldReturnUnprocessableEntityWhenUsernameFormatIsInvalid() throws Exception {
        // Given
        when(createUserUseCase.create(any())).thenThrow(new InvalidValueException(
                "Username must have 3-50 characters: lowercase letters, digits, '.', '_' or '-'."));

        // When / Then
        mockMvc.perform(create(VALID_BODY).with(manager()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VAL-001"));
    }
}
