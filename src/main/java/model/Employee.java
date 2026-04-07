package model;

import cfg.Config;
import shiftPoolGenerator.ShiftCombination;
import shiftPoolGenerator.ShiftPool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Employee {
    private static final Map<String, Map<Integer, Integer>> sectionSaturdayUsage = new HashMap<>();

    private final String name;
    private final String surname;
    private final String section;
    private final int totalHours;
    private final int totalDays;
    private final List<ShiftCombination> shiftPool;
    private final List<Integer> daysOff;

    public Employee(String name, String surname, String section, int totalHours, int totalDays, List<Integer> daysOff)
            throws IllegalArgumentException {
        this.name = name;
        this.surname = surname;
        this.section = section;
        this.totalHours = totalHours;
        this.totalDays = totalDays;
        this.shiftPool = ShiftPool.getInstance().generateAll(totalHours, totalDays);
        this.daysOff = (List<Integer>) grantFreeWeekend(daysOff);
        validateTotalHours(Config.getInstance().getShiftLengths());
        validateTotalDays(totalDays);
    }

    private List<Integer> grantFreeWeekend(List<Integer> daysOff) {
        List<Integer> result = new java.util.ArrayList<>(daysOff);
        if (Config.getInstance().isGrantFreeWeekend()) {
            int firstDayIndex = Config.getInstance().getFirstDayOfWeek().getValue() % 7;
            Map<Integer, Integer> usage = sectionSaturdayUsage.computeIfAbsent(section, k -> new HashMap<>());
            List<Integer> saturdays = new java.util.ArrayList<>();
            for (int i = 0; i < Config.getInstance().getDaysInMonth(); i++) {
                int dayOfWeek = (firstDayIndex + i) % 7;
                if (dayOfWeek == 6) {
                    saturdays.add(i);
                    usage.putIfAbsent(i, 0);
                }
            }
            if (!saturdays.isEmpty()) {
                int minUsage = saturdays.stream().mapToInt(usage::get).min().orElse(0);
                List<Integer> leastUsed = saturdays.stream()
                        .filter(s -> usage.get(s) == minUsage && !result.contains(s))
                        .collect(java.util.stream.Collectors.toList());
                if (leastUsed.isEmpty()) {
                    leastUsed = saturdays.stream()
                            .filter(s -> usage.get(s) == minUsage)
                            .collect(java.util.stream.Collectors.toList());
                }
                int picked = leastUsed.get(new java.util.Random().nextInt(leastUsed.size()));
                if (!result.contains(picked)) {
                    result.add(picked);
                }
                usage.merge(picked, 1, Integer::sum);
            }
        }
        return result;
    }

    private void validateTotalHours(List<Integer> validShifts) throws IllegalArgumentException {
        int minHours = validShifts.getFirst() * totalDays;
        int maxHours = validShifts.getLast() * totalDays;

        if (totalHours < minHours || totalHours > maxHours) {
            throw new IllegalArgumentException(String.format(
                    "Nie można przydzielić %d godzin w %d dniach. Minimalna liczba godzin: %d, maksymalna liczba godzin: %d.",
                    totalHours, totalDays, minHours, maxHours));
        }
    }

    private void validateTotalDays(int totalDays) throws IllegalArgumentException {
        if (totalDays <= 0 || (totalDays - daysOff.size() - Config.getInstance().getClosedDaysSize() < 0)) {
            throw new IllegalArgumentException("Niepoprawna wartość ilości dni pracy.");
        }
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getSection() {
        return section;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public int getTotalDays() {
        return totalDays;
    }

    public List<ShiftCombination> getShiftPool() {
        return shiftPool;
    }

    public List<Integer> getDaysOff() {
        return daysOff;
    }
}
