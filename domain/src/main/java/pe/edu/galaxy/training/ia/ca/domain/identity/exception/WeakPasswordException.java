package pe.edu.galaxy.training.ia.ca.domain.identity.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/**
 * La contraseña falta, no tiene entre 12 y 64 caracteres o supera 72 bytes en UTF-8 (BR-USR-003, {@code AUTH-008},
 * HTTP 422).
 */
public class WeakPasswordException extends DomainException {

    /**
     * Crea el error con el código {@code AUTH-008}.
     *
     * @param message qué límite se incumplió, sin incluir la contraseña
     */
    public WeakPasswordException(String message) {
        super("AUTH-008", message);
    }
}
