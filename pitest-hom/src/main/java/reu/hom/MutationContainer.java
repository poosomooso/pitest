package reu.hom;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.List;

public class MutationContainer implements Comparable<MutationContainer>{
    private HigherOrderMutation mutation;
    private double              fitness;

    public MutationContainer(HigherOrderMutation hom) {
        this.mutation = hom;
        this.fitness = mutationFitness(hom);
    }

    public HigherOrderMutation getMutation() {
        return mutation;
    }

    public MutationContainer randomlyMutate(List<MutationDetails> allFOMs) {
        if (Math.random() < 0.5) { //delete fom
            int deletedIndex = Utils.randRange(0, mutation.getOrder());
            return deleteFOM(deletedIndex);

        } else { //add fom
            int newFOMIndex = Utils.randRange(0, allFOMs.size());
            return addFOM(newFOMIndex, allFOMs);
        }
    }

    public MutationContainer[] crossover(MutationContainer o) {
        int randIndex1 = Utils.randRange(0, mutation.getOrder());
        int randIndex2 = Utils.randRange(0, o.mutation.getOrder());

        HigherOrderMutation child1 = new HigherOrderMutation();
        HigherOrderMutation child2 = new HigherOrderMutation();

        for (int i = 0; i < mutation.getOrder(); i++) {
            if (i == randIndex1) {
                child1.addMutation(o.mutation.getMutant(randIndex2));
            } else {
                child1.addMutation(mutation.getMutant(i));
            }
        }

        for (int i = 0; i < o.mutation.getOrder(); i++) {
            if (i == randIndex2) {
                child1.addMutation(mutation.getMutant(randIndex1));
            } else {
                child1.addMutation(o.mutation.getMutant(i));
            }
        }
        return new MutationContainer[]{new MutationContainer(child1), new MutationContainer(child2)};
    }

    /**
     * This is the reverse of the natural ordering for doubles, such that stronger
     * fitness values will be considered larger than wekaer fitness values.
     * Fitness values are stronger the closer to 0 it gets; however, 0 is the
     * weakest fitness value.
     * @param o
     * @return
     */
    @Override
    public int compareTo(MutationContainer o) {
        if (fitness < 0 || o.fitness < 0) {
            throw new IllegalStateException("Fitness values cannot be less than 0");
        }
        double epsilon = 1e-10;
        if (Math.abs(fitness - o.fitness) < epsilon) {
            return 0;
        }
        else if (Math.abs(fitness) < epsilon) {
            return -1;
        } else if (Math.abs(o.fitness) < epsilon) {
            return 1;
        }
        return -(Double.compare(fitness, o.fitness));
    }

    @Override
    public String toString() {
        return "MutationContainer [fitness=" + fitness + ", hom=" + mutation + "]";
    }

    protected MutationContainer deleteFOM(int deletedIndex) {
        HigherOrderMutation newHom = new HigherOrderMutation();

        for (int i = 0; i < mutation.getOrder(); i++) {
            if (i != deletedIndex) {
                newHom.addMutation(mutation.getMutant(i));
            }
        }

        return new MutationContainer(newHom);
    }

    protected MutationContainer addFOM(int fomToAdd, List<MutationDetails> allFOMs) {
        HigherOrderMutation newHom = mutation.clone();
        newHom.addMutation(allFOMs.get(fomToAdd));
        return new MutationContainer(newHom);
    }

    public static double mutationFitness(HigherOrderMutation hom) {
        //Set allKilledTests = null;
        //for m in hom
        // killed = m.getKilledTests()
        // for allKilledTests == null
        //  allKilledTests = killed
        // else
        //  allKilledTests = allKilledTests.intersect(killed)

        //fragilityFOM = allKilledTests.size() / numTests
        //fragilityHOM = hom.getKilledTests() / numTests
        //return fragilityHOM / fragilityFOM;
        return 0.0;
    }
}
