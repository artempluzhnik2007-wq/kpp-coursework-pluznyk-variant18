

package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;
import ua.edu.nupp.kpp.server.model.Violation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ViolationDAO {

    // Отримання всіх порушень
    public List<Violation> getAll() throws SQLException {

        List<Violation> violations = new ArrayList<>();

        String sql = "SELECT * FROM violation";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                Violation violation = new Violation();

                violation.setId(rs.getLong("id"));
                violation.setPlotId(rs.getLong("plot_id"));
                violation.setDescription(rs.getString("description"));
                violation.setSeverity(rs.getInt("severity"));
                violation.setDetectedDate(
                        rs.getDate("detected_date").toLocalDate());
                violation.setStatus(rs.getString("status"));

                violations.add(violation);
            }
        }

        return violations;
    }

    // Додавання порушення
    public void create(Violation violation) throws SQLException {

        String sql = "INSERT INTO violation " +
                "(plot_id, description, severity, detected_date, status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, violation.getPlotId());
            statement.setString(2, violation.getDescription());
            statement.setInt(3, violation.getSeverity());
            statement.setDate(4,
                    java.sql.Date.valueOf(violation.getDetectedDate()));
            statement.setString(5, violation.getStatus());

            statement.executeUpdate();
        }
    }

    // Оновлення порушення
    public void update(Violation violation) throws SQLException {

        String sql = "UPDATE violation SET " +
                "plot_id = ?, description = ?, severity = ?, " +
                "detected_date = ?, status = ? WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, violation.getPlotId());
            statement.setString(2, violation.getDescription());
            statement.setInt(3, violation.getSeverity());
            statement.setDate(4,
                    java.sql.Date.valueOf(violation.getDetectedDate()));
            statement.setString(5, violation.getStatus());
            statement.setLong(6, violation.getId());

            statement.executeUpdate();
        }
    }

    // Видалення порушення
    public boolean delete(long id) throws SQLException {

        String sql = "DELETE FROM violation WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}

