
package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

@MultipartConfig(
        maxFileSize = 2 * 1024 * 1024,
        maxRequestSize = 2 * 1024 * 1024
)
public class CsvImportServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Part file = request.getPart("file");

            if (file == null || file.getSize() == 0) {
                throw new IllegalArgumentException(
                        "CSV-файл не вибрано");
            }

            int imported = importCsv(file);

            mapper.writeValue(
                    response.getWriter(),
                    Map.of("success", true, "imported", imported)
            );

        } catch (IllegalArgumentException e) {

            response.setStatus(400);
            mapper.writeValue(
                    response.getWriter(),
                    Map.of("error", e.getMessage())
            );

        } catch (SQLException e) {

            log("Помилка імпорту CSV", e);

            response.setStatus(500);
            mapper.writeValue(
                    response.getWriter(),
                    Map.of("error", "Помилка бази даних: "
                            + e.getMessage())
            );
        }
    }

    private int importCsv(Part file)
            throws IOException, SQLException {

        try (Connection connection = DBUtil.getConnection()) {

            connection.setAutoCommit(false);

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            file.getInputStream(),
                            StandardCharsets.UTF_8))) {

                String header = reader.readLine();

                if (header == null) {
                    throw new IllegalArgumentException(
                            "CSV-файл порожній");
                }

                header = header.replace("\uFEFF", "").trim();

                String type = detectType(header);
                String sql = getInsertSql(type);

                int imported = 0;
                int lineNumber = 1;

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    String line;

                    while ((line = reader.readLine()) != null) {

                        lineNumber++;

                        if (line.isBlank()) {
                            continue;
                        }

                        String[] values = line.split(";", -1);

                        try {
                            switch (type) {

                                case "landplots" ->
                                        fillLandPlot(statement, values);

                                case "violations" ->
                                        fillViolation(
                                                connection,
                                                statement,
                                                values);

                                case "observations" ->
                                        fillObservation(
                                                connection,
                                                statement,
                                                values);

                                case "tasks" ->
                                        fillTask(
                                                connection,
                                                statement,
                                                values);

                                default ->
                                        throw new IllegalArgumentException(
                                                "Невідомий тип CSV");
                            }

                            statement.executeUpdate();
                            imported++;

                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(
                                    "Рядок " + lineNumber
                                            + ": " + e.getMessage(),
                                    e);
                        }
                    }
                }

                connection.commit();
                return imported;

            } catch (IOException | SQLException |
                     RuntimeException e) {

                connection.rollback();
                throw e;
            }
        }
    }

    private String detectType(String header) {

        return switch (header) {

            case "address;area;ecological_score" ->
                    "landplots";

            case "plot_id;description;severity;detected_date;status" ->
                    "violations";

            case "plot_id;observation_date;area_affected;comment" ->
                    "observations";

            case "violation_id;planned_date;completion_date;task_status" ->
                    "tasks";

            default -> throw new IllegalArgumentException(
                    "Невідомий формат CSV. Перевірте заголовок файлу.");
        };
    }

    private String getInsertSql(String type) {

        return switch (type) {

            case "landplots" ->
                    "INSERT INTO land_plot "
                            + "(address, area, ecological_score) "
                            + "VALUES (?, ?, ?)";

            case "violations" ->
                    "INSERT INTO violation "
                            + "(plot_id, description, severity, "
                            + "detected_date, status) "
                            + "VALUES (?, ?, ?, ?, ?)";

            case "observations" ->
                    "INSERT INTO observation "
                            + "(plot_id, observation_date, "
                            + "area_affected, comment) "
                            + "VALUES (?, ?, ?, ?)";

            case "tasks" ->
                    "INSERT INTO inspection_task "
                            + "(violation_id, planned_date, "
                            + "completion_date, task_status) "
                            + "VALUES (?, ?, ?, ?)";

            default -> throw new IllegalArgumentException(
                    "Невідомий тип даних");
        };
    }

    private void fillLandPlot(
            PreparedStatement statement,
            String[] values) throws SQLException {

        requireColumns(values, 3);

        String address = required(values[0], "address");
        double area = positiveDouble(values[1], "area");
        int score = percentage(values[2], "ecological_score");

        statement.setString(1, address);
        statement.setDouble(2, area);
        statement.setInt(3, score);
    }

    private void fillViolation(
            Connection connection,
            PreparedStatement statement,
            String[] values) throws SQLException {

        requireColumns(values, 5);

        long plotId = positiveLong(values[0], "plot_id");

        checkExists(connection, "land_plot", plotId);

        String description =
                required(values[1], "description");

        int severity = percentage(values[2], "severity");

        Date detectedDate =
                parseDate(values[3], "detected_date", false);

        String status = required(values[4], "status");

        if (!status.equals("ВИЯВЛЕНО")) {
            throw new IllegalArgumentException(
                    "Нове порушення повинно мати статус ВИЯВЛЕНО");
        }

        statement.setLong(1, plotId);
        statement.setString(2, description);
        statement.setInt(3, severity);
        statement.setDate(4, detectedDate);
        statement.setString(5, status);
    }

    private void fillObservation(
            Connection connection,
            PreparedStatement statement,
            String[] values) throws SQLException {

        requireColumns(values, 4);

        long plotId = positiveLong(values[0], "plot_id");

        checkExists(connection, "land_plot", plotId);

        Date observationDate =
                parseDate(values[1], "observation_date", false);

        double areaAffected =
                nonNegativeDouble(values[2], "area_affected");

        String comment = values[3].trim();

        statement.setLong(1, plotId);
        statement.setDate(2, observationDate);
        statement.setDouble(3, areaAffected);
        statement.setString(4, comment);
    }

    private void fillTask(
            Connection connection,
            PreparedStatement statement,
            String[] values) throws SQLException {

        requireColumns(values, 4);

        long violationId =
                positiveLong(values[0], "violation_id");

        checkExists(connection, "violation", violationId);

        Date plannedDate =
                parseDate(values[1], "planned_date", false);

        Date completionDate =
                parseDate(values[2], "completion_date", true);

        String taskStatus =
                required(values[3], "task_status");

        statement.setLong(1, violationId);
        statement.setDate(2, plannedDate);

        if (completionDate == null) {
            statement.setNull(3, java.sql.Types.DATE);
        } else {
            statement.setDate(3, completionDate);
        }

        statement.setString(4, taskStatus);
    }

    private void checkExists(
            Connection connection,
            String table,
            long id) throws SQLException {

        // Назви таблиць визначені тільки в коді,
        // а не беруться з CSV-файлу.
        if (!table.equals("land_plot")
                && !table.equals("violation")) {
            throw new IllegalArgumentException(
                    "Невідома таблиця");
        }

        String sql = "SELECT 1 FROM " + table
                + " WHERE id = ?";

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    throw new IllegalArgumentException(
                            "Запис ID=" + id
                                    + " не існує в таблиці " + table);
                }
            }
        }
    }

    private void requireColumns(String[] values, int count) {

        if (values.length != count) {
            throw new IllegalArgumentException(
                    "Очікується " + count
                            + " стовпців, отримано "
                            + values.length);
        }
    }

    private String required(String value, String field) {

        String result = value.trim();

        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    "Порожнє поле " + field);
        }

        return result;
    }

    private long positiveLong(String value, String field) {

        try {
            long result = Long.parseLong(value.trim());

            if (result <= 0) {
                throw new NumberFormatException();
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Некоректний ID у полі " + field);
        }
    }

    private double nonNegativeDouble(
            String value, String field) {

        try {
            double result = Double.parseDouble(value.trim());

            if (!Double.isFinite(result) || result < 0) {
                throw new NumberFormatException();
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Некоректне число у полі " + field);
        }
    }

    private double positiveDouble(
            String value, String field) {

        double result = nonNegativeDouble(value, field);

        if (result <= 0) {
            throw new IllegalArgumentException(
                    "Поле " + field + " повинно бути більше 0");
        }

        return result;
    }

    private int percentage(String value, String field) {

        try {
            int result = Integer.parseInt(value.trim());

            if (result < 0 || result > 100) {
                throw new NumberFormatException();
            }

            return result;

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Поле " + field
                            + " повинно бути від 0 до 100");
        }
    }

    private Date parseDate(
            String value,
            String field,
            boolean optional) {

        String text = value.trim();

        if (text.isEmpty() && optional) {
            return null;
        }

        try {
            return Date.valueOf(LocalDate.parse(text));

        } catch (DateTimeParseException |
                 IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Некоректна дата у полі " + field
                            + ". Формат: YYYY-MM-DD");
        }
    }
}
