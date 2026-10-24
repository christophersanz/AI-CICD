package pe.edu.galaxy.training.ia.ca.infrastructure.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeResult;
import pe.edu.galaxy.training.ia.ca.application.port.in.RegisterEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.ListEmployeesUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.UpdateEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.domain.employee.vo.DocumentType;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.rest.EmployeeApiMapperImpl;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.rest.EmployeeController;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityFilterChainConfig;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt.JwtClaims;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt.RsaKeys;

/** Cadenas pública y privada con tokens firmados de verdad: valida firma, exp, iss y aud. */
@WebMvcTest(EmployeeController.class)
@Import({EmployeeApiMapperImpl.class, SecurityFilterChainConfig.class})
class SecurityFilterChainTest {

    private static final String EMPLOYEES = "/api/v1/employees";
    private static final String VALID_BODY = """
            {"documentType": "DNI", "documentNumber": "12345678",
             "firstNames": "Ana", "lastNames": "Pérez", "email": "ana@example.com"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RsaKeys rsaKeys;

    @MockitoBean
    private RegisterEmployeeUseCase registerEmployeeUseCase;

    @MockitoBean
    private UpdateEmployeeUseCase updateEmployeeUseCase;

    @MockitoBean
    private GetEmployeeUseCase getEmployeeUseCase;

    @MockitoBean
    private ListEmployeesUseCase listEmployeesUseCase;

    private String token(UnaryOperator<JwtClaimsSet.Builder> customizer) {
        return sign(rsaKeys.publicKey(), rsaKeys.privateKey(), customizer);
    }

    private static String sign(RSAPublicKey publicKey, RSAPrivateKey privateKey,
                               UnaryOperator<JwtClaimsSet.Builder> customizer) {
        RSAKey key = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("test").build();
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(key)));
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .subject(UUID.randomUUID().toString())
                .issuer("contract-api")
                .audience(List.of("contract-api"))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .claim(JwtClaims.PERMISSIONS, List.of("employee:create"));
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).keyId("test").build();
        return encoder.encode(JwtEncoderParameters.from(header, customizer.apply(claims).build())).getTokenValue();
    }

    private static MockHttpServletRequestBuilder register(String bearer) {
        MockHttpServletRequestBuilder request = post(EMPLOYEES).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY);
        return bearer == null ? request : request.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearer);
    }

    @Test
    void shouldReturnUnauthorizedProblemWhenTokenIsMissing() throws Exception {
        mockMvc.perform(register(null))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:invalid-access-token"))
                .andExpect(jsonPath("$.code").value("AUTH-003"));
        verifyNoInteractions(registerEmployeeUseCase);
    }

    @Test
    void shouldAllowRequestWhenTokenIsValid() throws Exception {
        // Given
        when(registerEmployeeUseCase.register(any())).thenReturn(
                new EmployeeResult(UUID.randomUUID(), DocumentType.DNI, "12345678", "Ana", "Pérez", "ana@example.com"));

        // When / Then
        mockMvc.perform(register(token(claims -> claims)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturnExpiredCodeWhenTokenHasExpired() throws Exception {
        // Given: expirado hace 5 minutos, fuera de la tolerancia de reloj de 60 s
        Instant past = Instant.now().minusSeconds(1200);
        String expired = token(claims -> claims.issuedAt(past).expiresAt(past.plusSeconds(900)));

        // When / Then
        mockMvc.perform(register(expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-004"))
                .andExpect(jsonPath("$.type").value("urn:contract-api:errors:expired-access-token"));
    }

    @Test
    void shouldReturnInvalidCodeWhenAudienceDoesNotMatch() throws Exception {
        mockMvc.perform(register(token(claims -> claims.audience(List.of("another-api")))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    void shouldReturnInvalidCodeWhenIssuerDoesNotMatch() throws Exception {
        mockMvc.perform(register(token(claims -> claims.issuer("someone-else"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    void shouldReturnInvalidCodeWhenTokenIsSignedWithAnotherKey() throws Exception {
        // Given
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var otherPair = generator.generateKeyPair();
        String forged = sign((RSAPublicKey) otherPair.getPublic(), (RSAPrivateKey) otherPair.getPrivate(),
                claims -> claims);

        // When / Then
        mockMvc.perform(register(forged))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
        verifyNoInteractions(registerEmployeeUseCase);
    }

    @Test
    void shouldReturnInvalidCodeWhenTokenIsMalformed() throws Exception {
        mockMvc.perform(register("not-a-jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH-003"));
    }

    @Test
    void shouldNotRequireTokenWhenPathIsPublic() throws Exception {
        // El controller de login llega en el paso 1.8: sin él la ruta pública responde 404, no 401
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnForbiddenProblemWhenActuatorIsCalledWithoutMonitorPermission() throws Exception {
        mockMvc.perform(get("/actuator/metrics").with(jwt()))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.code").value("AUTH-001"));
    }

    @Test
    void shouldPassAuthorizationWhenActuatorIsCalledWithMonitorPermission() throws Exception {
        // Sin actuator en el slice web la petición llega al dispatcher y responde 404
        mockMvc.perform(get("/actuator/metrics")
                        .with(jwt().authorities(new SimpleGrantedAuthority("system:monitor"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDenyPathWhenItIsNotDeclared() throws Exception {
        mockMvc.perform(get("/internal/anything").with(jwt()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH-001"));
    }
}
