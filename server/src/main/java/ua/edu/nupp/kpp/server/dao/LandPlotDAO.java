
package ua.edu.nupp.kpp.server.dao;

import ua.edu.nupp.kpp.server.DBUtil;
import ua.edu.nupp.kpp.server.model.LandPlot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LandPlotDAO {

    // Отримання всіх земельних ділянок
    public List<LandPlot> getAll() throws SQLException {

        List<LandPlot> plots = new ArrayList<>();

        String sql = "SELECT * FROM land_plot";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                LandPlot plot = new LandPlot();

                plot.setId(resultSet.getLong("id"));
                plot.setAddress(resultSet.getString("address"));
                plot.setArea(resultSet.getDouble("area"));
                plot.setEcologicalScore(
                        resultSet.getInt("ecological_score"));

                plots.add(plot);
            }
        }

        return plots;
    }

    // Додавання нової земельної ділянки
    public void create(LandPlot plot) throws SQLException {

        String sql = "INSERT INTO land_plot " +
                "(address, area, ecological_score) " +
                "VALUES (?, ?, ?)";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, plot.getAddress());
            statement.setDouble(2, plot.getArea());
            statement.setInt(3, plot.getEcologicalScore());

            statement.executeUpdate();
        }
    }

    // Оновлення земельної ділянки
    public void update(LandPlot plot) throws SQLException {

        String sql = "UPDATE land_plot SET " +
                "address = ?, area = ?, ecological_score = ? " +
                "WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, plot.getAddress());
            statement.setDouble(2, plot.getArea());
            statement.setInt(3, plot.getEcologicalScore());
            statement.setLong(4, plot.getId());

            statement.executeUpdate();
        }
    }

    // Видалення земельної ділянки за ID
    public boolean delete(long id) throws SQLException {

        String sql = "DELETE FROM land_plot WHERE id = ?";

        try (Connection connection = DBUtil.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            return statement.executeUpdate() > 0;
        }
    }


}
