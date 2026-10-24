package pe.edu.galaxy.training.ia.ca.application.dto;

import java.util.Set;

public record CreateUserCommand(String username, String password, Set<String> roles) {

    @Override
    public String toString() {
        return "CreateUserCommand[username=%s, password=****, roles=%s]".formatted(username, roles);
    }
}
