package pe.edu.galaxy.training.ia.ca.application.dto;

public record BootstrapAdministratorCommand(String username, String password) {

    @Override
    public String toString() {
        return "BootstrapAdministratorCommand[username=%s, password=****]".formatted(username);
    }
}
