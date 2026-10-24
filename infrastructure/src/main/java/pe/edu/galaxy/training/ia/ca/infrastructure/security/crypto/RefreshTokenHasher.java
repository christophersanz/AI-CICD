package pe.edu.galaxy.training.ia.ca.infrastructure.security.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

/**
 * Genera refresh tokens opacos de 256 bits y calcula su SHA-256. No se usa BCrypt: el valor tiene
 * entropía suficiente para que la fuerza bruta sea inviable y el hash debe permitir búsqueda por índice.
 */
@Component
public class RefreshTokenHasher {

    private static final int TOKEN_BYTES = 32;

    private final SecureRandom random = new SecureRandom();

    /** Valor Base64URL sin relleno (43 caracteres) que se entrega al cliente. */
    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** SHA-256 en hexadecimal en minúsculas (64 caracteres); es lo único que se persiste. */
    public String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
