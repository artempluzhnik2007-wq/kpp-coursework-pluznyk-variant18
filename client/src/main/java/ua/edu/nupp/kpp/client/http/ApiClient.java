
package ua.edu.nupp.kpp.client.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

public class ApiClient {

    private static final String BASE_URL =
            "http://localhost:8080/server/api/";

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper mapper = new ObjectMapper()
            .findAndRegisterModules();

    // Отримання записів
    public JsonNode getAll(String endpoint)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8)
        );

        checkResponse(response);
        return mapper.readTree(response.body());
    }

    // Створення, редагування та видалення записів
    public JsonNode post(String endpoint, Map<String, String> data)
            throws IOException, InterruptedException {

        StringBuilder body = new StringBuilder();

        for (Map.Entry<String, String> entry : data.entrySet()) {

            if (body.length() > 0) {
                body.append("&");
            }

            body.append(URLEncoder.encode(
                    entry.getKey(), StandardCharsets.UTF_8));

            body.append("=");

            body.append(URLEncoder.encode(
                    entry.getValue(), StandardCharsets.UTF_8));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(
                        body.toString(), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8)
        );

        checkResponse(response);
        return mapper.readTree(response.body());
    }

    // Розрахунок інтегрального показника
    public JsonNode calculateIndicator(
            long plotId,
            long violationId,
            long observationId)
            throws IOException, InterruptedException {

        String url = BASE_URL + "indicator"
                + "?plotId=" + plotId
                + "&violationId=" + violationId
                + "&observationId=" + observationId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8)
        );

        checkResponse(response);
        return mapper.readTree(response.body());
    }

    // Імпорт CSV-файлу
    public JsonNode importCsv(File file)
            throws IOException, InterruptedException {

        String boundary = "Boundary-" + UUID.randomUUID();

        byte[] header = (
                "--" + boundary + "\r\n"
                        + "Content-Disposition: form-data; "
                        + "name=\"file\"; filename=\""
                        + "import.csv\"\r\n"
                        + "Content-Type: text/csv\r\n\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        byte[] footer = (
                "\r\n--" + boundary + "--\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[
                header.length + fileBytes.length + footer.length
                ];

        System.arraycopy(
                header, 0, body, 0, header.length);

        System.arraycopy(
                fileBytes, 0, body,
                header.length, fileBytes.length);

        System.arraycopy(
                footer, 0, body,
                header.length + fileBytes.length,
                footer.length);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "import"))
                .header("Content-Type",
                        "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8)
        );

        checkResponse(response);
        return mapper.readTree(response.body());
    }

    // Перевірка відповіді сервера
    private void checkResponse(HttpResponse<String> response)
            throws IOException {

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new IOException(
                    "HTTP " + response.statusCode()
                            + ": " + response.body()
            );
        }
    }
}
