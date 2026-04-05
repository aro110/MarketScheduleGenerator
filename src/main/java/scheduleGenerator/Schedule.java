package scheduleGenerator;

import cfg.Config;
import model.Employee;
import model.Section;
import shiftPoolGenerator.ShiftCombination;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class Schedule implements Chromosome {
    private final Config cfg = Config.getInstance();
    private final int[][] genes;
    private final int employees;
    private final int daysInMonth = cfg.getDaysInMonth();
    private final DayOfWeek firstDay = cfg.getFirstDayOfWeek();
    private double fitness;
    private final Random random = new Random();

    private static final double PENALTY_DAY_OFF = 1000;     // pracownik ma dzień wolny, a jest zaplanowany
    private static final double PENALTY_NO_COVERAGE = 60;      // brak pokrycia godzin otwarcia
    private static final double PENALTY_CONSECUTIVE_DAYS = 30;  // za dużo dni pod rząd
    //private static final double PENALTY_NO_PEAK = 10;            // brak pokrycia peak hours
    private static final double PENALTY_DAY_WEIGHT = 15;         // niedopasowanie do wag dni
    private static final double PENALTY_FREE_DISTRIBUTION = 25; // nierownomiernie rozlozony grafik
    //private static final double PENALTY_SAME_START = 10;          // >2 osoby o tej samej godzinie
    private static final double PENALTY_FREE_WEEKENDS = 20;          // brak 1 wolnego weekendu

    public Schedule(Section section) {
        this.employees = section.getEmployees().size();
        this.genes = new int[employees][daysInMonth];

        List<Integer> closedDays = getClosedDayIndices();
        for (int i = 0; i < employees; i++) {
            genes[i] = initRow(section.getEmployees().get(i), closedDays);
        }

        calculateFitness();
    }

    public Schedule(Section section, int[][] genes) {
        this.employees = section.getEmployees().size();
        this.genes = genes;
        calculateFitness();
    }

    private int[] initRow(Employee employee, List<Integer> closedDays) {
        List<ShiftCombination> pool = employee.getShiftPool();
        ShiftCombination combo = pool.get(random.nextInt(pool.size()));

        int[] row = new int[daysInMonth];
        List<Integer> shifts = combo.getShifts();
        for (int i = 0; i < shifts.size(); i++) {
            row[i] = shifts.get(i);
        }

        shuffleArray(row);

        Set<Integer> allOff = new HashSet<>(closedDays);
        allOff.addAll(employee.getDaysOff());

        for (int offDay : allOff) {
            if (row[offDay] != 0) {
                for (int j = 0; j < row.length; j++) {
                    if (row[j] == 0 && !allOff.contains(j)) {
                        row[j] = row[offDay];
                        row[offDay] = 0;
                        break;
                    }
                }
            }
        }
        return row;
    }

    private List<Integer> getClosedDayIndices() {
        List<Integer> closedDays = new ArrayList<>();
        for (int i = 0; i < daysInMonth; i++) {
            LocalDate date = cfg.getYearMonth().atDay(i + 1);
            if (cfg.isClosedDay(date)) {
                closedDays.add(i);
            }
        }
        return closedDays;
    }

    private void shuffleArray(int[] ar) {
        for (int i = ar.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public double calculateFitness() {
        double total = 0;
        for (int day = 0; day < daysInMonth; day++) {
            total += checkDayCoverage(day);
            total += checkStaffingTarget(day);
            total += checkClosedDays(day);
        }

        for (int emp = 0; emp < employees; emp++) {
            total += checkConsecutiveDays(emp);
            total += checkWorkDistribution(emp);
            total += checkFreeWeekends(emp);
        }

        this.fitness = total;
        return total;
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
        LocalDate date = cfg.getYearMonth().atDay(dayIndex + 1);
        if (cfg.isClosedDay(date)) return 0;

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
        LocalDate date = cfg.getYearMonth().atDay(dayIndex + 1);
        if (cfg.isClosedDay(date)) return 0;

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

    private double checkClosedDays(int dayIndex) {
        LocalDate date = cfg.getYearMonth().atDay(dayIndex + 1);
        if (!cfg.isClosedDay(date)) return 0;

        double penalty = 0;
        for (int i = 0; i < employees; i++) {
            if (genes[i][dayIndex] > 0) {
                penalty += PENALTY_DAY_OFF;
            }
        }
        return penalty;
    }

    private double checkWorkDistribution(int employeeIndex) {
        int quarterLength = daysInMonth / 4;
        int[] workPerQuarter = new int[4];

        for (int day = 0; day < daysInMonth; day++) {
            if (genes[employeeIndex][day] != 0) {
                int quarter = Math.min(day / quarterLength, 3);
                workPerQuarter[quarter]++;
            }
        }

        double penalty = 0;
        for (int i = 0; i < 3; i++) {
            double diff = Math.abs(workPerQuarter[i] - workPerQuarter[i + 1]);
            penalty += diff * PENALTY_FREE_DISTRIBUTION;
        }
        return penalty;
    }

    private double checkFreeWeekends(int employeeIndex) {
        int firstSaturday = (DayOfWeek.SATURDAY.getValue() - firstDay.getValue() + 7) % 7;
        for (int day = firstSaturday; day < daysInMonth - 1; day += 7) {
            boolean satFree = genes[employeeIndex][day] == 0;
            boolean sunFree = genes[employeeIndex][day + 1] == 0;
            boolean sunClosed = cfg.isClosedDay(cfg.getYearMonth().atDay(day + 2));

            if (satFree && sunFree && !sunClosed) {
                return 0;
            }
        }
        return PENALTY_FREE_WEEKENDS;
    }

    public double getFitness() { return fitness; }
    public int[][] getGenes() { return genes; }
    public int getDaysInMonth() { return daysInMonth; }
    public int getEmployees() { return employees; }
}
