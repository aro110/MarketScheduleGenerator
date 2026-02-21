package shiftPoolGenerator;

import java.util.List;

public class ShiftCombination {

    private final List<Integer> shifts;
    private final double stdDev;

    public ShiftCombination(List<Integer> shifts) {
        double mean = shifts.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = shifts.stream().mapToDouble(i -> Math.pow(i - mean, 2)).average().orElse(0);
        this.stdDev = Math.sqrt(variance);
        this.shifts = shifts;
    }

    public List<Integer> getShifts() {
        return shifts;
    }

    public double getStdDev() {
        return stdDev;
    }
}
