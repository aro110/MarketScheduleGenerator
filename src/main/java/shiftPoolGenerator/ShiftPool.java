package shiftPoolGenerator;

import cfg.Config;

import java.util.ArrayList;
import java.util.List;

public class ShiftPool {
    private final List<Integer> shifts;

    public ShiftPool() {
        this.shifts = Config.getInstance().getShiftLengths();
    }

    public List<ShiftCombination> generateAll(int totalHours, int days) {
        List<List<Integer>> results = new ArrayList<>();
        backtrack(totalHours, days, 0, new ArrayList<>(), results);
        List<ShiftCombination> shiftCombinations = new ArrayList<>();
        for (List<Integer> combination : results) shiftCombinations.add(new ShiftCombination(combination));
        ShiftClusterer clusterer = new ShiftClusterer(shiftCombinations);
        return clusterer.getRepresentatives(4); // zmien potem na wartosc z configu
    }

    private void backtrack(int remaining, int slotsLeft, int startIndex,
                           List<Integer> current, List<List<Integer>> results) {
        if (slotsLeft == 0) {
            if (remaining == 0) {
                results.add(new ArrayList<>(current));
            }
            return;
        }

        for (int i = startIndex; i < shifts.size(); i++) {
            int shift = shifts.get(i);

            int maxPossible = shifts.getLast() * slotsLeft;
            if (remaining > maxPossible) break;

            if (shift * slotsLeft > remaining) break;

            if (shift > remaining) break;

            current.add(shift);
            backtrack(remaining - shift, slotsLeft - 1, i, current, results);
            current.removeLast();
        }
    }
}
