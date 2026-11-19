
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.LandPlotDAO;
import ua.edu.nupp.kpp.server.dao.ObservationDAO;
import ua.edu.nupp.kpp.server.dao.ViolationDAO;

import ua.edu.nupp.kpp.server.model.LandPlot;
import ua.edu.nupp.kpp.server.model.Observation;
import ua.edu.nupp.kpp.server.model.Violation;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;

public class IndicatorService {

    private static final Logger logger =
            Logger.getLogger(IndicatorService.class.getName());

    // Перевірка значення фактора
    private void validateFactor(String name, double value) {

        if (!Double.isFinite(value) ||
                value < 0 || value > 100) {

            throw new IllegalArgumentException(
                    name + " повинен бути від 0 до 100");
        }
    }

    // Розрахунок за готовими факторами
    public double calculate(double f1, double f2, double f3) {

        try {
            validateFactor("F1", f1);
            validateFactor("F2", f2);
            validateFactor("F3", f3);

            double result =
                    0.40 * f1 +
                            0.35 * f2 +
                            0.25 * f3;

            logger.info(
                    "Інтегральний показник: " + result);

            return result;

        } catch (IllegalArgumentException e) {

            logger.severe(
                    "Помилка розрахунку: " + e.getMessage());

            throw e;
        }
    }

    // Нормування площі порушення (F1)
    public double calculateF1(
            double plotArea,
            BigDecimal areaAffected) {

        if (!Double.isFinite(plotArea) || plotArea <= 0) {
            throw new IllegalArgumentException(
                    "Площа ділянки повинна бути більшою за 0");
        }

        if (areaAffected == null ||
                areaAffected.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Уражена площа не може бути від'ємною");
        }

        double affected = areaAffected.doubleValue();

        if (!Double.isFinite(affected)) {
            throw new IllegalArgumentException(
                    "Некоректна уражена площа");
        }

        double f1 = (affected / plotArea) * 100.0;

        return Math.min(100.0, f1);
    }

    // Розрахунок із вихідних даних
    public double calculateFromData(
            double plotArea,
            BigDecimal areaAffected,
            int severity,
            int ecologicalScore) {

        double f1 = calculateF1(plotArea, areaAffected);

        double f2 = severity;
        double f3 = ecologicalScore;

        double result = calculate(f1, f2, f3);

        logger.info(
                "F1 = " + f1 +
                        ", F2 = " + f2 +
                        ", F3 = " + f3 +
                        ", I = " + result);

        return result;
    }

    // Розрахунок із бази даних MySQL
    public double calculateFromDatabase(
            long plotId,
            long violationId,
            long observationId) throws SQLException {

        try {
            LandPlotDAO landPlotDAO = new LandPlotDAO();
            ViolationDAO violationDAO = new ViolationDAO();
            ObservationDAO observationDAO = new ObservationDAO();

            LandPlot selectedPlot = null;
            Violation selectedViolation = null;
            Observation selectedObservation = null;

            // Пошук земельної ділянки
            for (LandPlot plot : landPlotDAO.getAll()) {
                if (plot.getId() == plotId) {
                    selectedPlot = plot;
                    break;
                }
            }

            // Пошук порушення
            for (Violation violation : violationDAO.getAll()) {
                if (violation.getId() == violationId) {
                    selectedViolation = violation;
                    break;
                }
            }

            // Пошук спостереження
            for (Observation observation : observationDAO.getAll()) {
                if (observation.getId() == observationId) {
                    selectedObservation = observation;
                    break;
                }
            }

            // Перевірка наявності записів
            if (selectedPlot == null) {
                throw new IllegalArgumentException(
                        "Земельну ділянку не знайдено");
            }

            if (selectedViolation == null) {
                throw new IllegalArgumentException(
                        "Порушення не знайдено");
            }

            if (selectedObservation == null) {
                throw new IllegalArgumentException(
                        "Спостереження не знайдено");
            }

            // Перевірка зв'язків між таблицями
            if (selectedViolation.getPlotId() != plotId) {
                throw new IllegalArgumentException(
                        "Порушення належить іншій ділянці");
            }

            if (selectedObservation.getPlotId() != plotId) {
                throw new IllegalArgumentException(
                        "Спостереження належить іншій ділянці");
            }

            // Розрахунок інтегрального показника
            double result = calculateFromData(
                    selectedPlot.getArea(),
                    selectedObservation.getAreaAffected(),
                    selectedViolation.getSeverity(),
                    selectedPlot.getEcologicalScore()
            );

            logger.info(
                    "Показник розраховано з MySQL: " + result);

            return result;

        } catch (SQLException | IllegalArgumentException e) {

            logger.severe(
                    "Помилка розрахунку з БД: " + e.getMessage());

            throw e;
        }
    }
}
