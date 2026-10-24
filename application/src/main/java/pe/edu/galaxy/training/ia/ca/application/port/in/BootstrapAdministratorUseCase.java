package pe.edu.galaxy.training.ia.ca.application.port.in;

import pe.edu.galaxy.training.ia.ca.application.dto.BootstrapAdministratorCommand;

@FunctionalInterface
public interface BootstrapAdministratorUseCase {

    /** @return {@code true} si creó el administrador; {@code false} si ya existía uno (BR-USR-007) */
    boolean bootstrap(BootstrapAdministratorCommand command);
}
