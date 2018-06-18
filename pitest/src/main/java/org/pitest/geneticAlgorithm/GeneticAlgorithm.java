package org.pitest.geneticAlgorithm;

import org.pitest.mutationtest.engine.HigherOrderMutation;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.execute.AllTestDataListener;
import org.pitest.util.Log;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GeneticAlgorithm {
    private static final Logger LOG = Log.getLogger();
    private static final int MIN_ORDER = 2;
    private List<MutationDetails> allFOMs;
    private Function<HigherOrderMutation, AllTestDataListener> testRunner;
    private Map<MutationDetails, MutationContainer> fomFitness;

    public GeneticAlgorithm(List<MutationDetails> allFOMs,
        Function<HigherOrderMutation, AllTestDataListener> testRunner) {
        this.allFOMs = allFOMs;
        this.testRunner = testRunner;
        this.fomFitness = allFOMs.stream()
            .collect(Collectors.toMap(m -> m,
                m -> new MutationContainer(HigherOrderMutation.fomAsHom(m), testRunner, null)));
        LOG.fine("" + this.fomFitness);
    }

    public void geneticAlgorithm() {
        // actual algorithm
        int populationSize = 50;
        double percentDiscarded = 1.0 / 3.0; // TODO: properties file
        int numDiscarded = (int) (populationSize * percentDiscarded);
        int numIters = 100;

        //generate some homs based on foms
        MutationContainer[] homPopulation = genHOMs(2, populationSize);

        for (int i = 0; i < numIters; i++) {
            archive(homPopulation, i);
            Arrays.sort(homPopulation);
            int j = 0;

            for (; j < numDiscarded / 2; j += 2) {
                //crossover half of remaining
                int parentIndex1 = RandomUtils
                    .randRange(numDiscarded, populationSize);
                int parentIndex2;
                do {
                    parentIndex2 = RandomUtils
                        .randRange(numDiscarded, populationSize);
                } while (parentIndex1 == parentIndex2);

                MutationContainer parent1 = homPopulation[parentIndex1];
                MutationContainer parent2 = homPopulation[parentIndex2];

                MutationContainer[] children = crossover(parent1.getMutation(),
                    parent2.getMutation());
                homPopulation[j] = children[0];
                homPopulation[j + 1] = children[1];
            }
            for (; j < numDiscarded; j++) {
                //mutate half of remaining
                int parentIndex = RandomUtils
                    .randRange(numDiscarded, populationSize);
                MutationContainer parent = homPopulation[parentIndex];
                homPopulation[j] = randomlyMutate(parent.getMutation());
            }

        }
    }

    public MutationContainer randomlyMutate(HigherOrderMutation hom) {
        HigherOrderMutation newMutation;
        if (hom.getOrder() > 1 && Math.random() < 0.5) { //delete fom
            int deletedIndex = RandomUtils.randRange(0, hom.getOrder());
            newMutation = deleteFOM(hom, deletedIndex);

        } else { //add fom
            newMutation = addFOM(hom, generateRandomUnusedFOM(hom, this.allFOMs));
        }

        return new MutationContainer(newMutation, this.testRunner, this.fomFitness);
    }

    public MutationContainer[] crossover(HigherOrderMutation a, HigherOrderMutation b) {
        int randIndex1;
        int randIndex2;
        try {
            randIndex1 = generateRandomUnusedFOM(b, a.getAllMutationDetails());
            randIndex2 = generateRandomUnusedFOM(a, b.getAllMutationDetails());
        } catch(RuntimeException e) {
            LOG.fine("Cannot crossover, mutating instead.");
            return new MutationContainer[] { randomlyMutate(a),
                randomlyMutate(b) };
        }

        HigherOrderMutation child1 = new HigherOrderMutation();
        HigherOrderMutation child2 = new HigherOrderMutation();

        for (int i = 0; i < a.getOrder(); i++) {
            if (i == randIndex1) {
                child1.addMutation(b.getMutationDetail(randIndex2));
            } else {
                child1.addMutation(a.getMutationDetail(i));
            }
        }

        for (int i = 0; i < b.getOrder(); i++) {
            if (i == randIndex2) {
                child2.addMutation(a.getMutationDetail(randIndex1));
            } else {
                child2.addMutation(b.getMutationDetail(i));
            }
        }
        return new MutationContainer[] {
            new MutationContainer(child1, this.testRunner, this.fomFitness),
            new MutationContainer(child2, this.testRunner, this.fomFitness) };
    }

    protected HigherOrderMutation deleteFOM(HigherOrderMutation hom, int deletedIndex) {
        HigherOrderMutation newHom = new HigherOrderMutation();
        for (int i = 0; i < hom.getOrder(); i++) {
            if (i != deletedIndex) {
                newHom.addMutation(hom.getMutationDetail(i));
            }
        }
        return newHom;
    }

    protected HigherOrderMutation addFOM(HigherOrderMutation hom, int fomToAdd) {
        HigherOrderMutation newHom = hom.clone();
        newHom.addMutation(this.allFOMs.get(fomToAdd));
        return newHom;
    }

    private int generateRandomUnusedFOM(HigherOrderMutation hom, List<MutationDetails> fomList) {
        int newFOMIndex;
        int attempts = 0;
        do {
            if (attempts++ > fomList.size()) {
                Log.getLogger().info(
                    "Too many attempts to generate a random FOM (list size "
                        + fomList.size() + ") for HOM " + hom);
                throw new RuntimeException(
                    "Took too many attempts to generate a random FOM. Try changing the max order of your HOMs to something less than "
                        + fomList.size());
            }
            newFOMIndex = RandomUtils.randRange(0, fomList.size());
        } while (hom.getAllMutationDetails()
            .contains(fomList.get(newFOMIndex)));
        return newFOMIndex;
    }

    protected MutationContainer[] genHOMs(int maxOrder, int numHOMs) {
        if (maxOrder <= 0) {
            throw new IllegalArgumentException(
                "The max order of higher order mutations must be greater than 0.");
        }
        MutationContainer[] homs = new MutationContainer[numHOMs];
        for (int i = 0; i < numHOMs; i++) {
            int order = RandomUtils.randRange(MIN_ORDER, maxOrder+1);
            HigherOrderMutation newHOM = new HigherOrderMutation();

            for (int j = 0; j < order; j++) {
                int mutationIdx = generateRandomUnusedFOM(newHOM, allFOMs);
                newHOM.addMutation(this.allFOMs.get(mutationIdx));
            }

            MutationContainer container = new MutationContainer(newHOM,
                this.testRunner, this.fomFitness);
            homs[i] = container;
        }
        return homs;
    }

    /**
     * For now this is just printing to the console
     * TODO: actual archiving?
     * @param homPopulation
     * @param generation
     */
    private static void archive(MutationContainer[] homPopulation, int generation) {
        LOG.info("Generation " + generation + "---------------------------------------");
        for (MutationContainer mc : homPopulation) {
            LOG.info("" + mc);
        }
    }






}
