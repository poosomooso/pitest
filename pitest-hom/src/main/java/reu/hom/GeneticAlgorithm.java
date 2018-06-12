package reu.hom;

import org.pitest.mutationtest.commandline.OptionsParser;
import org.pitest.mutationtest.commandline.ParseResult;
import org.pitest.mutationtest.commandline.PluginFilter;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Arrays;
import java.util.List;

public class GeneticAlgorithm {
    public static void main(String[] args) {

        // PIT command line args for now
        final PluginServices plugins = PluginServices.makeForContextLoader();
        final OptionsParser parser = new OptionsParser(new PluginFilter(plugins));
        final ParseResult pr = parser.parse(args);

        if (!pr.isOk()) {
            parser.printHelp();
            System.out.println(">>>> " + pr.getErrorMessage().get());
            System.exit(0);
        }
        final ReportOptions data = pr.getOptions();
        final SettingsFactory settings = new SettingsFactory(data, plugins);

        // actual algorithm
        int populationSize = 30;
        double percentDiscarded = 1.0/3.0; // TODO: properties file
        int numDiscarded = (int) (populationSize * percentDiscarded);
        int numIters = 20;

        //generate all foms
        List<MutationDetails> allFOMs = MutationGeneration.getFOMs(data, settings);
        System.out.println(allFOMs);

        //generate some homs based on foms
        MutationContainer[] homPopulation = MutationGeneration.genHOMs(4, populationSize, allFOMs);

        for (int i = 0; i < numIters; i++) {
            archive(homPopulation, i);
            Arrays.sort(homPopulation);
            int j = 0;

            for (; j < numDiscarded/2; j+=2) {
                //crossover half of remaining
                int parentIndex1 = Utils.randRange(numDiscarded, populationSize);
                int parentIndex2;
                do {
                    parentIndex2 = Utils.randRange(numDiscarded, populationSize);
                } while (parentIndex1 == parentIndex2);

                MutationContainer parent1 = homPopulation[parentIndex1];
                MutationContainer parent2 = homPopulation[parentIndex2];

                MutationContainer[] children = parent1.crossover(parent2);
                homPopulation[j] = children[0];
                homPopulation[j+1] = children[1];
            }
            for (; j < numDiscarded; j++) {
                //mutate half of remaining
                int parentIndex = Utils.randRange(numDiscarded, populationSize);
                MutationContainer parent = homPopulation[parentIndex];
                homPopulation[j] = parent.randomlyMutate(allFOMs);
            }

        }
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
