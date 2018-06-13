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

import java.util.HashMap;
import java.util.List;

public class MutationGeneration {
  protected static List<MutationDetails> getFOMs(ReportOptions data, SettingsFactory settings) {
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
    final MutationSource source = new AllTestsMutationSource(mutationConfig, testPrioritiser, bas, interceptor, coverageData);
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
   * Copied from EntryPoint.java
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
