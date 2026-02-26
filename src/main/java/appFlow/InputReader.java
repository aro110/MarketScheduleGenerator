package appFlow;

import java.time.YearMonth;
import java.util.Scanner;

public class InputReader {
    Scanner scanner = new Scanner(System.in);

    public int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter an integer.");
            scanner.next();
            System.out.print(prompt);
        }
        return scanner.nextInt();
    }

    public String readString(String prompt) {
        System.out.print(prompt);
        return scanner.next();
    }

    public YearMonth readYearMonth() {
        int year = readInt("Rok: ");
        int month = readInt("Miesiąc (1-12): ");
        while (month < 1 || month > 12) {
            System.out.println("Invalid month. Please enter a value between 1 and 12.");
            month = readInt("Miesiąc (1-12): ");
        }
        return YearMonth.of(year, month);
    }
}
