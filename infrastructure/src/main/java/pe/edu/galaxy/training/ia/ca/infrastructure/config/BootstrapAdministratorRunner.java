package pe.edu.galaxy.training.ia.ca.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pe.edu.galaxy.training.ia.ca.application.dto.BootstrapAdministratorCommand;
import pe.edu.galaxy.training.ia.ca.application.exception.ApplicationException;
import pe.edu.galaxy.training.ia.ca.application.port.in.BootstrapAdministratorUseCase;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;

/**
 * Crea el administrador inicial al arrancar si no hay ningún usuario {@code ADMIN} (BR-USR-007).
 * Sin las variables de entorno solo avisa. Si no puede crearlo (username ocupado por otro usuario,
 * contraseña que no cumple BR-USR-003) la aplicación no arranca. Nunca registra la contraseña.
 */
@Component
public class BootstrapAdministratorRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdministratorRunner.class);

    private final BootstrapAdministratorUseCase bootstrapAdministratorUseCase;
    private final SecurityProperties.Admin admin;

    public BootstrapAdministratorRunner(BootstrapAdministratorUseCase bootstrapAdministratorUseCase,
                                        SecurityProperties properties) {
        this.bootstrapAdministratorUseCase = bootstrapAdministratorUseCase;
        this.admin = properties.bootstrap().admin();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!admin.isConfigured()) {
            log.warn("Initial administrator not configured: set SECURITY_BOOTSTRAP_ADMIN_USERNAME and "
                    + "SECURITY_BOOTSTRAP_ADMIN_PASSWORD to create it if no ADMIN user exists.");
            return;
        }
        boolean created;
        try {
            created = bootstrapAdministratorUseCase.bootstrap(
                    new BootstrapAdministratorCommand(admin.username(), admin.password()));
        } catch (DomainException | ApplicationException ex) {
            throw new IllegalStateException("Initial administrator could not be created (%s): %s"
                    .formatted(codeOf(ex), ex.getMessage()), ex);
        }
        if (created) {
            log.info("Initial administrator created.");
        } else {
            log.info("An ADMIN user already exists; initial administrator not created.");
        }
    }

    private static String codeOf(RuntimeException ex) {
        return ex instanceof DomainException domain ? domain.code() : ((ApplicationException) ex).code();
    }
}
