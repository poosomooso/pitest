package reu.hom;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.DefaultCoverageGenerator;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationSource;
import org.pitest.mutationtest.build.MutationTestBuilder;
import org.pitest.mutationtest.build.TestPrioritiser;
import org.pitest.mutationtest.commandline.OptionsParser;
import org.pitest.mutationtest.commandline.ParseResult;
import org.pitest.mutationtest.commandline.PluginFilter;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.mutationtest.tooling.KnownLocationJavaAgentFinder;
import org.pitest.mutationtest.tooling.MutationCoverage;
import org.pitest.mutationtest.tooling.MutationStrategies;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Timings;

import java.util.Arrays;
import java.util.HashMap;
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
        int numSurviving = populationSize - numDiscarded;

        //generate all foms
        List<MutationDetails> allFOMs = getMutations(data, settings);
        System.out.println(allFOMs);

        //generate some homs based on foms
        MutationContainer[] homPopulation = new MutationContainer[populationSize];

        int numIters = 20;
        for (int i = 0; i < numIters; i++) {
            archive(homPopulation, i);
            Arrays.sort(homPopulation);
            int j = 0;

            for (; j < numDiscarded/2; j+=2) {
                //crossover half of remaining
                int parentIndex1 = ((int)(Math.random() * numSurviving)) + numDiscarded;
                int parentIndex2;
                do {
                    parentIndex2 =((int)(Math.random() * numSurviving)) + numDiscarded;
                } while (parentIndex1 == parentIndex2);

                MutationContainer parent1 = homPopulation[parentIndex1];
                MutationContainer parent2 = homPopulation[parentIndex2];

                MutationContainer[] children = parent1.crossover(parent2);
                homPopulation[j] = children[0];
                homPopulation[j+1] = children[1];
            }
            for (; j < numDiscarded; j++) {
                //mutate half of remaining
                int parentIndex = ((int)(Math.random() * numSurviving)) + numDiscarded;
                MutationContainer parent = homPopulation[parentIndex];
//                homPopulation[j] = parent.randomlyMutate(allFOMs);
            }

        }
    }

    protected static List<MutationDetails> getMutations(ReportOptions data, SettingsFactory settings) {
        // EntryPoint.java
        selectTestPlugin(data);

        final ClassPath cp = data.getClassPath();

        final JavaAgent jac = new JarCreatingJarFinder(
                new ClassPathByteArraySource(cp));

        final KnownLocationJavaAgentFinder ja = new KnownLocationJavaAgentFinder(
                jac.getJarLocation().get());

        final ResultOutputStrategy reportOutput = settings.getOutputStrategy();

        final MutationResultListenerFactory reportFactory = settings
                .createListener();

        final CoverageOptions coverageOptions = settings.createCoverageOptions();
        final LaunchOptions launchOptions = new LaunchOptions(ja,
                settings.getJavaExecutable(), data.getJvmArgs(), new HashMap<String, String>());
        final ProjectClassPaths cps = data.getMutationClassPaths();
        System.out.println(cps.getClassPath().classNames());

        final CodeSource code = new CodeSource(cps);

        final Timings timings = new Timings();

        final CoverageGenerator coverageGenerator = new DefaultCoverageGenerator(
                null, coverageOptions, launchOptions, code,
                settings.createCoverageExporter(), timings, !data.isVerbose());

        final MutationStrategies strategies = new MutationStrategies(
                settings.createEngine(), null, coverageGenerator, reportFactory,
                reportOutput);

        MutationCoverage coverage = new MutationCoverage(strategies, null,
                code, data, settings, timings);

        // MutationCoverage.java
        coverage.checkCode();

        final CoverageDatabase coverageData = strategies.coverage().calculateCoverage();

        final EngineArguments args = EngineArguments.arguments()
                .withExcludedMethods(data.getExcludedMethods())
                .withMutators(data.getMutators());
        final MutationEngine engine = strategies.factory().createEngine(args);

        // buildMutations()
        final MutationConfig mutationConfig = new MutationConfig(engine, strategies.coverage()
                .getLaunchOptions());

        final ClassByteArraySource bas = coverage.fallbackToClassLoader(new ClassPathByteArraySource(
                data.getClassPath()));

        final TestPrioritiser testPrioritiser = settings.getTestPrioritiser()
                .makeTestPrioritiser(data.getFreeFormProperties(), code,
                        coverageData);

        final MutationInterceptor interceptor = settings.getInterceptor()
                .createInterceptor(data, bas);

        // Actually generating the mutations
        final MutationSource source = new MutationSource(mutationConfig, testPrioritiser, bas, interceptor);
        MutationTestBuilder builder = new MutationTestBuilder(null, null, source, null);
        return builder.getAllMutations(code.getCodeUnderTestNames());
    }

    protected static MutationContainer[] genHOMs(int maxOrder, int numHOMs, List<MutationDetails> allFOMs) {
        if (maxOrder <= 0) {
            throw new IllegalArgumentException(
                "The max order of higher order mutations must be greater than 0.");
        }
        MutationContainer[] homs = new MutationContainer[numHOMs];
        for (int i = 0; i < numHOMs; i++) {
            int order = Utils.randRange(0, maxOrder) + 1; // +1 for converting from index to length
            HigherOrderMutation newHOM = new HigherOrderMutation();

            for (int j = 0; j < order; j++) {
                while (true) {
                    try {
                        int mutationIdx = Utils.randRange(0, numHOMs);
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

    /**
     * Copied from EntryPoint.java
     * @param data
     */
    private static void selectTestPlugin(ReportOptions data) {
        if ((data.getTestPlugin() == null) || data.getTestPlugin().equals("")) {
            if (junit5PluginIsOnClasspath()) {
                data.setTestPlugin("junit5");
            } else {
                data.setTestPlugin("junit");
            }
        }
    }

    /**
     * Copied from EntryPOint.java
     * @return
     */
    private static boolean junit5PluginIsOnClasspath() {
        try {
            Class.forName("org.pitest.junit5.JUnit5TestPluginFactory");
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }


}
