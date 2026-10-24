package pe.edu.galaxy.training.ia.ca.application.dto;

public record RefreshTokenCommand(String refreshToken) {

    @Override
    public String toString() {
        return "RefreshTokenCommand[refreshToken=****]";
    }
}
