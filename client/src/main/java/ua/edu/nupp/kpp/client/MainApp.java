
package ua.edu.nupp.kpp.client;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import ua.edu.nupp.kpp.client.http.ApiClient;

import java.io.File;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class MainApp extends Application {

    private final ApiClient api = new ApiClient();

    private static final String[] STATUSES = {
            "ВИЯВЛЕНО",
            "ПЕРЕВІРЕНО",
            "ЗАХІД_ЗАПЛАНОВАНО",
            "ЗАКРИТО"
    };

    @Override
    public void start(Stage stage) {

        Label title = new Label(
                "Облік порушень земельного законодавства"
        );
        title.getStyleClass().add("app-title");

        Label subtitle = new Label(
                "Інформаційна система моніторингу земельних ділянок"
        );
        subtitle.getStyleClass().add("app-subtitle");

        VBox header = new VBox(5, title, subtitle);
        header.getStyleClass().add("app-header");

        TabPane tabs = new TabPane();

        tabs.setTabClosingPolicy(
                TabPane.TabClosingPolicy.UNAVAILABLE
        );

        tabs.getTabs().addAll(
                createTab(
                        "Земельні ділянки",
                        "landplots",
                        "id", "address", "area", "ecologicalScore"
                ),
                createTab(
                        "Порушення",
                        "violations",
                        "id", "plotId", "description",
                        "severity", "detectedDate", "status"
                ),
                createTab(
                        "Спостереження",
                        "observations",
                        "id", "plotId", "observationDate",
                        "areaAffected", "comment"
                ),
                createTab(
                        "Завдання перевірки",
                        "tasks",
                        "id", "violationId", "plannedDate",
                        "completionDate", "taskStatus"
                )
        );

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1200, 720);

        var css = getClass().getResource("/style.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Курсова робота — Варіант 18");
        stage.setMinWidth(900);
        stage.setMinHeight(550);
        stage.setScene(scene);
        stage.show();
    }

    private Tab createTab(
            String name,
            String endpoint,
            String... fields) {

        TableView<Map<String, String>> table =
                new TableView<>();

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );

        ObservableList<Map<String, String>> rows =
                FXCollections.observableArrayList();

        table.setItems(rows);
        table.setPlaceholder(new Label("Немає записів"));

        for (String field : fields) {

            TableColumn<Map<String, String>, String> column =
                    new TableColumn<>(columnName(field));

            column.setCellValueFactory(data ->
                    new SimpleStringProperty(
                            data.getValue().getOrDefault(field, "")
                    )
            );

            column.setPrefWidth(
                    field.equals("address") ||
                            field.equals("description") ? 250 : 150
            );

            table.getColumns().add(column);
        }

        Label countLabel = new Label("Записів: 0");
        countLabel.getStyleClass().add("status-label");

        Label statusLabel = new Label("Готово");
        statusLabel.getStyleClass().add("status-label");

        Button add = new Button("＋ Додати");
        Button edit = new Button("✎ Редагувати");
        Button delete = new Button("Видалити");
        Button refreshButton = new Button("⟳ Оновити");

        Button indicatorButton =
                new Button("Розрахувати показник");

        Button importButton =
                new Button("Імпорт CSV");

        add.getStyleClass().add("primary-button");
        delete.getStyleClass().add("danger-button");

        indicatorButton.getStyleClass().add("indicator-button");
        importButton.getStyleClass().add("indicator-button");

        // Завантаження даних з сервера
        Runnable refresh = () -> {

            statusLabel.setText("Завантаження...");

            Thread worker = new Thread(() -> {

                try {
                    JsonNode json = api.getAll(endpoint);

                    if (!json.isArray()) {
                        throw new IllegalStateException(
                                "Сервер повернув не масив JSON"
                        );
                    }

                    ObservableList<Map<String, String>> loaded =
                            FXCollections.observableArrayList();

                    for (JsonNode item : json) {

                        Map<String, String> row =
                                new LinkedHashMap<>();

                        for (String field : fields) {

                            JsonNode value = item.path(field);

                            row.put(
                                    field,
                                    value.isMissingNode() || value.isNull()
                                            ? "" : value.asText()
                            );
                        }

                        loaded.add(row);
                    }

                    Platform.runLater(() -> {
                        rows.setAll(loaded);

                        countLabel.setText(
                                "Записів: " + loaded.size()
                        );

                        statusLabel.setText("Дані оновлено");
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Помилка підключення");
                        showError(ex.getMessage());
                    });
                }
            });

            worker.setDaemon(true);
            worker.start();
        };

        // CREATE
        add.setOnAction(e -> {

            Map<String, String> data =
                    showForm(
                            "Додати: " + name,
                            endpoint,
                            fields,
                            null
                    );

            if (data != null) {
                data.put("action", "create");
                send(endpoint, data, refresh, statusLabel);
            }
        });

        // UPDATE
        edit.setOnAction(e -> {

            Map<String, String> selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showError("Спочатку виберіть запис у таблиці");
                return;
            }

            Map<String, String> data =
                    showForm(
                            "Редагувати: " + name,
                            endpoint,
                            fields,
                            selected
                    );

            if (data != null) {
                data.put("action", "update");
                data.put("id", selected.get("id"));

                send(endpoint, data, refresh, statusLabel);
            }
        });

        // DELETE
        delete.setOnAction(e -> {

            Map<String, String> selected =
                    table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                showError("Спочатку виберіть запис у таблиці");
                return;
            }

            Alert confirm =
                    new Alert(Alert.AlertType.CONFIRMATION);

            confirm.setTitle("Підтвердження");
            confirm.setHeaderText("Видалити запис?");
            confirm.setContentText("ID: " + selected.get("id"));

            if (confirm.showAndWait().orElse(ButtonType.CANCEL)
                    == ButtonType.OK) {

                Map<String, String> data =
                        new LinkedHashMap<>();

                data.put("action", "delete");
                data.put("id", selected.get("id"));

                send(endpoint, data, refresh, statusLabel);
            }
        });

        refreshButton.setOnAction(e -> refresh.run());

        indicatorButton.setOnAction(e ->
                showIndicatorDialog()
        );

        // CSV-імпорт для кожної вкладки
        importButton.setOnAction(e -> {

            FileChooser chooser = new FileChooser();

            chooser.setTitle(
                    "Виберіть CSV-файл: " + name
            );

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(
                            "CSV-файли (*.csv)", "*.csv"
                    )
            );

            File file = chooser.showOpenDialog(
                    table.getScene().getWindow()
            );

            if (file == null) {
                return;
            }

            // Перевірка заголовка перед імпортом
            String expectedHeader = expectedCsvHeader(endpoint);

            try {
                String header = java.nio.file.Files.readAllLines(
                        file.toPath(),
                        java.nio.charset.StandardCharsets.UTF_8
                ).stream().findFirst().orElse("");

                header = header.replace("\uFEFF", "").trim();

                if (!header.equals(expectedHeader)) {
                    showError(
                            "Цей CSV-файл не відповідає вкладці \""
                                    + name + "\".\n\n"
                                    + "Очікуваний заголовок:\n"
                                    + expectedHeader
                    );
                    return;
                }

            } catch (Exception ex) {
                showError(
                        "Не вдалося прочитати CSV-файл: "
                                + ex.getMessage()
                );
                return;
            }

            Alert confirm =
                    new Alert(Alert.AlertType.CONFIRMATION);

            confirm.setTitle("Імпорт CSV");
            confirm.setHeaderText("Імпортувати файл?");

            confirm.setContentText(
                    "Файл: " + file.getName()
                            + "\nВкладка: " + name
                            + "\n\nЗаписи будуть додані до бази даних."
                            + "\nПовторний імпорт може створити дублікати."
            );

            if (confirm.showAndWait().orElse(ButtonType.CANCEL)
                    != ButtonType.OK) {
                return;
            }

            statusLabel.setText("Імпорт CSV...");

            Thread worker = new Thread(() -> {

                try {
                    JsonNode result = api.importCsv(file);

                    if (!result.path("success").asBoolean(false)
                            || !result.path("imported").isInt()) {
                        throw new IllegalStateException(
                                "Некоректна відповідь сервера"
                        );
                    }

                    int imported = result.path("imported").asInt();

                    Platform.runLater(() -> {

                        refresh.run();

                        Alert alert = new Alert(
                                Alert.AlertType.INFORMATION
                        );

                        alert.setTitle("Імпорт завершено");
                        alert.setHeaderText(
                                "CSV-файл успішно імпортовано"
                        );

                        alert.setContentText(
                                "Додано записів: " + imported
                        );

                        alert.showAndWait();
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        statusLabel.setText("Помилка імпорту");
                        showError(ex.getMessage());
                    });
                }
            });

            worker.setDaemon(true);
            worker.start();
        });

        // Панель кнопок
        HBox toolbar = new HBox(
                10, add, edit, delete, refreshButton
        );

        if (endpoint.equals("landplots")) {
            toolbar.getChildren().add(indicatorButton);
        }

        // Кнопка імпорту на всіх чотирьох вкладках
        toolbar.getChildren().add(importButton);

        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.getStyleClass().add("action-toolbar");

        HBox footer = new HBox(
                20, countLabel, statusLabel
        );

        footer.setAlignment(Pos.CENTER_LEFT);
        footer.getStyleClass().add("status-bar");

        BorderPane content = new BorderPane();
        content.setTop(toolbar);
        content.setCenter(table);
        content.setBottom(footer);

        Tab tab = new Tab(name, content);

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                refresh.run();
            }
        });

        if (endpoint.equals("landplots")) {
            refresh.run();
        }

        return tab;
    }

    // Очікувані заголовки CSV
    private String expectedCsvHeader(String endpoint) {

        return switch (endpoint) {

            case "landplots" ->
                    "address;area;ecological_score";

            case "violations" ->
                    "plot_id;description;severity;detected_date;status";

            case "observations" ->
                    "plot_id;observation_date;area_affected;comment";

            case "tasks" ->
                    "violation_id;planned_date;completion_date;task_status";

            default -> "";
        };
    }

    // Українські назви стовпців
    private String columnName(String field) {

        return switch (field) {

            case "id" -> "ID";
            case "address" -> "Адреса";
            case "area" -> "Площа";
            case "ecologicalScore" -> "Екологічний бал";
            case "plotId" -> "ID ділянки";
            case "description" -> "Опис порушення";
            case "severity" -> "Серйозність";
            case "detectedDate" -> "Дата виявлення";
            case "status" -> "Статус порушення";
            case "observationDate" -> "Дата спостереження";
            case "areaAffected" -> "Уражена площа";
            case "comment" -> "Коментар";
            case "violationId" -> "ID порушення";
            case "plannedDate" -> "Запланована дата";
            case "completionDate" -> "Дата завершення";
            case "taskStatus" -> "Статус завдання";

            default -> field;
        };
    }

    private boolean isDateField(String field) {

        return field.equals("detectedDate")
                || field.equals("observationDate")
                || field.equals("plannedDate")
                || field.equals("completionDate");
    }

    // Форма додавання та редагування
    private Map<String, String> showForm(
            String title,
            String endpoint,
            String[] fields,
            Map<String, String> existing) {

        Dialog<Map<String, String>> dialog = new Dialog<>();

        dialog.setTitle(title);
        dialog.setHeaderText("Заповніть дані запису");

        ButtonType saveButton = new ButtonType(
                "Зберегти",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                saveButton, ButtonType.CANCEL
        );

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        Map<String, Control> inputs =
                new LinkedHashMap<>();

        int row = 0;

        for (String field : fields) {

            if (field.equals("id")) {
                continue;
            }

            Control input;

            String currentValue = existing == null
                    ? ""
                    : existing.getOrDefault(field, "");

            if (field.equals("status")
                    && endpoint.equals("violations")) {

                ComboBox<String> combo = new ComboBox<>();

                combo.getItems().addAll(STATUSES);

                if (existing == null) {
                    combo.setValue("ВИЯВЛЕНО");
                    combo.setDisable(true);

                } else {
                    String oldStatus = currentValue;

                    combo.setValue(oldStatus);
                    combo.getItems().setAll(oldStatus);

                    String next = nextStatus(oldStatus);

                    if (next != null) {
                        combo.getItems().add(next);
                    }
                }

                combo.setMaxWidth(Double.MAX_VALUE);
                input = combo;

            } else if (isDateField(field)) {

                DatePicker picker = new DatePicker();

                if (!currentValue.isBlank()) {
                    try {
                        picker.setValue(
                                LocalDate.parse(currentValue)
                        );
                    } catch (Exception ignored) {
                        // Некоректна дата
                    }
                }

                picker.setConverter(
                        new StringConverter<LocalDate>() {

                            @Override
                            public String toString(LocalDate date) {
                                return date == null
                                        ? "" : date.toString();
                            }

                            @Override
                            public LocalDate fromString(String text) {
                                return text == null || text.isBlank()
                                        ? null
                                        : LocalDate.parse(text.trim());
                            }
                        }
                );

                picker.setMaxWidth(Double.MAX_VALUE);
                input = picker;

            } else {
                TextField text = new TextField(currentValue);
                text.setPrefWidth(320);
                input = text;
            }

            grid.add(
                    new Label(columnName(field) + ":"),
                    0, row
            );

            grid.add(input, 1, row);

            inputs.put(field, input);
            row++;
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {

            if (button != saveButton) {
                return null;
            }

            Map<String, String> result =
                    new LinkedHashMap<>();

            inputs.forEach((field, control) -> {

                String value = "";

                if (control instanceof TextField text) {
                    value = text.getText().trim();

                } else if (control instanceof DatePicker picker) {
                    value = picker.getValue() == null
                            ? ""
                            : picker.getValue().toString();

                } else if (control instanceof ComboBox<?> combo) {
                    value = combo.getValue() == null
                            ? ""
                            : combo.getValue().toString();
                }

                result.put(field, value);
            });

            return result;
        });

        return dialog.showAndWait().orElse(null);
    }

    private String nextStatus(String current) {

        return switch (current) {

            case "ВИЯВЛЕНО" -> "ПЕРЕВІРЕНО";
            case "ПЕРЕВІРЕНО" -> "ЗАХІД_ЗАПЛАНОВАНО";
            case "ЗАХІД_ЗАПЛАНОВАНО" -> "ЗАКРИТО";

            default -> null;
        };
    }

    // Відправлення POST-запитів
    private void send(
            String endpoint,
            Map<String, String> data,
            Runnable refresh,
            Label statusLabel) {

        statusLabel.setText("Виконання операції...");

        Thread worker = new Thread(() -> {

            try {
                api.post(endpoint, data);

                Platform.runLater(() -> {

                    refresh.run();

                    Alert alert = new Alert(
                            Alert.AlertType.INFORMATION
                    );

                    alert.setTitle("Успіх");
                    alert.setHeaderText(null);
                    alert.setContentText(
                            "Операцію виконано успішно"
                    );

                    alert.show();
                });

            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Помилка операції");
                    showError(ex.getMessage());
                });
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    // Розрахунок інтегрального показника
    private void showIndicatorDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();

        dialog.setTitle(
                "Розрахунок інтегрального показника"
        );

        dialog.setHeaderText("Введіть ID записів");

        ButtonType calculateButton = new ButtonType(
                "Розрахувати",
                ButtonBar.ButtonData.OK_DONE
        );

        dialog.getDialogPane().getButtonTypes().addAll(
                calculateButton, ButtonType.CANCEL
        );

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField plotId = new TextField();
        TextField violationId = new TextField();
        TextField observationId = new TextField();

        grid.add(
                new Label("ID земельної ділянки:"), 0, 0
        );
        grid.add(plotId, 1, 0);

        grid.add(
                new Label("ID порушення:"), 0, 1
        );
        grid.add(violationId, 1, 1);

        grid.add(
                new Label("ID спостереження:"), 0, 2
        );
        grid.add(observationId, 1, 2);

        dialog.getDialogPane().setContent(grid);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL)
                != calculateButton) {
            return;
        }

        final long p;
        final long v;
        final long o;

        try {
            p = Long.parseLong(plotId.getText().trim());
            v = Long.parseLong(violationId.getText().trim());
            o = Long.parseLong(observationId.getText().trim());

            if (p <= 0 || v <= 0 || o <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException ex) {
            showError("Введіть коректні додатні ID");
            return;
        }

        Thread worker = new Thread(() -> {

            try {
                JsonNode result =
                        api.calculateIndicator(p, v, o);

                JsonNode value = result.get("indicator");

                if (value == null || !value.isNumber()) {
                    throw new IllegalStateException(
                            "Сервер не повернув числовий показник"
                    );
                }

                double indicator = value.asDouble();

                Platform.runLater(() -> {

                    Alert alert = new Alert(
                            Alert.AlertType.INFORMATION
                    );

                    alert.setTitle("Результат розрахунку");
                    alert.setHeaderText("Інтегральний показник");

                    alert.setContentText(
                            String.format(
                                    Locale.forLanguageTag("uk-UA"),
                                    "I = %.2f",
                                    indicator
                            )
                    );

                    alert.showAndWait();
                });

            } catch (Exception ex) {
                Platform.runLater(() ->
                        showError(ex.getMessage())
                );
            }
        });

        worker.setDaemon(true);
        worker.start();
    }

    // Відображення помилок
    private void showError(String message) {

        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Помилка");
        alert.setHeaderText("Операцію не виконано");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
