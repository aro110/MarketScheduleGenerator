import cfg.Config;
import model.Employee;
import model.Section;
import scheduleGenerator.Population;
import scheduleGenerator.Schedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        // ==================== Config ====================

        Map<DayOfWeek, Config.DayHours> hours = new EnumMap<>(DayOfWeek.class);
        hours.put(DayOfWeek.MONDAY, new Config.DayHours(LocalTime.of(8, 0), LocalTime.of(21, 0)));
        hours.put(DayOfWeek.TUESDAY, new Config.DayHours(LocalTime.of(9, 0), LocalTime.of(21, 0)));
        hours.put(DayOfWeek.WEDNESDAY, new Config.DayHours(LocalTime.of(8, 0), LocalTime.of(21, 0)));
        hours.put(DayOfWeek.THURSDAY, new Config.DayHours(LocalTime.of(9, 0), LocalTime.of(21, 0)));
        hours.put(DayOfWeek.FRIDAY, new Config.DayHours(LocalTime.of(8, 0), LocalTime.of(21, 0)));
        hours.put(DayOfWeek.SATURDAY, new Config.DayHours(LocalTime.of(9, 0), LocalTime.of(21, 0)));
        hours.put(DayOfWeek.SUNDAY, new Config.DayHours(LocalTime.of(10, 0), LocalTime.of(18, 0)));

        // Staffing: parabola — Pon 75%, Wt 60%, Śr 50%, Czw 60%, Pt 75%, Sob 95%, Nd
        // 50%
        Map<DayOfWeek, Integer> staffing = new EnumMap<>(DayOfWeek.class);
        staffing.put(DayOfWeek.MONDAY, 90);
        staffing.put(DayOfWeek.TUESDAY, 80);
        staffing.put(DayOfWeek.WEDNESDAY, 70);
        staffing.put(DayOfWeek.THURSDAY, 80);
        staffing.put(DayOfWeek.FRIDAY, 90);
        staffing.put(DayOfWeek.SATURDAY, 95);
        staffing.put(DayOfWeek.SUNDAY, 50);

        Map<DayOfWeek, LocalTime> peakHours = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            peakHours.put(day, LocalTime.of(12, 0));
        }

        YearMonth yearMonth = YearMonth.of(2026, 4); // kwiecień 2026

        // ==================== Inicjalizacja Config (przed pracownikami!)
        // ====================

        Config.initForTest(
                List.of(6, 7, 8, 9, 10, 11, 12), 5,
                hours, staffing,
                yearMonth, List.of());

        // ==================== Sekcje i pracownicy ====================

        // Sekcja 1: Elektronika — 6 osób
        List<Employee> elektronika = List.of(
                new Employee("Jan", "Kowalski", "Elektronika", 168, 21, List.of()),
                new Employee("Anna", "Nowak", "Elektronika", 168, 21, List.of()),
                new Employee("Piotr", "Wiśniewski", "Elektronika", 168, 21, List.of()),
                new Employee("Kasia", "Wójcik", "Elektronika", 168, 20, List.of()),
                new Employee("Marek", "Zieliński", "Elektronika", 168, 21, List.of()),
                new Employee("Ola", "Kamińska", "Elektronika", 168, 21, List.of()));

        // Sekcja 2: AGD — 6 osób
        List<Employee> agd = List.of(
                new Employee("Tomek", "Lewandowski", "AGD", 168, 21, List.of()),
                new Employee("Magda", "Szymańska", "AGD", 168, 20, List.of()),
                new Employee("Bartek", "Woźniak", "AGD", 168, 21, List.of()),
                new Employee("Ewa", "Dąbrowska", "AGD", 168, 21, List.of()),
                new Employee("Kamil", "Kozłowski", "AGD", 168, 21, List.of()),
                new Employee("Natalia", "Jankowska", "AGD", 168, 21, List.of()));

        // Sekcja 3: Kasa — 5 osób
        List<Employee> kasa = List.of(
                new Employee("Michał", "Mazur", "Kasa", 168, 21, List.of()),
                new Employee("Agata", "Krawczyk", "Kasa", 168, 20, List.of()),
                new Employee("Dawid", "Piotrowicz", "Kasa", 168, 21, List.of()),
                new Employee("Zofia", "Grabowska", "Kasa", 168, 21, List.of()),
                new Employee("Filip", "Pawlak", "Kasa", 168, 21, List.of()));

        // Sekcja 4: Magazyn — 3 osoby
        List<Employee> magazyn = List.of(
                new Employee("Robert", "Michalski", "Magazyn", 168, 21, List.of()),
                new Employee("Sylwia", "Adamczyk", "Magazyn", 168, 20, List.of()),
                new Employee("Łukasz", "Zając", "Magazyn", 168, 21, List.of()));

        // Sekcja 5: Obsługa klienta — 2 osoby
        List<Employee> obsluga = List.of(
                new Employee("Monika", "Król", "Obsługa klienta", 168, 21, List.of()),
                new Employee("Paweł", "Wieczorek", "Obsługa klienta", 168, 21, List.of()));

        List<Section> sections = List.of(
                new Section("Elektronika", elektronika),
                new Section("AGD", agd),
                new Section("Kasa", kasa),
                new Section("Magazyn", magazyn),
                new Section("Obsługa klienta", obsluga));

        // ==================== Generowanie grafików ====================

        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           GENERATOR GRAFIKÓW — KWIECIEŃ 2026                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        int populationSize = 300;
        int generations = 1000;
        int eliteCount = 5;
        int tournamentSize = 3;
        int totalEmployees = sections.stream().mapToInt(s -> s.getEmployees().size()).sum();

        // Zbieramy najlepsze grafiki dla podsumowania
        List<Schedule> bestSchedules = new ArrayList<>();

        for (Section section : sections) {
            long start = System.currentTimeMillis();

            Population population = new Population(section, populationSize);
            Schedule best = population.run(generations, eliteCount, tournamentSize);

            long elapsed = System.currentTimeMillis() - start;

            bestSchedules.add(best);
            printSchedule(section, best, yearMonth, elapsed);
        }

        // ==================== Podsumowanie globalne ====================

        printGlobalSummary(sections, bestSchedules, yearMonth, totalEmployees, staffing);
    }

    private static void printSchedule(Section section, Schedule best, YearMonth yearMonth, long elapsed) {
        System.out.println("━".repeat(100));
        System.out.printf("  DZIAŁ: %s (%d pracowników)   |   Fitness: %.1f   |   Czas: %dms%n",
                section.getEmployees().get(0).getSection(),
                section.getEmployees().size(),
                best.getFitness(),
                elapsed);
        System.out.println("━".repeat(100));

        // Nagłówek — dni miesiąca
        System.out.printf("  %-14s", "");
        for (int d = 1; d <= best.getDaysInMonth(); d++) {
            System.out.printf("%3d", d);
        }
        System.out.println("   | Σh  Dni");

        // Nagłówek — dni tygodnia
        System.out.printf("  %-14s", "");
        DayOfWeek first = yearMonth.atDay(1).getDayOfWeek();
        String[] dayLabels = { "Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd" };
        for (int d = 0; d < best.getDaysInMonth(); d++) {
            DayOfWeek dow = first.plus(d);
            System.out.printf("%3s", dayLabels[dow.getValue() - 1]);
        }
        System.out.println("   |");

        System.out.println("  " + "─".repeat(96));

        // Wiersze pracowników
        List<Employee> emps = section.getEmployees();
        for (int i = 0; i < best.getEmployees(); i++) {
            String name = emps.get(i).getName() + " " + emps.get(i).getSurname().charAt(0) + ".";
            System.out.printf("  %-14s", name);

            int sumHours = 0;
            int workDays = 0;
            for (int j = 0; j < best.getDaysInMonth(); j++) {
                int val = best.getGenes()[i][j];
                if (val == 0) {
                    System.out.printf("%3s", "·");
                } else {
                    System.out.printf("%3d", val);
                    sumHours += val;
                    workDays++;
                }
            }
            System.out.printf("   |%3dh %2dd%n", sumHours, workDays);
        }

        // Podsumowanie dnia — sekcja
        System.out.println("  " + "─".repeat(96));
        System.out.printf("  %-14s", "Pracujących:");
        for (int j = 0; j < best.getDaysInMonth(); j++) {
            int working = 0;
            for (int i = 0; i < best.getEmployees(); i++) {
                if (best.getGenes()[i][j] > 0)
                    working++;
            }
            System.out.printf("%3d", working);
        }
        System.out.println();

        System.out.printf("  %-14s", "Suma godzin:");
        for (int j = 0; j < best.getDaysInMonth(); j++) {
            int totalH = 0;
            for (int i = 0; i < best.getEmployees(); i++) {
                totalH += best.getGenes()[i][j];
            }
            System.out.printf("%3d", totalH);
        }
        System.out.println();
        System.out.println();
    }

    private static void printGlobalSummary(List<Section> sections, List<Schedule> bestSchedules,
            YearMonth yearMonth, int totalEmployees,
            Map<DayOfWeek, Integer> staffing) {
        int daysInMonth = yearMonth.lengthOfMonth();
        DayOfWeek first = yearMonth.atDay(1).getDayOfWeek();
        Config cfg = Config.getInstance();

        System.out.println(
                "╔══════════════════════════════════════════════════════════════════════════════════════════════════╗");
        System.out.println(
                "║                              PODSUMOWANIE GLOBALNE SKLEPU                                       ║");
        System.out.println(
                "╚══════════════════════════════════════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("  Łącznie pracowników: " + totalEmployees);
        System.out.println();

        // Nagłówek
        System.out.printf("  %-18s", "");
        for (int d = 1; d <= daysInMonth; d++) {
            System.out.printf("%4d", d);
        }
        System.out.println();

        // Dni tygodnia
        System.out.printf("  %-18s", "");
        String[] dayLabels = { "Pn", "Wt", "Śr", "Cz", "Pt", "So", "Nd" };
        for (int d = 0; d < daysInMonth; d++) {
            DayOfWeek dow = first.plus(d);
            System.out.printf("%4s", dayLabels[dow.getValue() - 1]);
        }
        System.out.println();
        System.out.println("  " + "─".repeat(18 + daysInMonth * 4));

        // Pracujący per sekcja
        for (int s = 0; s < sections.size(); s++) {
            Schedule schedule = bestSchedules.get(s);
            String sectionName = sections.get(s).getEmployees().get(0).getSection();
            System.out.printf("  %-18s", sectionName);
            for (int j = 0; j < daysInMonth; j++) {
                int working = 0;
                for (int i = 0; i < schedule.getEmployees(); i++) {
                    if (schedule.getGenes()[i][j] > 0)
                        working++;
                }
                System.out.printf("%4d", working);
            }
            System.out.println();
        }

        System.out.println("  " + "─".repeat(18 + daysInMonth * 4));

        // Suma pracujących — cały sklep
        int[] globalWorking = new int[daysInMonth];
        int[] globalHours = new int[daysInMonth];
        for (Schedule schedule : bestSchedules) {
            for (int j = 0; j < daysInMonth; j++) {
                for (int i = 0; i < schedule.getEmployees(); i++) {
                    if (schedule.getGenes()[i][j] > 0)
                        globalWorking[j]++;
                    globalHours[j] += schedule.getGenes()[i][j];
                }
            }
        }

        System.out.printf("  %-18s", "ŁĄCZNIE OSÓB:");
        for (int j = 0; j < daysInMonth; j++) {
            System.out.printf("%4d", globalWorking[j]);
        }
        System.out.println();

        System.out.printf("  %-18s", "ŁĄCZNIE GODZIN:");
        for (int j = 0; j < daysInMonth; j++) {
            System.out.printf("%4d", globalHours[j]);
        }
        System.out.println();

        // Cel staffing
        System.out.printf("  %-18s", "CEL (staffing%):");
        for (int d = 0; d < daysInMonth; d++) {
            DayOfWeek dow = first.plus(d);
            int target = (int) Math.round(totalEmployees * staffing.get(dow) / 100.0);
            System.out.printf("%4d", target);
        }
        System.out.println();

        // Odchylenie
        System.out.printf("  %-18s", "ODCHYLENIE:");
        for (int d = 0; d < daysInMonth; d++) {
            DayOfWeek dow = first.plus(d);
            int target = (int) Math.round(totalEmployees * staffing.get(dow) / 100.0);
            int diff = globalWorking[d] - target;
            if (cfg.isClosedDay(yearMonth.atDay(d + 1))) {
                System.out.printf("%4s", "—");
            } else if (diff == 0) {
                System.out.printf("%4s", "✓");
            } else if (diff > 0) {
                System.out.printf("  +%d", diff);
            } else {
                System.out.printf("  %d", diff);
            }
        }
        System.out.println();

        // Statystyki per dzień tygodnia
        System.out.println();
        System.out.println("  STAFFING PER DZIEŃ TYGODNIA:");
        System.out.println("  " + "─".repeat(60));
        System.out.printf("  %-12s %6s %6s %6s %6s %6s%n", "Dzień", "Cel%", "Cel os.", "Śr.os.", "Min", "Max");
        System.out.println("  " + "─".repeat(60));

        for (DayOfWeek dow : DayOfWeek.values()) {
            int target = (int) Math.round(totalEmployees * staffing.get(dow) / 100.0);
            List<Integer> dailyWorking = new ArrayList<>();

            for (int d = 0; d < daysInMonth; d++) {
                if (first.plus(d) == dow && !cfg.isClosedDay(yearMonth.atDay(d + 1))) {
                    dailyWorking.add(globalWorking[d]);
                }
            }

            if (dailyWorking.isEmpty()) {
                System.out.printf("  %-12s %5d%% %6d %6s %6s %6s%n",
                        dayLabels[dow.getValue() - 1], staffing.get(dow), target, "—", "—", "—");
            } else {
                double avg = dailyWorking.stream().mapToInt(Integer::intValue).average().orElse(0);
                int min = dailyWorking.stream().mapToInt(Integer::intValue).min().orElse(0);
                int max = dailyWorking.stream().mapToInt(Integer::intValue).max().orElse(0);
                System.out.printf("  %-12s %5d%% %6d %6.1f %6d %6d%n",
                        dayLabels[dow.getValue() - 1], staffing.get(dow), target, avg, min, max);
            }
        }
        System.out.println();
    }
}
