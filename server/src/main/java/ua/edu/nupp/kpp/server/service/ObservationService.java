
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.LandPlotDAO;
import ua.edu.nupp.kpp.server.dao.ObservationDAO;
import ua.edu.nupp.kpp.server.model.LandPlot;
import ua.edu.nupp.kpp.server.model.Observation;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ObservationService {

    private static final Logger logger =
            Logger.getLogger(ObservationService.class.getName());

    private final ObservationDAO observationDAO =
            new ObservationDAO();

    private final LandPlotDAO landPlotDAO =
            new LandPlotDAO();

    // Перевірка існування земельної ділянки
    private boolean plotExists(long plotId) throws SQLException {

        for (LandPlot plot : landPlotDAO.getAll()) {
            if (plot.getId() == plotId) {
                return true;
            }
        }

        return false;
    }

    // Перевірка даних спостереження
    private void validate(Observation observation)
            throws SQLException {

        if (observation == null) {
            throw new IllegalArgumentException(
                    "Спостереження не може бути null");
        }

        if (observation.getPlotId() <= 0 ||
                !plotExists(observation.getPlotId())) {
            throw new IllegalArgumentException(
                    "Земельна ділянка не існує");
        }

        if (observation.getObservationDate() == null) {
            throw new IllegalArgumentException(
                    "Дата спостереження обов'язкова");
        }

        if (observation.getAreaAffected() == null ||
                observation.getAreaAffected()
                        .compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Уражена площа не може бути від'ємною");
        }

        if (observation.getAreaAffected()
                .compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException(
                    "Уражена площа перевищує допустиме значення");
        }
    }

    // CREATE
    public void create(Observation observation)
            throws SQLException {

        try {
            validate(observation);
            observationDAO.create(observation);

            logger.info("Спостереження створено");

        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Помилка створення: " + e.getMessage());
            throw e;
        }
    }

    // READ
    public List<Observation> getAll() throws SQLException {

        try {
            List<Observation> observations =
                    observationDAO.getAll();

            logger.info("Отримано список спостережень");

            return observations;

        } catch (SQLException e) {
            logger.severe("Помилка читання: " + e.getMessage());
            throw e;
        }
    }

    // UPDATE
    public void update(Observation observation)
            throws SQLException {

        try {
            validate(observation);

            if (observation.getId() <= 0) {
                throw new IllegalArgumentException(
                        "Некоректний ID спостереження");
            }

            observationDAO.update(observation);

            logger.info("Спостереження оновлено");

        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Помилка оновлення: " + e.getMessage());
            throw e;
        }
    }

    // DELETE
    public boolean delete(long id) throws SQLException {

        try {
            if (id <= 0) {
                throw new IllegalArgumentException(
                        "Некоректний ID спостереження");
            }

            boolean deleted = observationDAO.delete(id);

            if (deleted) {
                logger.info("Спостереження видалено");
            } else {
                logger.warning("Спостереження не знайдено");
            }

            return deleted;

        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Помилка видалення: " + e.getMessage());
            throw e;
        }
    }
}
