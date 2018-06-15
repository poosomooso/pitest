package org.pitest.geneticAlgorithm;

import org.pitest.mutationtest.engine.HigherOrderMutation;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.execute.AllTestDataListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class GeneticAlgorithm {
    public static void geneticAlgorithm(Collection<MutationDetails> allFOMs,
        Function<HigherOrderMutation, AllTestDataListener> testRunner) {

        // actual algorithm
        int populationSize = 30;
        double percentDiscarded = 1.0 / 3.0; // TODO: properties file
        int numDiscarded = (int) (populationSize * percentDiscarded);
        int numIters = 20;

        //generate all foms
        System.out.println(allFOMs);

        //generate some homs based on foms
//        MutationContainer[] homPopulation = genHOMs(4, populationSize, allFOMs);
//
//        for (int i = 0; i < numIters; i++) {
//            archive(homPopulation, i);
//            Arrays.sort(homPopulation);
//            int j = 0;
//
//            for (; j < numDiscarded / 2; j += 2) {
//                //crossover half of remaining
//                int parentIndex1 = RandomUtils
//                    .randRange(numDiscarded, populationSize);
//                int parentIndex2;
//                do {
//                    parentIndex2 = RandomUtils
//                        .randRange(numDiscarded, populationSize);
//                } while (parentIndex1 == parentIndex2);
//
//                MutationContainer parent1 = homPopulation[parentIndex1];
//                MutationContainer parent2 = homPopulation[parentIndex2];
//
//                MutationContainer[] children = parent1.crossover(parent2);
//                homPopulation[j] = children[0];
//                homPopulation[j + 1] = children[1];
//            }
//            for (; j < numDiscarded; j++) {
//                //mutate half of remaining
//                int parentIndex = RandomUtils
//                    .randRange(numDiscarded, populationSize);
//                MutationContainer parent = homPopulation[parentIndex];
//                homPopulation[j] = parent.randomlyMutate(allFOMs);
//            }
//
//        }
    }

    protected static MutationContainer[] genHOMs(int maxOrder, int numHOMs, List<MutationDetails> allFOMs) {
        if (maxOrder <= 0) {
            throw new IllegalArgumentException(
                "The max order of higher order mutations must be greater than 0.");
        }
        MutationContainer[] homs = new MutationContainer[numHOMs];
        for (int i = 0; i < numHOMs; i++) {
            int order = RandomUtils.randRange(0, maxOrder) + 1; // +1 for converting from index to length
            HigherOrderMutation newHOM = new HigherOrderMutation();

            for (int j = 0; j < order; j++) {
                while (true) {
                    try {
                        int mutationIdx = RandomUtils.randRange(0, numHOMs);
                        newHOM.addMutation(allFOMs.get(mutationIdx));
                        break;
                    } catch (IllegalArgumentException e) {}
                }
            }

            MutationContainer container = new MutationContainer(newHOM);
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
        System.out.println("Generation " + generation + "---------------------------------------");
        for (MutationContainer mc : homPopulation) {
            System.out.println(mc);
        }
    }






}
