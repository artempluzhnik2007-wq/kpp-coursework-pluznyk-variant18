
package ua.edu.nupp.kpp.server.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ViolationLifecycleTest {

    // Тест 1. Виявлено -> Перевірено
    @Test
    void detectedToVerified() {

        assertTrue(
                ViolationLifecycle.isValidTransition(
                        "ВИЯВЛЕНО",
                        "ПЕРЕВІРЕНО"
                )
        );
    }

    // Тест 2. Перевірено -> Захід заплановано
    @Test
    void verifiedToPlanned() {

        assertTrue(
                ViolationLifecycle.isValidTransition(
                        "ПЕРЕВІРЕНО",
                        "ЗАХІД_ЗАПЛАНОВАНО"
                )
        );
    }

    // Тест 3. Захід заплановано -> Закрито
    @Test
    void plannedToClosed() {

        assertTrue(
                ViolationLifecycle.isValidTransition(
                        "ЗАХІД_ЗАПЛАНОВАНО",
                        "ЗАКРИТО"
                )
        );
    }

    // Тест 4. Заборона пропуску стану
    @Test
    void cannotSkipStatus() {

        assertFalse(
                ViolationLifecycle.isValidTransition(
                        "ВИЯВЛЕНО",
                        "ЗАХІД_ЗАПЛАНОВАНО"
                )
        );
    }

    // Тест 5. Заборона повернення назад
    @Test
    void cannotGoBackward() {

        assertFalse(
                ViolationLifecycle.isValidTransition(
                        "ПЕРЕВІРЕНО",
                        "ВИЯВЛЕНО"
                )
        );
    }

    // Тест 6. Закритий стан є кінцевим
    @Test
    void closedIsFinal() {

        assertFalse(
                ViolationLifecycle.isValidTransition(
                        "ЗАКРИТО",
                        "ВИЯВЛЕНО"
                )
        );
    }

    // Тест 7. Невідомий статус
    @Test
    void unknownStatusRejected() {

        assertFalse(
                ViolationLifecycle.isValidTransition(
                        "НЕВІДОМО",
                        "ПЕРЕВІРЕНО"
                )
        );
    }

    // Тест 8. Null-статус
    @Test
    void nullStatusRejected() {

        assertFalse(
                ViolationLifecycle.isValidTransition(
                        null,
                        "ПЕРЕВІРЕНО"
                )
        );
    }

    // Тест 9. Редагування без зміни статусу
    @Test
    void sameStatusAllowedForUpdate() {

        assertTrue(
                ViolationLifecycle.canUpdateStatus(
                        "ВИЯВЛЕНО",
                        "ВИЯВЛЕНО"
                )
        );
    }

    // Тест 10. Заборона недопустимого оновлення
    @Test
    void invalidUpdateRejected() {

        assertFalse(
                ViolationLifecycle.canUpdateStatus(
                        "ВИЯВЛЕНО",
                        "ЗАКРИТО"
                )
        );
    }
}
