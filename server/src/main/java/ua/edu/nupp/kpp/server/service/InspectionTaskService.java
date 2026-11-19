
package ua.edu.nupp.kpp.server.service;

import ua.edu.nupp.kpp.server.dao.InspectionTaskDAO;
import ua.edu.nupp.kpp.server.dao.ViolationDAO;
import ua.edu.nupp.kpp.server.model.InspectionTask;
import ua.edu.nupp.kpp.server.model.Violation;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class InspectionTaskService {

    private static final Logger logger =
            Logger.getLogger(InspectionTaskService.class.getName());

    private final InspectionTaskDAO taskDAO =
            new InspectionTaskDAO();

    private final ViolationDAO violationDAO =
            new ViolationDAO();

    // Перевірка існування порушення
    private boolean violationExists(long violationId)
            throws SQLException {

        for (Violation violation : violationDAO.getAll()) {
            if (violation.getId() == violationId) {
                return true;
            }
        }

        return false;
    }

    // Перевірка даних завдання
    private void validate(InspectionTask task)
            throws SQLException {

        if (task == null) {
            throw new IllegalArgumentException(
                    "Завдання не може бути null");
        }

        if (task.getViolationId() <= 0 ||
                !violationExists(task.getViolationId())) {
            throw new IllegalArgumentException(
                    "Порушення не існує");
        }

        if (task.getPlannedDate() == null) {
            throw new IllegalArgumentException(
                    "Запланована дата обов'язкова");
        }

        if (task.getCompletionDate() != null &&
                task.getCompletionDate()
                        .isBefore(task.getPlannedDate())) {
            throw new IllegalArgumentException(
                    "Дата завершення не може бути раніше запланованої");
        }

        if (task.getTaskStatus() == null ||
                task.getTaskStatus().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Статус завдання обов'язковий");
        }
    }

    // CREATE
    public void create(InspectionTask task)
            throws SQLException {

        try {
            validate(task);
            taskDAO.create(task);

            logger.info("Завдання створено");

        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Помилка створення: " + e.getMessage());
            throw e;
        }
    }

    // READ
    public List<InspectionTask> getAll()
            throws SQLException {

        try {
            List<InspectionTask> tasks = taskDAO.getAll();

            logger.info("Отримано список завдань");
            return tasks;

        } catch (SQLException e) {
            logger.severe("Помилка читання: " + e.getMessage());
            throw e;
        }
    }

    // UPDATE
    public void update(InspectionTask task)
            throws SQLException {

        try {
            validate(task);

            if (task.getId() <= 0) {
                throw new IllegalArgumentException(
                        "Некоректний ID завдання");
            }

            taskDAO.update(task);

            logger.info("Завдання оновлено");

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
                        "Некоректний ID завдання");
            }

            boolean deleted = taskDAO.delete(id);

            if (deleted) {
                logger.info("Завдання видалено");
            } else {
                logger.warning("Завдання не знайдено");
            }

            return deleted;

        } catch (SQLException | IllegalArgumentException e) {
            logger.severe("Помилка видалення: " + e.getMessage());
            throw e;
        }
    }
}
