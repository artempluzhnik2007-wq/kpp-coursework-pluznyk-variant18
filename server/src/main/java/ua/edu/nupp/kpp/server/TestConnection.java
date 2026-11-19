
package ua.edu.nupp.kpp.server;

import ua.edu.nupp.kpp.server.service.IndicatorService;

public class TestConnection {

    public static void main(String[] args) {

        IndicatorService service = new IndicatorService();

        try {
            double result = service.calculateFromDatabase(
                    1,
                    1,
                    1
            );

            System.out.println(
                    "Інтегральний показник: " + result);

            System.out.println("ТЕСТ ПРОЙДЕНО!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
