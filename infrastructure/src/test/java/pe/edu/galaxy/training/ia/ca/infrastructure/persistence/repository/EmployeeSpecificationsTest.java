package pe.edu.galaxy.training.ia.ca.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmployeeSpecificationsTest {

    @Test
    void shouldRemoveAccentsAndLowercaseWhenTextIsNormalized() {
        assertThat(EmployeeSpecifications.normalize("PÉREZ Muñoz Ñandú")).isEqualTo("perez munoz nandu");
    }

    @Test
    void shouldEscapeLikeWildcardsWhenTextContainsThem() {
        assertThat(EmployeeSpecifications.escapeLike("50%_a\\b")).isEqualTo("50\\%\\_a\\\\b");
    }

    @Test
    void shouldMapEachAccentedCharacterToItsPlainLetterWhenTranslating() {
        // translate() empareja por posición: ambas cadenas deben tener la misma longitud
        assertThat(EmployeeSpecifications.ACCENTED).hasSameSizeAs(EmployeeSpecifications.PLAIN);
        for (int i = 0; i < EmployeeSpecifications.ACCENTED.length(); i++) {
            String accented = String.valueOf(EmployeeSpecifications.ACCENTED.charAt(i));
            assertThat(EmployeeSpecifications.normalize(accented))
                    .isEqualTo(String.valueOf(EmployeeSpecifications.PLAIN.charAt(i)).toLowerCase());
        }
    }
}
