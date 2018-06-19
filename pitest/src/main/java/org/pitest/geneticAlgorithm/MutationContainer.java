package org.pitest.geneticAlgorithm;

import org.pitest.mutationtest.engine.HigherOrderMutation;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.execute.AllTestDataListener;
import org.pitest.testapi.Description;
import org.pitest.util.Log;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MutationContainer implements Comparable<MutationContainer>{
    private final HigherOrderMutation mutation;
    private final double           fitness;
    private final Set<Description> killedTests;
    private final double           numTests;

    public MutationContainer(HigherOrderMutation hom,
        Function<HigherOrderMutation, AllTestDataListener> testRunner,
        Map<MutationDetails, MutationContainer> foms) {
        this.mutation = hom;
        AllTestDataListener listener = testRunner.apply(hom);
        this.killedTests = listener.getKilledTests();
        this.numTests =
            listener.getKilledTests().size() + listener.getSurvivedTests()
                .size();
        this.fitness = mutationFitness(foms);
    }

    public HigherOrderMutation getMutation() {
        return mutation;
    }

    public double getFitness() {
        return fitness;
    }

    public boolean hasValidFitness() {
        return !(Double.isInfinite(fitness) || Math.abs(fitness) < 1e-10);
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
        return "MutationContainer [fitness=" + fitness + ", killedTests=" + killedTests.size() + ", hom=" + mutation + "]";
    }

    public double mutationFitness(Map<MutationDetails, MutationContainer> foms) {
        if (mutation.getOrder() <= 1) {
            Log.getLogger().fine("FOM killed tests: " + this.killedTests.size());
            return 0.0;
        }
        Set<Description> fomKilledTests = null;
        for (MutationDetails m : mutation.getAllMutationDetails()) {
            Log.getLogger().fine("FOM: killed " + foms.get(m).killedTests.size());
            if (fomKilledTests == null) {
                fomKilledTests = foms.get(m).killedTests;
            } else {
                // intersect
                fomKilledTests.retainAll(foms.get(m).killedTests);
            }
        }

        double fragilityFOM = fomKilledTests.size() / this.numTests;
        double fragilityHOM = this.killedTests.size() / this.numTests;
        Log.getLogger().fine("FOM: " + fragilityFOM + " HOM: " + fragilityHOM);
        return fragilityHOM / fragilityFOM;
    }
}
