package reu.hom;

import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.geneticAlgorithm.GeneticAlgorithmMutationTestWorker;
import org.pitest.mutationtest.config.ClientPluginServices;
import org.pitest.mutationtest.config.MinionSettings;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.execute.*;
import org.pitest.testapi.TestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Log;
import org.pitest.util.SafeDataInputStream;

import java.net.Socket;
import java.util.List;
import java.util.logging.Level;

public class HigherOrderMutationTestMinion extends MutationTestMinion {
  public HigherOrderMutationTestMinion(MinionSettings plugins, final SafeDataInputStream dis,
      final Reporter reporter) {
    super(plugins, dis, reporter);
  }

  public void run() {
    try {
      final MinionArguments paramsFromParent = this.dis
          .read(MinionArguments.class);
      Log.setVerbose(paramsFromParent.isVerbose());

      final ClassLoader loader = IsolationUtils.getContextClassLoader();

      final ClassByteArraySource byteSource = new CachingByteArraySource(new ClassloaderByteArraySource(
          loader), CACHE_SIZE);

      final MutationEngine engine = createEngine(paramsFromParent.engine, paramsFromParent.engineArgs);

      final MutationTestWorker worker = getMutationTestWorker(engine,
          byteSource, loader);

      final List<TestUnit> tests = findTestsForTestClasses(loader,
          paramsFromParent.testClasses, createTestPlugin(paramsFromParent.pitConfig));

      worker.run(paramsFromParent.mutations, this.reporter,
          new TimeOutDecoratedTestSource(paramsFromParent.timeoutStrategy,
              tests, this.reporter));

      this.reporter.done(ExitCode.OK);
    } catch (final Throwable ex) {
      ex.printStackTrace(System.out);
      LOG.log(Level.WARNING, "Error during mutation test", ex);
      this.reporter.done(ExitCode.UNKNOWN_ERROR);
    }

  }

  public static void main(final String[] args) {

    LOG.log(Level.FINE, "minion started");

    enablePowerMockSupport();

    final int port = Integer.valueOf(args[0]);

    Socket s = null;
    try {
      s = new Socket("localhost", port);
      final SafeDataInputStream dis = new SafeDataInputStream(
          s.getInputStream());

      final Reporter reporter = new DefaultReporter(s.getOutputStream());
      addMemoryWatchDog(reporter);
      final ClientPluginServices plugins = new ClientPluginServices(IsolationUtils.getContextClassLoader());
      final MinionSettings factory = new MinionSettings(plugins);
      final MutationTestMinion instance = new HigherOrderMutationTestMinion(
          factory, dis, reporter);
      instance.run();
    } catch (final Throwable ex) {
      ex.printStackTrace(System.out);
      LOG.log(Level.WARNING, "Error during mutation test", ex);
    } finally {
      if (s != null) {
        safelyCloseSocket(s);
      }
    }

  }

  @Override
  protected MutationTestWorker getMutationTestWorker(
      MutationEngine engine, ClassByteArraySource byteSource,
      ClassLoader loader) {
    return new GeneticAlgorithmMutationTestWorker(new HotSwapHOM(byteSource),
        engine.createMutator(byteSource), loader);
  }
}
