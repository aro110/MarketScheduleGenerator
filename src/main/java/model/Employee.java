package model;

import cfg.Config;
import shiftPoolGenerator.ShiftCombination;
import shiftPoolGenerator.ShiftPool;

import java.util.List;

public class Employee {
    private final String name;
    private final String surname;
    private final String section;
    private final int totalHours;
    private final int totalDays;
    private final List<ShiftCombination> shiftPool;
    private final List<Integer> daysOff;


    public Employee(String name, String surname, String section, int totalHours, int totalDays, List<Integer> daysOff) throws IllegalArgumentException {
        this.name = name;
        this.surname = surname;
        this.section = section;
        this.totalHours = totalHours;
        this.totalDays = totalDays;
        this.shiftPool = ShiftPool.getInstance().generateAll(totalHours, totalDays);
        this.daysOff = daysOff;
        validateTotalHours(Config.getInstance().getShiftLengths());
        validateTotalDays(totalDays);
    }

    private void validateTotalHours(List<Integer> validShifts) throws IllegalArgumentException {
        int minHours = validShifts.getFirst() * totalDays;
        int maxHours = validShifts.getLast() * totalDays;

        if (totalHours < minHours || totalHours > maxHours) {
            throw new IllegalArgumentException(String.format(
                    "Nie można przydzielić %d godzin w %d dniach. Minimalna liczba godzin: %d, maksymalna liczba godzin: %d.",
                    totalHours, totalDays, minHours, maxHours
            ));
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
