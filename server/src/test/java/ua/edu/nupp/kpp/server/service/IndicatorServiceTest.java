
package ua.edu.nupp.kpp.server.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class IndicatorServiceTest {

    private final IndicatorService service =
            new IndicatorService();

    @Test
    void testCorrectCalculation() {
        assertEquals(78.0,
                service.calculate(80, 60, 100), 0.000001);
    }

    @Test
    void testZeroFactors() {
        assertEquals(0.0,
                service.calculate(0, 0, 0), 0.000001);
    }

    @Test
    void testMaximumFactors() {
        assertEquals(100.0,
                service.calculate(100, 100, 100), 0.000001);
    }

    @Test
    void testNegativeF1() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(-10, 50, 60));
    }

    @Test
    void testF2Above100() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(50, 150, 60));
    }

    @Test
    void testNegativeF3() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculate(50, 60, -10));
    }

    @Test
    void testCorrectAreaNormalization() {
        assertEquals(40.0,
                service.calculateF1(
                        500.0,
                        new BigDecimal("200.00")),
                0.000001);
    }

    @Test
    void testAreaNormalizationLimit() {
        assertEquals(100.0,
                service.calculateF1(
                        100.0,
                        new BigDecimal("150.00")),
                0.000001);
    }

    @Test
    void testZeroPlotArea() {
        assertThrows(IllegalArgumentException.class,
                () -> service.calculateF1(
                        0.0,
                        new BigDecimal("50.00")));
    }

    @Test
    void testCalculationFromData() {
        assertEquals(59.0,
                service.calculateFromData(
                        500.0,
                        new BigDecimal("200.00"),
                        80,
                        60),
                0.000001);
    }
}

