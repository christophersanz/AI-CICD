package pe.edu.galaxy.training.ia.ca.domain.contract.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;
import pe.edu.galaxy.training.ia.ca.domain.shared.vo.Money;

class SalaryTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void shouldRejectSalaryWhenCurrencyIsNotPen() {
        assertThatThrownBy(() -> new Salary(Money.of(new BigDecimal("3000"), USD)))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldRejectSalaryWhenAmountIsZeroOrMissing() {
        assertThatThrownBy(() -> Salary.ofPen(BigDecimal.ZERO)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> new Salary(null)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldRejectMinimumWageWhenCurrencyIsNotPenOrAmountIsZero() {
        assertThatThrownBy(() -> new MinimumWage(Money.of(BigDecimal.TEN, USD)))
                .isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> MinimumWage.ofPen(BigDecimal.ZERO)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldBeBelowMinimumWageOnlyWhenAmountIsLower() {
        MinimumWage minimumWage = MinimumWage.ofPen(new BigDecimal("1130"));

        assertThat(Salary.ofPen(new BigDecimal("1129.99")).isBelow(minimumWage)).isTrue();
        assertThat(Salary.ofPen(new BigDecimal("1130.00")).isBelow(minimumWage)).isFalse();
    }
}
