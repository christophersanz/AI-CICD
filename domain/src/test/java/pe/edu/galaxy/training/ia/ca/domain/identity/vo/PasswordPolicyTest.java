package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.identity.exception.WeakPasswordException;

class PasswordPolicyTest {

    @Test
    void shouldAcceptPasswordWhenLengthIsWithinLimits() {
        assertThatCode(() -> PasswordPolicy.validate("a".repeat(12))).doesNotThrowAnyException();
        assertThatCode(() -> PasswordPolicy.validate("a".repeat(64))).doesNotThrowAnyException();
    }

    @Test
    void shouldAcceptPasswordWithoutCompositionRulesWhenLengthIsValid() {
        assertThatCode(() -> PasswordPolicy.validate("only lowercase words")).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectPasswordWhenShorterThanTwelveCharacters() {
        assertThatThrownBy(() -> PasswordPolicy.validate("a".repeat(11)))
                .isInstanceOf(WeakPasswordException.class)
                .extracting("code").isEqualTo("AUTH-008");
    }

    @Test
    void shouldRejectPasswordWhenLongerThanSixtyFourCharacters() {
        assertThatThrownBy(() -> PasswordPolicy.validate("a".repeat(65))).isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void shouldRejectPasswordWhenItExceedsSeventyTwoBytesInUtf8() {
        // Given: 40 caracteres de 2 bytes = 80 bytes, dentro del límite de caracteres
        String password = "ñ".repeat(40);

        // When / Then
        assertThatThrownBy(() -> PasswordPolicy.validate(password))
                .isInstanceOf(WeakPasswordException.class)
                .hasMessageContaining("72 bytes");
    }

    @Test
    void shouldCountCodePointsWhenPasswordHasCharactersOutsideTheBmp() {
        // 12 emojis = 12 caracteres (24 unidades UTF-16) y 48 bytes
        assertThatCode(() -> PasswordPolicy.validate("😀".repeat(12))).doesNotThrowAnyException();
    }

    @Test
    void shouldRejectPasswordWhenNull() {
        assertThatThrownBy(() -> PasswordPolicy.validate(null)).isInstanceOf(WeakPasswordException.class);
    }
}
