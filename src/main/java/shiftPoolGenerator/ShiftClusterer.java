package shiftPoolGenerator;

import java.util.ArrayList;
import java.util.List;

public class ShiftClusterer {
    private List<ShiftCombination> low;
    private List<ShiftCombination> medium;
    private List<ShiftCombination> high;

    public ShiftClusterer(List<ShiftCombination> combinations) {
        this.low = combinations.stream().filter(shift -> shift.getStdDev() < 1.5).toList();
        this.medium = combinations.stream().filter(shift -> shift.getStdDev() >= 1.5 && shift.getStdDev() < 2.5).toList();
        this.high = combinations.stream().filter(shift -> shift.getStdDev() >= 2.5).toList();
    }

    public List<ShiftCombination> getRepresentatives(int maxPerCluster) {
        List<ShiftCombination> reps = new ArrayList<>();
        pickRepresentatives(low, maxPerCluster, reps);
        pickRepresentatives(medium, maxPerCluster, reps);
        pickRepresentatives(high, maxPerCluster, reps);
        return reps;
    }

    private void pickRepresentatives(List<ShiftCombination> cluster, int maxReps,
                                     List<ShiftCombination> target) {
        if (cluster.size() <= maxReps) {
            target.addAll(cluster);
            return;
        }
        for (int i = 0; i < maxReps; i++) {
            int index = (int) Math.round((double) i * (cluster.size() - 1) / (maxReps - 1));
            target.add(cluster.get(index));
        }
    }
}
