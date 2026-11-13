package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

/** Motivo por el que se revoca un refresh token. */
public enum RevocationReason {
    /** El usuario cerró la sesión. */
    LOGOUT,
    /** Se presentó un token ya usado: posible robo (BR-USR-006). */
    REUSE_DETECTED,
    /** El usuario está deshabilitado (BR-USR-004). */
    USER_DISABLED,
    /** Cambió la contraseña (BR-USR-005, pendiente). */
    PASSWORD_CHANGED
}
