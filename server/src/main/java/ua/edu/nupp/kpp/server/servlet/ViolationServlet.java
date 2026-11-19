
package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.model.Violation;
import ua.edu.nupp.kpp.server.service.ViolationService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class ViolationServlet extends HttpServlet {

    private final ViolationService service = new ViolationService();
    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {
        prepare(response);

        try {
            mapper.writeValue(response.getWriter(), service.getAll());
        } catch (SQLException e) {
            log("Помилка отримання порушень", e);
            error(response, 500, "Помилка бази даних");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        prepare(response);

        try {
            String action = request.getParameter("action");
            if (action == null || action.isBlank()) {
                action = "create";
            }

            switch (action) {
                case "create":
                    service.create(readViolation(request, false));
                    response.setStatus(201);
                    success(response, "Порушення створено");
                    break;

                case "update":
                    service.update(readViolation(request, true));
                    success(response, "Порушення оновлено");
                    break;

                case "delete":
                    long id = Long.parseLong(required(request, "id"));
                    if (service.delete(id)) {
                        success(response, "Порушення видалено");
                    } else {
                        error(response, 404, "Порушення не знайдено");
                    }
                    break;

                default:
                    error(response, 400, "Невідома дія: " + action);
            }

        } catch (IllegalArgumentException e) {
            error(response, 400, e.getMessage());
        } catch (SQLException e) {
            log("Помилка операції з порушенням", e);
            error(response, 500, "Помилка бази даних");
        }
    }

    private Violation readViolation(HttpServletRequest request,
                                    boolean withId) {
        Violation v = new Violation();

        if (withId) {
            v.setId(Long.parseLong(required(request, "id")));
        }

        v.setPlotId(Long.parseLong(required(request, "plotId")));
        v.setDescription(required(request, "description"));
        v.setSeverity(Integer.parseInt(required(request, "severity")));
        v.setDetectedDate(LocalDate.parse(required(request, "detectedDate")));
        v.setStatus(required(request, "status"));

        return v;
    }

    private String required(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Відсутній параметр: " + name);
        }
        return value;
    }

    private void prepare(HttpServletResponse response) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
    }

    private void success(HttpServletResponse response, String message)
            throws IOException {
        mapper.writeValue(response.getWriter(), Map.of("message", message));
    }

    private void error(HttpServletResponse response, int status,
                       String message) throws IOException {
        response.setStatus(status);
        mapper.writeValue(response.getWriter(), Map.of("error", message));
    }
}

