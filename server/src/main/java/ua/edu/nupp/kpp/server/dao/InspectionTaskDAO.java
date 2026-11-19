
package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;
import ua.edu.nupp.kpp.server.model.InspectionTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class InspectionTaskDAO {

    // READ - отримання всіх завдань
    public List<InspectionTask> getAll() throws SQLException {

        List<InspectionTask> tasks = new ArrayList<>();

        String sql = "SELECT * FROM inspection_task";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                InspectionTask task = new InspectionTask();

                task.setId(rs.getLong("id"));
                task.setViolationId(rs.getLong("violation_id"));
                task.setPlannedDate(
                        rs.getDate("planned_date").toLocalDate());

                Date completionDate =
                        rs.getDate("completion_date");

                if (completionDate != null) {
                    task.setCompletionDate(
                            completionDate.toLocalDate());
                }

                task.setTaskStatus(rs.getString("task_status"));

                tasks.add(task);
            }
        }

        return tasks;
    }

    // CREATE - додавання завдання
    public void create(InspectionTask task) throws SQLException {

        String sql = "INSERT INTO inspection_task " +
                "(violation_id, planned_date, completion_date, task_status) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, task.getViolationId());
            statement.setDate(2,
                    Date.valueOf(task.getPlannedDate()));

            if (task.getCompletionDate() != null) {
                statement.setDate(3,
                        Date.valueOf(task.getCompletionDate()));
            } else {
                statement.setNull(3, java.sql.Types.DATE);
            }

            statement.setString(4, task.getTaskStatus());

            statement.executeUpdate();
        }
    }

    // UPDATE - оновлення завдання
    public void update(InspectionTask task) throws SQLException {

        String sql = "UPDATE inspection_task SET " +
                "violation_id = ?, planned_date = ?, " +
                "completion_date = ?, task_status = ? WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, task.getViolationId());
            statement.setDate(2,
                    Date.valueOf(task.getPlannedDate()));

            if (task.getCompletionDate() != null) {
                statement.setDate(3,
                        Date.valueOf(task.getCompletionDate()));
            } else {
                statement.setNull(3, java.sql.Types.DATE);
            }

            statement.setString(4, task.getTaskStatus());
            statement.setLong(5, task.getId());

            statement.executeUpdate();
        }
    }

    // DELETE - видалення завдання
    public boolean delete(long id) throws SQLException {

        String sql = "DELETE FROM inspection_task WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}
