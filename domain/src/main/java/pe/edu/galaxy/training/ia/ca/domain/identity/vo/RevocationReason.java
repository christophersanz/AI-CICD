package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

/** Motivo por el que se revoca un refresh token. */
public enum RevocationReason {
    LOGOUT,
    REUSE_DETECTED,
    USER_DISABLED,
    PASSWORD_CHANGED
}
