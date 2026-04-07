package scheduleGenerator;

import model.Employee;
import model.Section;
import cfg.Config;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class ShiftAssigner {

    private final Config cfg = Config.getInstance();
    private int[] coverage;

    public Map<Employee, Integer> assignStartTimes(Schedule schedule, Section section, int dayIndex) {
        Map<Employee, Integer> workingEmployees = new HashMap<>();
        for (int i = 0; i < section.getEmployees().size(); i++) {
            if (schedule.getGenes()[i][dayIndex] != 0) {
                Employee emp = section.getEmployees().get(i);
                workingEmployees.put(emp, schedule.getGenes()[i][dayIndex]); // Domyślna godzina startu
            }
        }

        List<Map.Entry<Employee, Integer>> sortedEmployees = new ArrayList<>(workingEmployees.entrySet());
        sortedEmployees.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

        this.coverage = new int[cfg.getCloseHourInt(cfg.getYearMonth().atDay(dayIndex + 1).getDayOfWeek())
                - cfg.getOpenHourInt(cfg.getYearMonth().atDay(dayIndex + 1).getDayOfWeek())];
        for (Map.Entry<Employee, Integer> entry : sortedEmployees) {
            Employee emp = entry.getKey();
            int bestStart = findBestStart((int) entry.getValue(),
                    cfg.getOpenHourInt(cfg.getYearMonth().atDay(dayIndex + 1).getDayOfWeek()),
                    cfg.getCloseHourInt(cfg.getYearMonth().atDay(dayIndex + 1).getDayOfWeek()),
                    cfg.getPeakHoursArray(cfg.getYearMonth().atDay(dayIndex + 1).getDayOfWeek()));
            workingEmployees.put(emp, bestStart);
        }

        return workingEmployees;
    }

    private int findBestStart(int shiftLength, int openHour, int closeHour, int[] peakHours) {
        int bestStart = openHour;
        int bestScore = Integer.MIN_VALUE;
        int totalSlots = closeHour - openHour;

        for (int start = 0; start <= totalSlots - shiftLength; start++) {
            int score = 0;

            for (int j = start; j < start + shiftLength; j++) {
                if (peakHours[j] == 1) {
                    score += 10;
                }
            }

            for (int j = start; j < start + shiftLength; j++) {
                if (coverage[j] == 0) {
                    score += 5;
                }
            }

            for (int j = start; j < start + shiftLength; j++) {
                if (coverage[j] == 1) {
                    score += 2;
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestStart = openHour + start;
            }
        }

        int startIndex = bestStart - openHour;
        for (int j = startIndex; j < startIndex + shiftLength; j++) {
            coverage[j]++;
        }

        return bestStart;
    }
}
