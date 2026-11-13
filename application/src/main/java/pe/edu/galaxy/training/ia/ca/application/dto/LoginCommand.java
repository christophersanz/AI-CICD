package pe.edu.galaxy.training.ia.ca.application.dto;

/**
 * Credenciales de inicio de sesión. Su {@code toString()} oculta la contraseña.
 * @param username nombre de usuario tal como lo escribió la persona
 * @param password contraseña en claro
 */
public record LoginCommand(String username, String password) {

    @Override
    public String toString() {
        return "LoginCommand[username=%s, password=****]".formatted(username);
    }
}
