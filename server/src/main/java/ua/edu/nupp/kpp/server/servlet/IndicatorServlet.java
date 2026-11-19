
package ua.edu.nupp.kpp.server.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import ua.edu.nupp.kpp.server.service.IndicatorService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class IndicatorServlet extends HttpServlet {

    private final IndicatorService service = new IndicatorService();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            long plotId = Long.parseLong(
                    required(request, "plotId"));

            long violationId = Long.parseLong(
                    required(request, "violationId"));

            long observationId = Long.parseLong(
                    required(request, "observationId"));

            double result = service.calculateFromDatabase(
                    plotId, violationId, observationId);

            mapper.writeValue(
                    response.getWriter(),
                    Map.of("indicator", result)
            );

        } catch (IllegalArgumentException e) {
            response.setStatus(400);
            mapper.writeValue(
                    response.getWriter(),
                    Map.of("error", e.getMessage())
            );

        } catch (SQLException e) {
            log("Помилка розрахунку показника", e);
            response.setStatus(500);
            mapper.writeValue(
                    response.getWriter(),
                    Map.of("error", "Помилка бази даних")
            );
        }
    }

    private String required(HttpServletRequest request, String name) {
        String value = request.getParameter(name);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Відсутній параметр: " + name);
        }

        return value;
    }
}
