package pe.edu.galaxy.training.ia.ca.domain.identity.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

class IdentityValueObjectsTest {

    @Test
    void shouldNormalizeUsernameToLowercaseWhenItHasUppercaseAndSpaces() {
        assertThat(new Username("  Ana.Perez_01 ").value()).isEqualTo("ana.perez_01");
    }

    @Test
    void shouldConsiderUsernamesEqualWhenTheyDifferOnlyInCase() {
        assertThat(new Username("ADMIN")).isEqualTo(new Username("admin"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "ana perez", "ana@perez", "ñandú", ""})
    void shouldRejectUsernameWhenFormatIsInvalid(String value) {
        assertThatThrownBy(() -> new Username(value))
                .isInstanceOf(InvalidValueException.class)
                .extracting("code").isEqualTo("VAL-001");
    }

    @Test
    void shouldRejectUsernameWhenItExceedsFiftyCharacters() {
        assertThatThrownBy(() -> new Username("a".repeat(51))).isInstanceOf(InvalidValueException.class);
        assertThat(new Username("a".repeat(50)).value()).hasSize(50);
    }

    @ParameterizedTest
    @ValueSource(strings = {"contract:create", "contract:read:own", "user:manage", "system:monitor"})
    void shouldAcceptPermissionWhenItFollowsResourceActionScopeFormat(String value) {
        assertThat(new Permission(value).value()).isEqualTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"contract", "Contract:create", "contract:", "contract:read:own:extra", "contract create"})
    void shouldRejectPermissionWhenFormatIsInvalid(String value) {
        assertThatThrownBy(() -> new Permission(value)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldNormalizeRoleNameToUppercaseWhenCreated() {
        assertThat(new RoleName("hr")).isEqualTo(RoleName.HR);
    }

    @Test
    void shouldRejectRoleNameWhenItContainsInvalidCharacters() {
        assertThatThrownBy(() -> new RoleName("H R")).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldHideHashWhenPasswordHashIsPrinted() {
        assertThat(new PasswordHash("{bcrypt}$2a$10$abc").toString()).doesNotContain("bcrypt");
    }

    @Test
    void shouldRejectPasswordHashWhenBlank() {
        assertThatThrownBy(() -> new PasswordHash(" ")).isInstanceOf(InvalidValueException.class);
    }
}
