
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
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

public class ApiClient {

    private static final String BASE_URL =
            "http://localhost:8080/server/api/";

    private final HttpClient client = HttpClient.newHttpClient();

    private final ObjectMapper mapper =
            new ObjectMapper().findAndRegisterModules();

    // Отримання всіх записів
    public JsonNode getAll(String endpoint)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET()
                .build();

        return execute(request);
    }

    // Створення, редагування та видалення записів
    public JsonNode post(
            String endpoint,
            Map<String, String> data)
            throws IOException, InterruptedException {

        StringBuilder form = new StringBuilder();

        for (Map.Entry<String, String> entry : data.entrySet()) {

            if (form.length() > 0) {
                form.append("&");
            }

            form.append(encode(entry.getKey()))
                    .append("=")
                    .append(encode(entry.getValue()));
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header(
                        "Content-Type",
                        "application/x-www-form-urlencoded; charset=UTF-8"
                )
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                form.toString(),
                                StandardCharsets.UTF_8
                        )
                )
                .build();

        return execute(request);
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

        return execute(request);
    }

    // Імпорт повного CSV-файлу
    public JsonNode importCsv(File file)
            throws IOException, InterruptedException {

        String boundary = UUID.randomUUID().toString();

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        String header = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; "
                + "name=\"file\"; filename=\"import.csv\"\r\n"
                + "Content-Type: text/csv\r\n\r\n";

        String footer = "\r\n--" + boundary + "--\r\n";

        byte[] start = header.getBytes(StandardCharsets.UTF_8);
        byte[] end = footer.getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[
                start.length + fileBytes.length + end.length
                ];

        System.arraycopy(
                start,
                0,
                body,
                0,
                start.length
        );

        System.arraycopy(
                fileBytes,
                0,
                body,
                start.length,
                fileBytes.length
        );

        System.arraycopy(
                end,
                0,
                body,
                start.length + fileBytes.length,
                end.length
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "import"))
                .header(
                        "Content-Type",
                        "multipart/form-data; boundary=" + boundary
                )
                .POST(
                        HttpRequest.BodyPublishers.ofByteArray(body)
                )
                .build();

        return execute(request);
    }

    // Імпорт одного CSV-рядка
    public JsonNode importCsvRow(
            String header,
            String row)
            throws IOException, InterruptedException {

        Path tempFile = Files.createTempFile(
                "coursework-import-", ".csv"
        );

        try {

            Files.writeString(
                    tempFile,
                    header + "\n" + row + "\n",
                    StandardCharsets.UTF_8
            );

            return importCsv(tempFile.toFile());

        } finally {

            Files.deleteIfExists(tempFile);
        }
    }

    // Кодування параметрів HTTP-запиту
    private String encode(String value) {

        return URLEncoder.encode(
                value == null ? "" : value,
                StandardCharsets.UTF_8
        );
    }

    // Виконання HTTP-запиту та обробка відповіді
    private JsonNode execute(HttpRequest request)
            throws IOException, InterruptedException {

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        String body = response.body();

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            String error = body;

            try {

                JsonNode json = mapper.readTree(body);

                if (json.has("error")) {
                    error = json.get("error").asText();
                }

            } catch (Exception ignored) {
                // Якщо відповідь не JSON, використовуємо текст
            }

            throw new IOException(
                    "HTTP " + response.statusCode()
                            + ": " + error
            );
        }

        return mapper.readTree(body);
    }
}
