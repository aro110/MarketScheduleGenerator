package appFlow;

import model.Employee;
import model.Section;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;


public class AppSetup {
    private final InputReader reader = new InputReader();

    public record AppData(YearMonth yearMonth, List<Section> sections) {}

    public AppData setup() {
        YearMonth yearMonth = setupYearMonth();
        List<Section> sections = setupSections();
        return new AppData(yearMonth, sections);
    }

    public YearMonth setupYearMonth() {
        return reader.readYearMonth();
    }

    public List<Section> setupSections() {
        List<Section> sections = new ArrayList<>();
        int sectionCount = reader.readInt("Podaj liczbę działów: ");

        for (int i = 0; i < sectionCount; i++) {
            String sectionName = reader.readString("Nazwa działu: ");
            int empCount = reader.readInt("Liczba pracowników w " + sectionName + ": ");

            List<Employee> employees = new ArrayList<>();
            for (int j = 0; j < empCount; j++) {
                String name = reader.readString("  Imię: ");
                String surname = reader.readString("  Nazwisko: ");
                int hours = reader.readInt("  Godziny: ");
                int days = reader.readInt("  Dni: ");

                employees.add(new Employee(name, surname, sectionName, hours, days));
            }

            sections.add(new Section(sectionName, employees));
        }
        return sections;
    }
}