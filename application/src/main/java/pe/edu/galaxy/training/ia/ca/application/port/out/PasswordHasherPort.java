package pe.edu.galaxy.training.ia.ca.application.port.out;

import pe.edu.galaxy.training.ia.ca.domain.identity.vo.PasswordHash;

/** Hashea contraseñas con el algoritmo configurado; el resultado lleva el prefijo del algoritmo. */
@FunctionalInterface
public interface PasswordHasherPort {

    /**
     * Calcula el hash de una contraseña que ya cumple la política (BR-USR-003).
     * @param rawPassword contraseña en claro; nunca se registra ni se guarda
     * @return el hash con el prefijo del algoritmo, por ejemplo {@code {bcrypt}...}
     */
    PasswordHash hash(String rawPassword);
}
