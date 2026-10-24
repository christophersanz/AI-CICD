package pe.edu.galaxy.training.ia.ca.infrastructure.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.handler.ProblemDetailsAccessDeniedHandler;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.handler.ProblemDetailsAuthenticationEntryPoint;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.handler.SecurityProblemWriter;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt.JwtConfig;

/**
 * Dos cadenas: la pública (login, refresh, OpenAPI y health) y la privada (todo lo demás, con JWT).
 * Lo que no se declara se deniega.
 *
 * <p>CSRF desactivado: la API es stateless y el token viaja en la cabecera {@code Authorization},
 * que el navegador no envía por su cuenta; no hay cookie de sesión que un sitio ajeno pueda aprovechar.
 */
@Configuration
@EnableWebSecurity
@Import(JwtConfig.class)
public class SecurityFilterChainConfig {

    static final String[] PUBLIC_PATHS = {
            "/api/v1/auth/login",
            "/api/v1/auth/refresh",
            "/v3/api-docs", "/v3/api-docs/**",
            "/swagger-ui.html", "/swagger-ui/**",
            "/actuator/health", "/actuator/health/**"
    };

    static final String[] USER_MANAGEMENT_PATHS = {"/api/v1/users", "/api/v1/users/**"};

    @Bean
    SecurityProblemWriter securityProblemWriter(ObjectMapper objectMapper) {
        return new SecurityProblemWriter(objectMapper);
    }

    @Bean
    ProblemDetailsAuthenticationEntryPoint authenticationEntryPoint(SecurityProblemWriter writer) {
        return new ProblemDetailsAuthenticationEntryPoint(writer);
    }

    @Bean
    ProblemDetailsAccessDeniedHandler accessDeniedHandler(SecurityProblemWriter writer) {
        return new ProblemDetailsAccessDeniedHandler(writer);
    }

    @Bean
    @Order(1)
    SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        return stateless(http)
                .securityMatcher(PUBLIC_PATHS)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain privateChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter,
                                     ProblemDetailsAuthenticationEntryPoint authenticationEntryPoint,
                                     ProblemDetailsAccessDeniedHandler accessDeniedHandler) throws Exception {
        return stateless(http)
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        // Temporal en la fase 1 (decisión del paso 0): sin ella cualquier usuario autenticado
                        // podría crearse un ADMIN. TODO(fase 2, paso 2.1): sustituir por @PreAuthorize.
                        .requestMatchers(USER_MANAGEMENT_PATHS).hasAuthority("user:manage")
                        .requestMatchers("/api/v1/**").authenticated()
                        .requestMatchers("/actuator/**").hasAuthority("system:monitor")
                        .anyRequest().denyAll())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .build();
    }

    private static HttpSecurity stateless(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }
}
