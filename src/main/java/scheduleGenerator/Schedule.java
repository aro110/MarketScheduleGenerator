package scheduleGenerator;

import cfg.Config;
import model.Employee;
import model.Section;
import shiftPoolGenerator.ShiftCombination;

import java.time.DayOfWeek;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Random;

public class Schedule implements Chromosome {
    private final int[][] genes;
    private final int employees;
    private final int daysInMonth;
    private final DayOfWeek firstDay;
    private double fitness;
    private final Random random = new Random();

    private static final double PENALTY_NO_COVERAGE = 60;      // brak pokrycia godzin otwarcia
    private static final double PENALTY_CONSECUTIVE_DAYS = 30;  // za dużo dni pod rząd
    private static final double PENALTY_NO_PEAK = 10;            // brak pokrycia peak hours
    private static final double PENALTY_DAY_WEIGHT = 15;         // niedopasowanie do wag dni
    private static final double PENALTY_SAME_START = 10;          // >2 osoby o tej samej godzinie

    private final Config cfg = Config.getInstance();

    public Schedule(Section section, YearMonth yearMonth) {
        this.employees = section.getEmployees().size();
        this.daysInMonth = yearMonth.lengthOfMonth();
        this.firstDay = yearMonth.atDay(1).getDayOfWeek();
        this.genes = new int[employees][daysInMonth];

        for (int i = 0; i < employees; i++) {
            genes[i] = initRow(section.getEmployees().get(i));
        }

        this.fitness = calculateFitness();
    }

    public Schedule(Section section, YearMonth yearMonth, int[][] genes) {
        this.employees = section.getEmployees().size();
        this.daysInMonth = yearMonth.lengthOfMonth();
        this.firstDay = yearMonth.atDay(1).getDayOfWeek();
        this.genes = genes;
        this.fitness = calculateFitness();
    }

    private int[] initRow(Employee employee) {
        List<ShiftCombination> pool = employee.getShiftPool();
        ShiftCombination combo = pool.get(random.nextInt(pool.size()));

        int[] row = new int[daysInMonth];
        List<Integer> shifts = combo.getShifts();
        for (int i = 0; i < shifts.size(); i++) {
            row[i] = shifts.get(i);
        }

        shuffleArray(row);
        return row;
    }

    private void shuffleArray(int[] ar) {
        for (int i = ar.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }


    private double calculateFitness() {
        double fitness = 0;
        for (int day = 0; day < daysInMonth; day++) {
            fitness += checkDayCoverage(day);
            fitness += checkStaffingTarget(day);
        }

        for (int emp = 0; emp < employees; emp++) {
            fitness += checkConsecutiveDays(emp);
        }

        return fitness;
    }

    private double checkConsecutiveDays(int employeeIndex) {
        int consecutiveCount = 0;
        for (int day = 0; day < daysInMonth; day++) {
            if (genes[employeeIndex][day] > 0) {
                consecutiveCount++;
                if (consecutiveCount > cfg.getMaxWorkingDaysInARow()) {
                    return PENALTY_CONSECUTIVE_DAYS;
                }
            } else {
                consecutiveCount = 0;
            }
        }

        return 0;
    }

    private double checkDayCoverage(int dayIndex) {
        DayOfWeek day = firstDay.plus(dayIndex);
        Config.DayHours dayHours = cfg.getHours(day);

        int openHours = dayHours.close().getHour() - dayHours.open().getHour();

        int totalStaffHours = 0;
        for (int i = 0; i < employees; i++) {
            totalStaffHours += genes[i][dayIndex];
        }

        if (totalStaffHours < openHours) {
            return PENALTY_NO_COVERAGE;
        }
        return 0;
    }

    private double checkStaffingTarget(int dayIndex) {
        DayOfWeek day = firstDay.plus(dayIndex);
        double percent = cfg.getStaffingPercent(day) / 100.0;
        double target = Math.round(employees * percent);

        int working = 0;
        for (int i = 0; i < employees; i++) {
            if (genes[i][dayIndex] > 0) working++;
        }

        double diff = Math.abs(working - target);
        return diff * PENALTY_DAY_WEIGHT;
    }

    public double getFitness() {
        return fitness;
    }

    public int[][] getGenes() {
        return genes;
    }
}
