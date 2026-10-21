DROP DATABASE IF EXISTS land_violations;

CREATE DATABASE land_violations;

USE land_violations;

-- 1. Земельні ділянки
CREATE TABLE land_plot (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           address VARCHAR(255) NOT NULL,
                           area DECIMAL(10,2) NOT NULL,
                           ecological_score INT NOT NULL
);

-- 2. Порушення
CREATE TABLE violation (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           plot_id BIGINT NOT NULL,
                           description TEXT NOT NULL,
                           severity INT NOT NULL,
                           detected_date DATE NOT NULL,
                           status VARCHAR(30) NOT NULL,
                           FOREIGN KEY (plot_id) REFERENCES land_plot(id)
);

-- 3. Спостереження
CREATE TABLE observation (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             plot_id BIGINT NOT NULL,
                             observation_date DATE NOT NULL,
                             area_affected DECIMAL(10,2) NOT NULL,
                             comment TEXT,
                             FOREIGN KEY (plot_id) REFERENCES land_plot(id)
);

-- 4. Інспекційні завдання
CREATE TABLE inspection_task (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 violation_id BIGINT NOT NULL,
                                 planned_date DATE NOT NULL,
                                 completion_date DATE,
                                 task_status VARCHAR(30) NOT NULL,
                                 FOREIGN KEY (violation_id) REFERENCES violation(id)
);

-- Земельні ділянки
INSERT INTO land_plot (address, area, ecological_score) VALUES
                                                            ('м. Полтава, вул. Європейська, 10', 1200.00, 65),
                                                            ('Полтавська область, с. Розсошенці', 2500.00, 80),
                                                            ('м. Полтава, вул. Соборності, 25', 850.00, 45);

-- Порушення
INSERT INTO violation
(plot_id, description, severity, detected_date, status) VALUES
                                                            (1, 'Несанкціоноване розміщення відходів', 70, '2026-10-10', 'ВИЯВЛЕНО'),
                                                            (2, 'Пошкодження ґрунтового покриву', 85, '2026-10-12', 'ПЕРЕВІРЕНО'),
                                                            (3, 'Засмічення території земельної ділянки', 40, '2026-10-15', 'ЗАХІД_ЗАПЛАНОВАНО');

-- Спостереження
INSERT INTO observation
(plot_id, observation_date, area_affected, comment) VALUES
                                                        (1, '2026-10-10', 120.50, 'Виявлено ділянку зі сміттям'),
                                                        (2, '2026-10-12', 300.00, 'Зафіксовано пошкодження ґрунту'),
                                                        (3, '2026-10-15', 45.75, 'Виявлено локальне засмічення');

-- Інспекційні завдання
INSERT INTO inspection_task
(violation_id, planned_date, completion_date, task_status) VALUES
                                                               (1, '2026-10-23', NULL, 'ЗАПЛАНОВАНО'),
                                                               (2, '2026-10-20', NULL, 'У_РОБОТІ'),
                                                               (3, '2026-10-25', NULL, 'ЗАПЛАНОВАНО');