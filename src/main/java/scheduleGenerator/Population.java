package scheduleGenerator;

import model.Section;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Population {
    private final Section section;
    private List<Schedule> scheduleList;
    private final YearMonth yearMonth;
    private final int size;
    private final Random random;

    public Population(Section section, YearMonth yearMonth, int size) {
        this.section = section;
        this.yearMonth = yearMonth;
        this.size = size;
        this.scheduleList = new ArrayList<>();
        for (int i=0; i<size; i++) {
            scheduleList.add(new Schedule(section, yearMonth));
        }
        this.random = new Random();
    }

    public Schedule run(int generations, int eliteCount, int tournamentSize) {
        for (int i=0; i<generations; i++) {
            evolve(eliteCount, tournamentSize);
            scheduleList.sort(Comparator.comparingDouble(Schedule::getFitness));
            Schedule best = scheduleList.getFirst();

            if (best.getFitness() == 0) {
                return best;
            }
        }
        return scheduleList.getFirst();
    }

    private Schedule tournamentSelection(int tournamentSize) {
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
        scheduleList.sort(Comparator.comparingDouble(Schedule::getFitness));

        for (int i=0; i<eliteCount; i++) {
            evolvedSchedules.add(scheduleList.get(i));
        }

        while (evolvedSchedules.size() < size) {
            Schedule parent1 = tournamentSelection(tournamentSize);
            Schedule parent2 = tournamentSelection(tournamentSize);
            Schedule child = crossover(parent1, parent2);
            mutate(child, 0.05);
            evolvedSchedules.add(child);
        }

        this.scheduleList = evolvedSchedules;
    }

    private Schedule crossover(Schedule parent1, Schedule parent2) {
        int[][] childGenes = new int[parent1.getGenes().length][];
        for (int i = 0; i < parent1.getGenes().length; i++) {
            if (random.nextBoolean()) {
                childGenes[i] = parent1.getGenes()[i].clone();
            } else {
                childGenes[i] = parent2.getGenes()[i].clone();
            }
        }
        return new Schedule(section, yearMonth, childGenes);
    }

    private Schedule mutate(Schedule schedule, double mutationRate) {
        int[][] genes = schedule.getGenes();
        for (int i = 0; i < genes.length; i++) {
            if (random.nextDouble() < mutationRate) {
                int day1 = random.nextInt(genes[i].length);
                while (genes[i][day1] != 0) {
                    day1 = random.nextInt(genes[i].length);
                }

                int day2 = random.nextInt(genes[i].length);
                while (genes[i][day2] == 0) {
                    day2 = random.nextInt(genes[i].length);
                }

                int temp = genes[i][day1];
                genes[i][day1] = genes[i][day2];
                genes[i][day2] = temp;
            }
        }
        schedule.calculateFitness();
        return schedule;
    }

    public List<Schedule> getScheduleList() {
        return scheduleList;
    }
}
