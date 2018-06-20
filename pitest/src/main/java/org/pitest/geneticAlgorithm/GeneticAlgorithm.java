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
    private static final int MAX_ORDER = 5;
    private List<MutationDetails> allFOMs;
    public Function<HigherOrderMutation, AllTestDataListener> testRunner;
    public Map<MutationDetails, MutationContainer> fomFitness;

    private int numDeletions = 0;
    private int numAdditions = 0;
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
        double percentDiscarded = 1.0 / 2.0; // TODO: properties file

        int numIters = 100;

        //generate some homs based on foms
        MutationContainer[] homPopulation = genHOMs(2, populationSize);

        for (int i = 0; i < numIters; i++) {
            archive(homPopulation, i);
            Arrays.sort(homPopulation);
            int numDiscarded = Math.max(
                arrLastIndexOf(homPopulation), (int) (populationSize * percentDiscarded));
            int numCrossovers = crossover(homPopulation, numDiscarded);
            mutate(homPopulation, numCrossovers, numDiscarded);
        }
        LOG.info("Num deletions: " + numDeletions);
        LOG.info("Num additions: " + numAdditions);

    }

    private int arrLastIndexOf(MutationContainer[] homPopulation) {
        for (int i = homPopulation.length - 1; i >= 0; i--) {
            if (!homPopulation[i].hasValidFitness()) {
                return i;
            }
        }
        return 0;
    }

    public int crossover(MutationContainer[] sortedHOMS, int numDiscarded) {
        int i = 0;
        for (; i < numDiscarded / 2; i+=2) {
            int parentIndex1 = RandomUtils
                .randRange(numDiscarded, sortedHOMS.length);
            int parentIndex2 = RandomUtils
                    .randRange(numDiscarded, sortedHOMS.length);

            MutationContainer parent1 = sortedHOMS[parentIndex1];
            MutationContainer parent2 = sortedHOMS[parentIndex2];

            MutationContainer[] children = crossoverParents(parent1.getMutation(),
                parent2.getMutation());
            sortedHOMS[i] = children[0];
            sortedHOMS[i + 1] = children[1];
        }
        return i;
    }

    public void mutate(MutationContainer[] sortedHOMS, int startIndex, int numDiscarded) {
        for (int i = startIndex; i < numDiscarded; i++) {
            int parentIndex = RandomUtils
                .randRange(numDiscarded, sortedHOMS.length);
            MutationContainer parent = sortedHOMS[parentIndex];
            sortedHOMS[i] = randomlyMutate(parent.getMutation());
        }
    }


    public MutationContainer randomlyMutate(HigherOrderMutation hom) {
        HigherOrderMutation newMutation;
        if ((hom.getOrder() >= MAX_ORDER) ||
            (hom.getOrder() > MIN_ORDER && Math.random() < 0.5)) { //delete fom
            numDeletions++;
            int deletedIndex = RandomUtils.randRange(0, hom.getOrder());
            newMutation = deleteFOM(hom, deletedIndex);

        } else { //add fom
            numAdditions++;
            newMutation = addFOM(hom,
                generateRandomUnusedFOM(hom, this.allFOMs));
        }

        return new MutationContainer(newMutation, this.testRunner, this.fomFitness);
    }

    public MutationContainer[] crossoverParents(HigherOrderMutation a, HigherOrderMutation b) {
        int randIndex1;
        int randIndex2;
        try {
            randIndex1 = generateRandomUnusedFOM(b, a.getAllMutationDetails());
            randIndex2 = generateRandomUnusedFOM(a, b.getAllMutationDetails());
        } catch(RuntimeException e) {
            LOG.fine("Cannot crossoverParents, mutating instead.");
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
        LOG.info("" + newHom);
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
        int i = 0;
        int numAttempts = 0;
        while (i < numHOMs) {
            if (++numAttempts % 10 == 0) {
                LOG.info("Currently there have been " + numAttempts + " attempts at generating HOMS, " + i + " HOMS generated");
            }
            int order = 2; //RandomUtils.randRange(MIN_ORDER, maxOrder+1);
            HigherOrderMutation newHOM = new HigherOrderMutation();

            for (int j = 0; j < order; j++) {
                int mutationIdx = generateRandomUnusedFOM(newHOM, allFOMs);
                newHOM.addMutation(this.allFOMs.get(mutationIdx));
            }

            MutationContainer container = new MutationContainer(newHOM,
                this.testRunner, this.fomFitness);
            if (container.hasValidFitness() || i > 9) {
                homs[i++] = container;
            }
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
