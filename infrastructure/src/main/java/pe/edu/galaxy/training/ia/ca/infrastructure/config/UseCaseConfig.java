package pe.edu.galaxy.training.ia.ca.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.edu.galaxy.training.ia.ca.application.port.in.AuthenticateUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.BootstrapAdministratorUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.CreateUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetCurrentUserUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.GetEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.ListEmployeesUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.LogoutUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.RefreshTokenUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.RegisterEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.in.UpdateEmployeeUseCase;
import pe.edu.galaxy.training.ia.ca.application.port.out.CredentialsAuthenticatorPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.EmployeeRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.PasswordHasherPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RefreshTokenRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.RoleRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.TokenIssuerPort;
import pe.edu.galaxy.training.ia.ca.application.port.out.UserRepositoryPort;
import pe.edu.galaxy.training.ia.ca.application.usecase.AuthenticateUserService;
import pe.edu.galaxy.training.ia.ca.application.usecase.BootstrapAdministratorService;
import pe.edu.galaxy.training.ia.ca.application.usecase.CreateUserService;
import pe.edu.galaxy.training.ia.ca.application.usecase.GetCurrentUserService;
import pe.edu.galaxy.training.ia.ca.application.usecase.GetEmployeeService;
import pe.edu.galaxy.training.ia.ca.application.usecase.ListEmployeesService;
import pe.edu.galaxy.training.ia.ca.application.usecase.LogoutService;
import pe.edu.galaxy.training.ia.ca.application.usecase.RefreshTokenService;
import pe.edu.galaxy.training.ia.ca.application.usecase.RegisterEmployeeService;
import pe.edu.galaxy.training.ia.ca.application.usecase.UpdateEmployeeService;

/** Registra los casos de uso como beans; {@code application} no usa anotaciones de Spring (regla 3). */
@Configuration
public class UseCaseConfig {

    @Bean
    RegisterEmployeeUseCase registerEmployeeUseCase(EmployeeRepositoryPort employeeRepository) {
        return new RegisterEmployeeService(employeeRepository);
    }

    @Bean
    UpdateEmployeeUseCase updateEmployeeUseCase(EmployeeRepositoryPort employeeRepository) {
        return new UpdateEmployeeService(employeeRepository);
    }

    @Bean
    GetEmployeeUseCase getEmployeeUseCase(EmployeeRepositoryPort employeeRepository) {
        return new GetEmployeeService(employeeRepository);
    }

    @Bean
    ListEmployeesUseCase listEmployeesUseCase(EmployeeRepositoryPort employeeRepository) {
        return new ListEmployeesService(employeeRepository);
    }

    @Bean
    AuthenticateUserUseCase authenticateUserUseCase(CredentialsAuthenticatorPort credentialsAuthenticator,
                                                    RoleRepositoryPort roleRepository, TokenIssuerPort tokenIssuer,
                                                    RefreshTokenRepositoryPort refreshTokenRepository, Clock clock) {
        return new AuthenticateUserService(credentialsAuthenticator, roleRepository, tokenIssuer,
                refreshTokenRepository, clock);
    }

    @Bean
    RefreshTokenUseCase refreshTokenUseCase(RefreshTokenRepositoryPort refreshTokenRepository,
                                            UserRepositoryPort userRepository, RoleRepositoryPort roleRepository,
                                            TokenIssuerPort tokenIssuer, Clock clock) {
        return new RefreshTokenService(refreshTokenRepository, userRepository, roleRepository, tokenIssuer, clock);
    }

    @Bean
    LogoutUseCase logoutUseCase(RefreshTokenRepositoryPort refreshTokenRepository, TokenIssuerPort tokenIssuer,
                                Clock clock) {
        return new LogoutService(refreshTokenRepository, tokenIssuer, clock);
    }

    @Bean
    GetCurrentUserUseCase getCurrentUserUseCase(UserRepositoryPort userRepository,
                                                RoleRepositoryPort roleRepository) {
        return new GetCurrentUserService(userRepository, roleRepository);
    }

    @Bean
    CreateUserUseCase createUserUseCase(UserRepositoryPort userRepository, RoleRepositoryPort roleRepository,
                                        PasswordHasherPort passwordHasher) {
        return new CreateUserService(userRepository, roleRepository, passwordHasher);
    }

    @Bean
    BootstrapAdministratorUseCase bootstrapAdministratorUseCase(UserRepositoryPort userRepository,
                                                                PasswordHasherPort passwordHasher) {
        return new BootstrapAdministratorService(userRepository, passwordHasher);
    }
}
