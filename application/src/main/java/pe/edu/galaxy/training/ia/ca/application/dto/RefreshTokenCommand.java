package pe.edu.galaxy.training.ia.ca.application.dto;

/**
 * Petición de rotación. Su {@code toString()} oculta el token.
 * @param refreshToken valor opaco recibido del cliente
 */
public record RefreshTokenCommand(String refreshToken) {

    @Override
    public String toString() {
        return "RefreshTokenCommand[refreshToken=****]";
    }
}
