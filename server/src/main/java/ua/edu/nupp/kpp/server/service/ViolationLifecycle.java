package ua.edu.nupp.kpp.server.service;

public final class ViolationLifecycle {

    private ViolationLifecycle() {
    }

    // Перевірка допустимості переходу між станами
    public static boolean isValidTransition(
            String oldStatus,
            String newStatus) {

        if (oldStatus == null || newStatus == null) {
            return false;
        }

        return switch (oldStatus) {

            case "ВИЯВЛЕНО" ->
                    "ПЕРЕВІРЕНО".equals(newStatus);

            case "ПЕРЕВІРЕНО" ->
                    "ЗАХІД_ЗАПЛАНОВАНО".equals(newStatus);

            case "ЗАХІД_ЗАПЛАНОВАНО" ->
                    "ЗАКРИТО".equals(newStatus);

            case "ЗАКРИТО" -> false;

            default -> false;
        };
    }

    // Перевірка, чи можна зберегти новий статус
    public static boolean canUpdateStatus(
            String oldStatus,
            String newStatus) {

        if (oldStatus == null || newStatus == null) {
            return false;
        }

        return oldStatus.equals(newStatus)
                || isValidTransition(oldStatus, newStatus);
    }
}
