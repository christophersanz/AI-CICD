package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import java.nio.charset.StandardCharsets;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.WeakPasswordException;

/**
 * Política de contraseñas (BR-USR-003): 12–64 caracteres y como máximo 72 bytes en UTF-8,
 * el límite que procesa BCrypt. Sin reglas de composición (NIST SP 800-63B).
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 12;
    public static final int MAX_LENGTH = 64;
    public static final int MAX_BYTES = 72;

    private PasswordPolicy() {
    }

    public static void validate(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new WeakPasswordException("Password is required.");
        }
        String password = rawPassword.toString();
        int length = password.codePointCount(0, password.length());
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new WeakPasswordException(
                    "Password must have between %d and %d characters.".formatted(MIN_LENGTH, MAX_LENGTH));
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new WeakPasswordException("Password must not exceed %d bytes in UTF-8.".formatted(MAX_BYTES));
        }
    }
}
