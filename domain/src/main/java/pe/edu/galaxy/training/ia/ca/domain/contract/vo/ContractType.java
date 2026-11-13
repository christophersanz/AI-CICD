package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

/** Tipo de contrato. Solo {@code FIXED_TERM} tiene fecha de fin y puede renovarse (BR-CTR-003, BR-CTR-007). */
public enum ContractType {

    /** Plazo indeterminado: sin fecha de fin. */
    INDEFINITE(false),
    /** Plazo fijo o sujeto a modalidad: con fecha de fin, renovable y sujeto a vencimiento. */
    FIXED_TERM(true);

    private final boolean fixedTerm;

    ContractType(boolean fixedTerm) {
        this.fixedTerm = fixedTerm;
    }

    /** {@return {@code true} si la vigencia debe tener fecha de fin (BR-CTR-003)} */
    public boolean requiresEndDate() {
        return fixedTerm;
    }

    /** {@return {@code true} si el contrato puede renovarse (BR-CTR-007)} */
    public boolean isRenewable() {
        return fixedTerm;
    }

    /** {@return {@code true} si el contrato puede vencer; {@code expire()} solo aplica a plazo fijo (§5)} */
    public boolean isExpirable() {
        return fixedTerm;
    }
}
