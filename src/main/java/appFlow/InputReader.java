package appFlow;

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
}
