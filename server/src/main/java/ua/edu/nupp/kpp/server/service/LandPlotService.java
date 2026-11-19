
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.LandPlotDAO;
import ua.edu.nupp.kpp.server.model.LandPlot;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class LandPlotService {

    private static final Logger logger =
            Logger.getLogger(LandPlotService.class.getName());

    private final LandPlotDAO dao = new LandPlotDAO();

    // Перевірка даних земельної ділянки
    private void validate(LandPlot plot) {

        if (plot == null) {
            throw new IllegalArgumentException(
                    "Земельна ділянка не може бути null");
        }

        if (plot.getAddress() == null ||
                plot.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Адреса не може бути порожньою");
        }

        if (!Double.isFinite(plot.getArea()) ||
                plot.getArea() <= 0) {
            throw new IllegalArgumentException(
                    "Площа повинна бути більшою за 0");
        }

        if (plot.getEcologicalScore() < 0 ||
                plot.getEcologicalScore() > 100) {
            throw new IllegalArgumentException(
                    "Екологічна оцінка повинна бути від 0 до 100");
        }
    }

    // CREATE
    public void create(LandPlot plot) throws SQLException {

        try {
            validate(plot);
            dao.create(plot);

            logger.info("Земельну ділянку створено");
        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Помилка створення: " + e.getMessage());
            throw e;
        }
    }

    // READ
    public List<LandPlot> getAll() throws SQLException {

        try {
            List<LandPlot> plots = dao.getAll();

            logger.info("Отримано список земельних ділянок");
            return plots;
        } catch (SQLException e) {
            logger.severe("Помилка читання: " + e.getMessage());
            throw e;
        }
    }

    // UPDATE
    public void update(LandPlot plot) throws SQLException {

        try {
            validate(plot);

            if (plot.getId() <= 0) {
                throw new IllegalArgumentException(
                        "Некоректний ID ділянки");
            }

            dao.update(plot);

            logger.info("Земельну ділянку оновлено");
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
                        "Некоректний ID ділянки");
            }

            boolean deleted = dao.delete(id);

            if (deleted) {
                logger.info("Земельну ділянку видалено");
            } else {
                logger.warning("Ділянку не знайдено");
            }

            return deleted;
        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Помилка видалення: " + e.getMessage());
            throw e;
        }
    }
}

