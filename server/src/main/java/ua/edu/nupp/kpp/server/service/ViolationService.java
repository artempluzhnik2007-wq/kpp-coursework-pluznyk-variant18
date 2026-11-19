
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.LandPlotDAO;
import ua.edu.nupp.kpp.server.dao.ViolationDAO;
import ua.edu.nupp.kpp.server.model.LandPlot;
import ua.edu.nupp.kpp.server.model.Violation;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class ViolationService {

    private static final Logger logger =
            Logger.getLogger(ViolationService.class.getName());

    private final ViolationDAO violationDAO = new ViolationDAO();
    private final LandPlotDAO landPlotDAO = new LandPlotDAO();

    // Перевірка існування земельної ділянки
    private boolean plotExists(long plotId) throws SQLException {

        for (LandPlot plot : landPlotDAO.getAll()) {
            if (plot.getId() == plotId) {
                return true;
            }
        }

        return false;
    }

    // Перевірка даних порушення
    private void validate(Violation violation)
            throws SQLException {

        if (violation == null) {
            throw new IllegalArgumentException(
                    "Порушення не може бути null"
            );
        }

        if (violation.getPlotId() <= 0 ||
                !plotExists(violation.getPlotId())) {

            throw new IllegalArgumentException(
                    "Земельна ділянка не існує"
            );
        }

        if (violation.getDescription() == null ||
                violation.getDescription().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Опис порушення не може бути порожнім"
            );
        }

        if (violation.getSeverity() < 0 ||
                violation.getSeverity() > 100) {

            throw new IllegalArgumentException(
                    "Рівень серйозності повинен бути від 0 до 100"
            );
        }

        if (violation.getDetectedDate() == null) {
            throw new IllegalArgumentException(
                    "Дата виявлення обов'язкова"
            );
        }
    }

    // CREATE
    public void create(Violation violation)
            throws SQLException {

        try {
            validate(violation);

            if (!"ВИЯВЛЕНО".equals(
                    violation.getStatus())) {

                throw new IllegalArgumentException(
                        "Нове порушення повинно мати статус ВИЯВЛЕНО"
                );
            }

            violationDAO.create(violation);
            logger.info("Порушення створено");

        } catch (SQLException | IllegalArgumentException e) {

            logger.severe(
                    "Помилка створення: " + e.getMessage()
            );

            throw e;
        }
    }

    // READ
    public List<Violation> getAll()
            throws SQLException {

        try {
            List<Violation> violations =
                    violationDAO.getAll();

            logger.info("Отримано список порушень");

            return violations;

        } catch (SQLException e) {

            logger.severe(
                    "Помилка читання: " + e.getMessage()
            );

            throw e;
        }
    }

    // UPDATE
    public void update(Violation violation)
            throws SQLException {

        try {
            validate(violation);

            Violation oldViolation = null;

            for (Violation v : violationDAO.getAll()) {

                if (v.getId() == violation.getId()) {
                    oldViolation = v;
                    break;
                }
            }

            if (oldViolation == null) {
                throw new IllegalArgumentException(
                        "Порушення не знайдено"
                );
            }

            String oldStatus = oldViolation.getStatus();
            String newStatus = violation.getStatus();

            // Перевірка життєвого циклу
            if (!ViolationLifecycle.canUpdateStatus(
                    oldStatus, newStatus)) {

                throw new IllegalArgumentException(
                        "Недопустимий перехід статусу: "
                                + oldStatus + " -> " + newStatus
                );
            }

            violationDAO.update(violation);

            logger.info(
                    "Порушення оновлено. Статус: "
                            + oldStatus + " -> " + newStatus
            );

        } catch (SQLException | IllegalArgumentException e) {

            logger.severe(
                    "Помилка оновлення: " + e.getMessage()
            );

            throw e;
        }
    }

    // DELETE
    public boolean delete(long id)
            throws SQLException {

        try {
            if (id <= 0) {
                throw new IllegalArgumentException(
                        "Некоректний ID порушення"
                );
            }

            boolean deleted = violationDAO.delete(id);

            if (deleted) {
                logger.info("Порушення видалено");
            } else {
                logger.warning("Порушення не знайдено");
            }

            return deleted;

        } catch (SQLException | IllegalArgumentException e) {

            logger.severe(
                    "Помилка видалення: " + e.getMessage()
            );

            throw e;
        }
    }
}
