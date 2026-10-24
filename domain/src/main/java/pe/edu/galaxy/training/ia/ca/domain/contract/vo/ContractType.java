package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

/** Tipo de contrato. Solo {@code FIXED_TERM} tiene fecha de fin y puede renovarse (BR-CTR-003, BR-CTR-007). */
public enum ContractType {

    INDEFINITE(false),
    FIXED_TERM(true);

    private final boolean fixedTerm;

    ContractType(boolean fixedTerm) {
        this.fixedTerm = fixedTerm;
    }

    public boolean requiresEndDate() {
        return fixedTerm;
    }

    public boolean isRenewable() {
        return fixedTerm;
    }

    /** {@code expire()} solo aplica a contratos a plazo fijo (business-rules.md §5). */
    public boolean isExpirable() {
        return fixedTerm;
    }
}
