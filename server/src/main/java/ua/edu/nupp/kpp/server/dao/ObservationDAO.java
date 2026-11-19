
package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;
import ua.edu.nupp.kpp.server.model.Observation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ObservationDAO {

    // Отримання всіх спостережень
    public List<Observation> getAll() throws SQLException {

        List<Observation> observations = new ArrayList<>();

        String sql = "SELECT * FROM observation";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                Observation observation = new Observation();

                observation.setId(rs.getLong("id"));
                observation.setPlotId(rs.getLong("plot_id"));
                observation.setObservationDate(
                        rs.getDate("observation_date").toLocalDate());
                observation.setAreaAffected(
                        rs.getBigDecimal("area_affected"));
                observation.setComment(rs.getString("comment"));

                observations.add(observation);
            }
        }

        return observations;
    }

    // Додавання спостереження
    public void create(Observation observation) throws SQLException {

        String sql = "INSERT INTO observation " +
                "(plot_id, observation_date, area_affected, comment) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, observation.getPlotId());
            statement.setDate(2,
                    Date.valueOf(observation.getObservationDate()));
            statement.setBigDecimal(3, observation.getAreaAffected());
            statement.setString(4, observation.getComment());

            statement.executeUpdate();
        }
    }

    // Оновлення спостереження
    public void update(Observation observation) throws SQLException {

        String sql = "UPDATE observation SET " +
                "plot_id = ?, observation_date = ?, " +
                "area_affected = ?, comment = ? WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, observation.getPlotId());
            statement.setDate(2,
                    Date.valueOf(observation.getObservationDate()));
            statement.setBigDecimal(3, observation.getAreaAffected());
            statement.setString(4, observation.getComment());
            statement.setLong(5, observation.getId());

            statement.executeUpdate();
        }
    }

    // Видалення спостереження
    public boolean delete(long id) throws SQLException {

        String sql = "DELETE FROM observation WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}
