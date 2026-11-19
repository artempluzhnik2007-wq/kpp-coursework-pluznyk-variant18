
package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.model.LandPlot;
import ua.edu.nupp.kpp.server.service.LandPlotService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class LandPlotServlet extends HttpServlet {

    private final ObjectMapper mapper = new ObjectMapper();
    private final LandPlotService service = new LandPlotService();

    // GET - получить все участки
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            List<LandPlot> plots = service.getAll();
            mapper.writeValue(response.getWriter(), plots);

        } catch (SQLException e) {
            log("Помилка отримання ділянок", e);
            sendError(response, 500, "Помилка бази даних");
        }
    }

    // POST - создание, редактирование, удаление
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String action = request.getParameter("action");

            if (action == null || action.isBlank()) {
                action = "create";
            }

            switch (action) {
                case "create":
                    LandPlot newPlot = readPlot(request, false);
                    service.create(newPlot);

                    response.setStatus(201);
                    mapper.writeValue(response.getWriter(),
                            Map.of("message", "Ділянку створено"));
                    break;

                case "update":
                    LandPlot updatedPlot = readPlot(request, true);
                    service.update(updatedPlot);

                    mapper.writeValue(response.getWriter(),
                            Map.of("message", "Ділянку оновлено"));
                    break;

                case "delete":
                    long id = Long.parseLong(
                            required(request, "id")
                    );

                    boolean deleted = service.delete(id);

                    if (deleted) {
                        mapper.writeValue(response.getWriter(),
                                Map.of("message", "Ділянку видалено"));
                    } else {
                        sendError(response, 404, "Ділянку не знайдено");
                    }
                    break;

                default:
                    sendError(response, 400, "Невідома дія: " + action);
            }

        } catch (IllegalArgumentException e) {
            sendError(response, 400, e.getMessage());

        } catch (SQLException e) {
            log("Помилка операції із земельною ділянкою", e);
            sendError(response, 500, "Помилка бази даних");
        }
    }

    // Читаем параметры земельного участка
    private LandPlot readPlot(HttpServletRequest request,
                              boolean withId) {

        LandPlot plot = new LandPlot();

        if (withId) {
            plot.setId(Long.parseLong(required(request, "id")));
        }

        plot.setAddress(required(request, "address"));
        plot.setArea(Double.parseDouble(required(request, "area")));
        plot.setEcologicalScore(Integer.parseInt(
                required(request, "ecologicalScore")
        ));

        return plot;
    }

    // Проверяем наличие параметра
    private String required(HttpServletRequest request, String name) {
        String value = request.getParameter(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Відсутній параметр: " + name
            );
        }

        return value;
    }

    // JSON-ответ при ошибке
    private void sendError(HttpServletResponse response,
                           int status,
                           String message) throws IOException {

        response.setStatus(status);
        mapper.writeValue(response.getWriter(),
                Map.of("error", message));
    }
}
