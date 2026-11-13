package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.Set;

/**
 * Datos para crear un usuario. Su {@code toString()} oculta la contraseña.
 * @param username nombre de usuario, aún sin normalizar
 * @param password contraseña en claro
 * @param roles nombres de los roles que se le asignan
 */
public record CreateUserCommand(String username, String password, Set<String> roles) {

    @Override
    public String toString() {
        return "CreateUserCommand[username=%s, password=****, roles=%s]".formatted(username, roles);
    }
}
