package pe.edu.galaxy.training.ia.ca.infrastructure.security.jwt;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.converter.RsaKeyConverters;
import pe.edu.galaxy.training.ia.ca.infrastructure.security.config.SecurityProperties;

/** Par de claves RSA con el que se firman (privada) y validan (pública) los access tokens. */
public record RsaKeys(RSAPublicKey publicKey, RSAPrivateKey privateKey, String keyId) {

    private static final Logger log = LoggerFactory.getLogger(RsaKeys.class);

    /**
     * Carga las claves desde las propiedades. Sin claves configuradas solo arranca si
     * {@code generate-ephemeral-key} está activo (perfil {@code local}).
     */
    public static RsaKeys from(SecurityProperties.Jwt jwt) {
        if (hasText(jwt.publicKey()) && hasText(jwt.privateKey())) {
            return new RsaKeys(RsaKeyConverters.x509().convert(pem(jwt.publicKey())),
                    RsaKeyConverters.pkcs8().convert(pem(jwt.privateKey())), jwt.keyId());
        }
        if (!jwt.generateEphemeralKey()) {
            throw new IllegalStateException(
                    "JWT keys are not configured: set SECURITY_JWT_PUBLIC_KEY and SECURITY_JWT_PRIVATE_KEY.");
        }
        log.warn("Using an ephemeral RSA key pair: access tokens become invalid on restart. Local use only.");
        KeyPair pair = generate();
        return new RsaKeys((RSAPublicKey) pair.getPublic(), (RSAPrivateKey) pair.getPrivate(), jwt.keyId());
    }

    private static KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA is not available", e);
        }
    }

    /** Admite saltos de línea reales o escapados ({@code \n}), habituales en variables de entorno. */
    private static InputStream pem(String value) {
        return new ByteArrayInputStream(value.replace("\\n", "\n").strip().getBytes(StandardCharsets.US_ASCII));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Override
    public String toString() {
        return "RsaKeys[keyId=%s]".formatted(keyId);
    }
}
