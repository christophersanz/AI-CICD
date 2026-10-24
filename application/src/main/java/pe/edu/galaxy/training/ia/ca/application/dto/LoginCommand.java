package pe.edu.galaxy.training.ia.ca.application.dto;

public record LoginCommand(String username, String password) {

    @Override
    public String toString() {
        return "LoginCommand[username=%s, password=****]".formatted(username);
    }
}
