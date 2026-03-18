package scheduleGenerator;

import model.Section;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Population {
    private final Section section;
    private final List<Schedule> scheduleList;
    private final YearMonth yearMonth;
    private final int size;

    public Population(Section section, YearMonth yearMonth, int size) {
        this.section = section;
        this.yearMonth = yearMonth;
        this.size = size;
        this.scheduleList = new ArrayList<>();
        for (int i=0; i<size; i++) {
            scheduleList.add(new Schedule(section, yearMonth));
        }
    }

    private Schedule tournamentSelection(int tournamentSize) {
        Random random = new Random();
        Schedule bestSchedule = null;
        for (int i=0; i<tournamentSize; i++) {
            Schedule candidate = scheduleList.get(random.nextInt(size));
            if (bestSchedule == null || candidate.getFitness() < bestSchedule.getFitness()) {
                bestSchedule = candidate;
            }
        }
        return bestSchedule;
    }

    private void evolve(int eliteCount, int tournamentSize) {
        List<Schedule> evolvedSchedules = new ArrayList<>();
        scheduleList.sort((s1, s2) -> Double.compare(s1.getFitness(), s2.getFitness()));

        for (int i=0; i<eliteCount; i++) {
            evolvedSchedules.add(scheduleList.get(i));
        }

        while (evolvedSchedules.size() < size) {
            Schedule parent1 = tournamentSelection(tournamentSize);
            Schedule parent2 = tournamentSelection(tournamentSize);
            Schedule child = crossover(parent1, parent2);
            evolvedSchedules.add(child);
        }
    }

    private Schedule crossover(Schedule parent1, Schedule parent2) {
        int[][] childGenes = new int[parent1.getGenes().length][];
        Random random = new Random();
        for (int i = 0; i < parent1.getGenes().length; i++) {
            if (random.nextBoolean()) {
                childGenes[i] = parent1.getGenes()[i].clone();
            } else {
                childGenes[i] = parent2.getGenes()[i].clone();
            }
        }
        return new Schedule(section, yearMonth, childGenes);
    }

    // dodaj mutacje
}
