package pe.edu.galaxy.training.ia.ca.application.dto;

/**
 * Credenciales del administrador inicial, tomadas de las variables de entorno de arranque (BR-USR-007). Su
 * {@code toString()} oculta la contraseña.
 * @param username nombre de usuario, aún sin normalizar
 * @param password contraseña en claro
 */
public record BootstrapAdministratorCommand(String username, String password) {

    @Override
    public String toString() {
        return "BootstrapAdministratorCommand[username=%s, password=****]".formatted(username);
    }
}
