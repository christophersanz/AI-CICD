package pe.edu.galaxy.training.ia.ca.infrastructure.api.rest;

import org.mapstruct.Mapper;
import pe.edu.galaxy.training.ia.ca.application.dto.CreateUserCommand;
import pe.edu.galaxy.training.ia.ca.application.dto.UserResult;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.CreateUserRequest;
import pe.edu.galaxy.training.ia.ca.infrastructure.api.dto.UserResponse;

@Mapper
public interface UserApiMapper {

    CreateUserCommand toCommand(CreateUserRequest request);

    UserResponse toResponse(UserResult result);
}
