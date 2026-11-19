
package ua.edu.nupp.kpp.server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class IndicatorServiceTest {

    private IndicatorService service;

    @BeforeEach
    void setUp() {
        service = new IndicatorService();
    }

    // Тест 1. Звичайний розрахунок показника
    @Test
    void calculateNormalValues() {

        double result = service.calculate(20, 60, 80);

        assertEquals(49.0, result, 0.0001);
    }

    // Тест 2. Усі фактори дорівнюють нулю
    @Test
    void calculateAllZeros() {

        double result = service.calculate(0, 0, 0);

        assertEquals(0.0, result, 0.0001);
    }

    // Тест 3. Усі фактори мають максимальне значення
    @Test
    void calculateAllMaximum() {

        double result = service.calculate(100, 100, 100);

        assertEquals(100.0, result, 0.0001);
    }

    // Тест 4. Перевірка ваги першого фактора
    @Test
    void calculateFirstFactorWeight() {

        double result = service.calculate(100, 0, 0);

        assertEquals(40.0, result, 0.0001);
    }

    // Тест 5. Перевірка ваги другого фактора
    @Test
    void calculateSecondFactorWeight() {

        double result = service.calculate(0, 100, 0);

        assertEquals(35.0, result, 0.0001);
    }

    // Тест 6. Перевірка ваги третього фактора
    @Test
    void calculateThirdFactorWeight() {

        double result = service.calculate(0, 0, 100);

        assertEquals(25.0, result, 0.0001);
    }

    // Тест 7. Від'ємний фактор заборонений
    @Test
    void calculateNegativeFactor() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(-10, 50, 60)
        );
    }

    // Тест 8. Фактор понад 100 заборонений
    @Test
    void calculateFactorAbove100() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(50, 120, 60)
        );
    }

    // Тест 9. Значення NaN заборонене
    @Test
    void calculateNaN() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        Double.NaN, 50, 60
                )
        );
    }

    // Тест 10. Нескінченність заборонена
    @Test
    void calculateInfinity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(
                        50, Double.POSITIVE_INFINITY, 60
                )
        );
    }

    // Тест 11. Нормування ураженої площі
    @Test
    void calculateF1Normal() {

        double result = service.calculateF1(
                1000,
                new BigDecimal("250")
        );

        assertEquals(25.0, result, 0.0001);
    }

    // Тест 12. Обмеження F1 до 100
    @Test
    void calculateF1MaximumLimit() {

        double result = service.calculateF1(
                1000,
                new BigDecimal("1500")
        );

        assertEquals(100.0, result, 0.0001);
    }

    // Тест 13. Нульова площа ділянки заборонена
    @Test
    void calculateF1ZeroPlotArea() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateF1(
                        0,
                        new BigDecimal("100")
                )
        );
    }

    // Тест 14. Від'ємна уражена площа заборонена
    @Test
    void calculateF1NegativeAffectedArea() {

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateF1(
                        1000,
                        new BigDecimal("-50")
                )
        );
    }

    // Тест 15. Розрахунок за вихідними даними
    @Test
    void calculateFromDataNormal() {

        double result = service.calculateFromData(
                1000,
                new BigDecimal("250"),
                60,
                80
        );

        assertEquals(51.0, result, 0.0001);
    }
}
