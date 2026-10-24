package pe.edu.galaxy.training.ia.ca.application.port.out;

import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;

/** Hashea contraseñas con el algoritmo configurado; el resultado lleva el prefijo del algoritmo. */
@FunctionalInterface
public interface PasswordHasherPort {

    PasswordHash hash(String rawPassword);
}
