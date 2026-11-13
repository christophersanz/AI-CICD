package pe.edu.galaxy.training.ia.ca.domain.contract.exception;

import pe.edu.galaxy.training.ia.ca.domain.common.exception.DomainException;

/**
 * Falta el motivo o la fecha del término, o la fecha queda fuera de la vigencia del contrato (BR-CTR-009,
 * {@code CTR-007}, HTTP 422).
 */
public class InvalidTerminationException extends DomainException {

    /**
     * Crea el error con el código {@code CTR-007}.
     *
     * @param message qué dato del término no es válido
     */
    public InvalidTerminationException(String message) {
        super("CTR-007", message);
    }
}
