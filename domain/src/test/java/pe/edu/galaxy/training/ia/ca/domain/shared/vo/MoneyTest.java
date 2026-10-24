package pe.edu.galaxy.training.ia.ca.domain.shared.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import pe.edu.galaxy.training.ia.ca.domain.common.exception.InvalidValueException;

class MoneyTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void shouldNormalizeScaleWhenAmountHasFewerDecimals() {
        assertThat(Money.pen(new BigDecimal("1130")).amount()).isEqualTo(new BigDecimal("1130.00"));
    }

    @Test
    void shouldRejectMoneyWhenAmountIsNegative() {
        assertThatThrownBy(() -> Money.pen(new BigDecimal("-1")))
                .isInstanceOf(InvalidValueException.class)
                .extracting("code").isEqualTo("VAL-001");
    }

    @Test
    void shouldRejectMoneyWhenAmountHasMoreThanTwoDecimals() {
        assertThatThrownBy(() -> Money.pen(new BigDecimal("10.001"))).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldRejectMoneyWhenAmountOrCurrencyIsMissing() {
        assertThatThrownBy(() -> Money.of(null, USD)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> Money.of(BigDecimal.ONE, null)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldAddAndSubtractWhenCurrenciesMatch() {
        // Given
        Money ten = Money.pen(new BigDecimal("10.50"));
        Money three = Money.pen(new BigDecimal("3.25"));

        // When / Then
        assertThat(ten.add(three)).isEqualTo(Money.pen(new BigDecimal("13.75")));
        assertThat(ten.subtract(three)).isEqualTo(Money.pen(new BigDecimal("7.25")));
    }

    @Test
    void shouldRejectSubtractionWhenResultIsNegative() {
        assertThatThrownBy(() -> Money.pen(BigDecimal.ONE).subtract(Money.pen(BigDecimal.TEN)))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldCompareAmountsWhenCurrenciesMatch() {
        Money one = Money.pen(BigDecimal.ONE);
        Money ten = Money.pen(BigDecimal.TEN);

        assertThat(one.isLessThan(ten)).isTrue();
        assertThat(ten.isGreaterThan(one)).isTrue();
        assertThat(one.isGreaterThan(one)).isFalse();
        assertThat(Money.pen(BigDecimal.ZERO).isZero()).isTrue();
    }

    @Test
    void shouldRejectOperationWhenCurrenciesDiffer() {
        Money pen = Money.pen(BigDecimal.TEN);
        Money usd = Money.of(BigDecimal.TEN, USD);

        assertThatThrownBy(() -> pen.isLessThan(usd)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> pen.add(usd)).isInstanceOf(InvalidValueException.class);
        assertThatThrownBy(() -> pen.add(null)).isInstanceOf(InvalidValueException.class);
    }

    @Test
    void shouldBeEqualWhenAmountsDifferOnlyInScale() {
        assertThat(Money.pen(new BigDecimal("5"))).isEqualTo(Money.pen(new BigDecimal("5.0")));
    }
}
