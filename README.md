# Курсова робота — Варіант 18

## Облік порушень земельного законодавства

Клієнт-серверна інформаційна система для обліку земельних ділянок, виявлених порушень, спостережень та завдань перевірки.

Проєкт розроблено в межах курсової роботи з кросплатформного програмування.

## Використані технології

- Java 21
- JavaFX 21
- Apache Maven
- Apache Tomcat 9
- MySQL
- JDBC
- Java Servlet API
- HTTP / JSON
- Jackson
- JUnit 5
- CSV (UTF-8)

## Структура проєкту

```text
kpp-coursework-pluznyk-variant18/
├── server/
│   ├── src/main/java/
│   │   └── ua/edu/nupp/kpp/server/
│   │       ├── model/
│   │       ├── dao/
│   │       ├── service/
│   │       └── servlet/
│   ├── src/main/webapp/WEB-INF/
│   │   └── web.xml
│   ├── src/test/java/
│   └── pom.xml
├── client/
│   ├── src/main/java/
│   ├── src/main/resources/
│   └── pom.xml
├── sample-data/
├── database.sql
├── pom.xml
└── README.md
```

## Основні можливості

### CRUD-операції

Система підтримує створення, перегляд, редагування та видалення записів для чотирьох сутностей:

1. LandPlot — земельні ділянки.
2. Violation — порушення земельного законодавства.
3. Observation — спостереження.
4. InspectionTask — завдання перевірки.

### Життєвий цикл порушення

Передбачено чотири послідовні стани:

ВИЯВЛЕНО → ПЕРЕВІРЕНО → ЗАХІД_ЗАПЛАНОВАНО → ЗАКРИТО

Перехід до наступного стану дозволено лише у визначеному порядку. Пропуск етапів і повернення до попереднього стану заборонені.

### Інтегральний показник

Для оцінювання стану земельної ділянки використовується формула:

I = 0.40 × F1 + 0.35 × F2 + 0.25 × F3

де:

- F1 — частка ураженої площі у відсотках, обмежена значенням 100;
- F2 — рівень серйозності порушення;
- F3 — екологічний бал земельної ділянки.

Фактори мають значення від 0 до 100.

### CSV-імпорт

Реалізовано фоновий імпорт CSV-файлів через JavaFX Task.

Можливості:

- читання UTF-8;
- перевірка заголовків та кількості полів;
- послідовне передавання записів на сервер;
- відображення прогресу через ProgressBar;
- блокування повторного запуску імпорту;
- обробка помилок;
- журналювання через Logger;
- автоматичне оновлення таблиці після завершення.

Підтримувані формати:

**land_plots.csv**

```csv
address;area;ecological_score
Тестова ділянка;1000.0;75
```

**violations.csv**

```csv
plot_id;description;severity;detected_date;status
1;Порушення земельного законодавства;60;2026-10-15;ВИЯВЛЕНО
```

**observations.csv**

```csv
plot_id;observation_date;area_affected;comment
1;2026-10-16;250.0;Огляд земельної ділянки
```

**inspection_tasks.csv**

```csv
violation_id;planned_date;completion_date;task_status
1;2026-11-25;;ЗАПЛАНОВАНО
```

Роздільник полів — крапка з комою (`;`).

Під час імпорту необхідно враховувати зв'язки між записами та наявність відповідних ID у базі даних.

Повторний імпорт може створювати дублікати. При построковому імпорті записи, успішно додані до виникнення помилки, залишаються в базі даних.

## Підготовка бази даних

1. Запустити MySQL.
2. Створити базу даних `land_violations`.
3. Виконати SQL-команди з файлу `database.sql`.
4. Налаштувати параметри підключення в серверній частині.

Параметри підключення мають відповідати локальному середовищу.

Не публікуйте паролі та інші секретні дані в GitHub.

## Складання сервера

Відкрити термінал у кореневій папці проєкту.

```bash
cd server
mvn clean package
```

Після успішного складання створюється:

```text
server/target/server.war
```

## Запуск сервера

1. Встановити та налаштувати Apache Tomcat 9.
2. Скопіювати `server.war` до папки `webapps` Tomcat.
3. Запустити Tomcat.
4. Перевірити HTTP API.

Приклад адреси:

```text
http://localhost:8080/server/api/landplots
```

Сервер повертає JSON-дані.

## Запуск JavaFX-клієнта

У кореневій папці проєкту:

```bash
cd client
mvn javafx:run
```

Перед запуском клієнта сервер Tomcat і MySQL повинні працювати.

## Автоматизоване тестування

Для запуску JUnit 5:

```bash
cd server
mvn clean test
```

Реалізовано 25 модульних тестів:

- 15 тестів IndicatorServiceTest;
- 10 тестів ViolationLifecycleTest.

Перевіряються:

- правильність інтегрального показника;
- нормування факторів;
- обробка недопустимих значень;
- дозволені переходи життєвого циклу;
- заборонені переходи;
- незмінність статусу під час редагування.

Результати перевірки:

```text
Tests run: 25
Failures: 0
Errors: 0
Skipped: 0

BUILD SUCCESS
```

## HTTP API

Основні маршрути:

- GET/POST `/server/api/landplots`
- GET/POST `/server/api/violations`
- GET/POST `/server/api/observations`
- GET/POST `/server/api/tasks`
- GET `/server/api/indicator`
- POST `/server/api/import`

Для CRUD-операцій використовуються HTTP-запити та JSON-відповіді.

## Демонстрація роботи

Для захисту курсової роботи передбачено демонстрацію:

1. Запуск MySQL та Tomcat.
2. Запуск JavaFX-клієнта.
3. Перегляд чотирьох вкладок.
4. Виконання CRUD-операцій.
5. Перевірка переходів статусів.
6. Розрахунок інтегрального показника.
7. Фоновий CSV-імпорт.
8. Демонстрація обробки помилок.
9. Запуск тестів JUnit 5.
10. Перегляд вихідного коду в GitHub.

## Автор

Плужник Артем

ПОМІТКИ ДЛЯ СЕБЕ

Курсова робота, варіант 18.
Для включення Tomcat9 в Powershell

cd C:\Users\artem\Tomcat9\bin
.\catalina.bat run

Сайти Tomcat для перевірки Servlet
http://localhost:8080/server/api/violations

http://localhost:8080/server/api/observations

http://localhost:8080/server/api/tasks

http://localhost:8080/server/api/landplots

Команди для перевірки Servlet
GET — получить всі участки
POST — створити участок
UPDATE — поміняти участок
DELETE — удалить участок

LandplotServlet

GET — получить всі участки
curl http://localhost:8080/server/api/landplots
POST — створити участок
curl -X POST "http://localhost:8080/server/api/landplots" --data-urlencode "action=create" --data-urlencode "address=Test LandPlot" --data-urlencode "area=350.5" --data-urlencode "ecologicalScore=75"
UPDATE — поміняти участок
curl -X POST "http://localhost:8080/server/api/landplots" --data-urlencode "action=update" --data-urlencode "id=6" --data-urlencode "address=Updated LandPlot" --data-urlencode "area=450.5" --data-urlencode "ecologicalScore=85"
DELETE — удалить участок
curl -X POST "http://localhost:8080/server/api/landplots" -d "action=delete&id=6"

ViolationServlet

GET — получить всі участки
curl http://localhost:8080/server/api/violations
POST — створити участок
curl -X POST "http://localhost:8080/server/api/violations" --data-urlencode "action=create" --data-urlencode "plotId=1" --data-urlencode "description=Test violation" --data-urlencode "severity=70" --data-urlencode "detectedDate=2026-11-19" --data-urlencode "status=ВИЯВЛЕНО"
UPDATE — поміняти участок
curl -X POST "http://localhost:8080/server/api/violations" --data-urlencode "action=update" --data-urlencode "id=10" --data-urlencode "plotId=1" --data-urlencode "description=Updated violation" --data-urlencode "severity=80" --data-urlencode "detectedDate=2026-11-19" --data-urlencode "status=ПЕРЕВІРЕНО"
DELETE — удалить участок
curl -X POST "http://localhost:8080/server/api/violations" -d "action=delete&id=10"

ObservationServlet

GET — получить всі участки
curl http://localhost:8080/server/api/observations
POST — створити участок
curl -X POST "http://localhost:8080/server/api/observations" --data-urlencode "action=create" --data-urlencode "plotId=1" --data-urlencode "observationDate=2026-11-19" --data-urlencode "areaAffected=120.5" --data-urlencode "comment=Test observation"
UPDATE — поміняти участок
curl -X POST "http://localhost:8080/server/api/observations" --data-urlencode "action=update" --data-urlencode "id=10" --data-urlencode "plotId=1" --data-urlencode "observationDate=2026-11-19" --data-urlencode "areaAffected=150.5" --data-urlencode "comment=Updated observation"
DELETE — удалить участок
curl -X POST "http://localhost:8080/server/api/observations" -d "action=delete&id=10"

InspectionTaskServlet

GET — получить всі участки
curl http://localhost:8080/server/api/tasks
POST — створити участо
curl -X POST "http://localhost:8080/server/api/tasks" --data-urlencode "action=create" --data-urlencode "violationId=1" --data-urlencode "plannedDate=2026-11-25" --data-urlencode "taskStatus=ЗАПЛАНОВАНО"
UPDATE — поміняти участок
curl -X POST "http://localhost:8080/server/api/tasks" --data-urlencode "action=update" --data-urlencode "id=10" --data-urlencode "violationId=1" --data-urlencode "plannedDate=2026-11-25" --data-urlencode "completionDate=2026-11-27" --data-urlencode "taskStatus=ВИКОНАНО"
DELETE — удалить участок
curl -X POST "http://localhost:8080/server/api/tasks" -d "action=delete&id=10"

IndicatorServlet (Powershell)


Invoke-RestMethod -Uri "http://localhost:8080/server/api/indicator?plotId=1&violationId=1&observationId=1" -Method Get



