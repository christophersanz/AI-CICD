package pe.edu.galaxy.training.ia.ca.infrastructure.security.authn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@link AuthenticationManager} del login con usuario y contraseña. Lo usa solo
 * {@link CredentialsAuthenticatorAdapter}; las peticiones con Bearer las valida el resource server.
 */
@Configuration
public class AuthenticationConfig {

    @Bean
    AuthenticationManager authenticationManager(DbUserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        return create(userDetailsService, passwordEncoder);
    }

    /**
     * {@code DaoAuthenticationProvider} con el {@code DelegatingPasswordEncoder} de la aplicación. Si el
     * usuario no existe compara contra un hash ficticio, para no revelar qué cuentas existen por el tiempo
     * de respuesta.
     */
    static AuthenticationManager create(DbUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsPasswordService(userDetailsService);
        return new ProviderManager(provider);
    }
}
