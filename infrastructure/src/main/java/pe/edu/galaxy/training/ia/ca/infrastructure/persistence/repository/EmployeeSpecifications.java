package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository;

import jakarta.persistence.criteria.Expression;
import java.text.Normalizer;
import java.util.Locale;
import org.springframework.data.jpa.domain.Specification;
import pe.edu.galaxy.training.ia.ca.application.dto.EmployeeSearchQuery;
import pe.edu.galaxy.training.ia.ca.infrastructure.persistence.entity.EmployeeJpaEntity;

/**
 * Filtros del listado de empleados (BR-EMP-004).
 *
 * <p>El nombre se compara sin mayúsculas ni tildes con {@code translate()} de PostgreSQL, que no requiere
 * extensiones (a diferencia de {@code unaccent}); el texto buscado se normaliza igual en Java. No usa
 * índices: suficiente para el volumen de empleados de una organización.
 */
public final class EmployeeSpecifications {

    static final String ACCENTED = "ÁÀÂÄÉÈÊËÍÌÎÏÓÒÔÖÚÙÛÜÑÇáàâäéèêëíìîïóòôöúùûüñç";
    static final String PLAIN = "AAAAEEEEIIIIOOOOUUUUNCaaaaeeeeiiiioooouuuunc";
    private static final char ESCAPE = '\\';

    private EmployeeSpecifications() {
    }

    public static Specification<EmployeeJpaEntity> matching(EmployeeSearchQuery query) {
        Specification<EmployeeJpaEntity> specification = Specification.unrestricted();
        if (query.hasDocumentFilter()) {
            specification = specification.and(document(query));
        }
        if (query.hasNameFilter()) {
            specification = specification.and(nameContains(query.name()));
        }
        return specification;
    }

    private static Specification<EmployeeJpaEntity> document(EmployeeSearchQuery query) {
        return (root, criteria, builder) -> builder.and(
                builder.equal(root.get("documentType"), query.documentType()),
                builder.equal(root.get("documentNumber"), query.documentNumber()));
    }

    private static Specification<EmployeeJpaEntity> nameContains(String text) {
        String pattern = "%" + escapeLike(normalize(text)) + "%";
        return (root, criteria, builder) -> {
            Expression<String> fullName = builder.concat(
                    builder.concat(root.get("firstNames"), " "), root.get("lastNames"));
            Expression<String> plain = builder.lower(builder.function("translate", String.class,
                    fullName, builder.literal(ACCENTED), builder.literal(PLAIN)));
            return builder.like(plain, pattern, ESCAPE);
        };
    }

    /** Minúsculas y sin marcas diacríticas: "PÉREZ" → "perez". */
    static String normalize(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    /** Los comodines escritos por el usuario se buscan literalmente. */
    static String escapeLike(String text) {
        return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
