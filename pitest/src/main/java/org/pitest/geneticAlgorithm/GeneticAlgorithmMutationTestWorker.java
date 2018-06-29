package org.pitest.geneticAlgorithm;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.F3;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.HigherOrderMutation;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.execute.AllTestDataListener;
import org.pitest.mutationtest.execute.MutationTestWorker;
import org.pitest.mutationtest.execute.Reporter;
import org.pitest.mutationtest.execute.TimeOutDecoratedTestSource;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.Pitest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.pitest.util.Unchecked.translateCheckedException;

public class GeneticAlgorithmMutationTestWorker extends MutationTestWorker {

  private final F3<ClassName, ClassLoader, byte[], Boolean> hotswapHOM;

  public GeneticAlgorithmMutationTestWorker(
      F3<ClassName, ClassLoader, byte[], Boolean> hotswapMultiple, Mutater mutater,
      ClassLoader loader) {
    super(hotswapMultiple, mutater, loader);
    this.hotswapHOM = hotswapMultiple;
  }

  @Override
  public void run(final Collection<MutationDetails> range, final Reporter r,
      final TimeOutDecoratedTestSource testSource) throws IOException {

    GeneticAlgorithm ga = new GeneticAlgorithm(Collections.unmodifiableList(new ArrayList<>(range)),
        hom -> processHOM(r, testSource, hom));

    //getting mutations on one line
//    List<MutationDetails> lineNo32 = new ArrayList<>();
//    for (MutationDetails m : range) {
//      if (m.getLineNumber() == 32) {
//        lineNo32.add(m);
//        LOG.info(""+m);
//      }
//    }
//
//    GeneticAlgorithm ga = new GeneticAlgorithm(Collections.unmodifiableList(new ArrayList<>(lineNo32)),
//                hom -> processHOM(r, testSource, hom));
//    LOG.info("" + ga.fomFitness);
//
//    for (MutationDetails mutation1 : lineNo32) {
//      for (MutationDetails mutation2 : lineNo32) {
//        if (!mutation1.equals(mutation2)) {
//          LOG.info("" + mutation1);
//          LOG.info("" + mutation2);
//          HigherOrderMutation mutation = new HigherOrderMutation();
//          mutation.addMutation(mutation1);
//          mutation.addMutation(mutation2);
//          MutationContainer container = new MutationContainer(mutation,
//              ga.testRunner, ga.fomFitness);
//          LOG.info("" + container);
//        }
//      }
//    }

    LOG.info(
        "STARTING GENETIC ALGORITHM -------------------------- SIZE " + range.size());
        ga.geneticAlgorithm();

    // all pairs of mutants
//    for (final MutationDetails mutation1 : range) {
//      for (final MutationDetails mutation2 : range) {
//        if (!mutation1.equals(mutation2)) {
//          if (DEBUG) {
//            LOG.fine("Running mutations " + mutation1 + " and " + mutation2);
//          }
//
//          final long t0 = System.currentTimeMillis();
//          HigherOrderMutation mutation = new HigherOrderMutation();
//          mutation.addMutation(mutation1);
//          mutation.addMutation(mutation2);
//          MutationContainer container = new MutationContainer(mutation,
//              ga.testRunner, ga.fomFitness);
//          LOG.info("" + container.getFitness());
//          //          processHOM(r, testSource, mutation);
//
//          if (DEBUG) {
//            LOG.fine(
//                "processed mutations in " + (System.currentTimeMillis() - t0) + " ms.");
//          }
//        }
//      }
//    }
  }

  /**
   *
   * @param r TODO: use me
   * @param testSource
   * @param mutation
   */
  private AllTestDataListener processHOM(Reporter r, TimeOutDecoratedTestSource testSource,
      HigherOrderMutation mutation) {

    final List<TestUnit> allTests = mutation.getAllMutationDetails().stream()
        .map(md -> testSource.translateTests(md.getTestsInOrder())) // get tests for mutations
        .flatMap(Collection::stream) //flatten tests
        .distinct() //remove duplicates
        .collect(Collectors.toList());

    final AllTestDataListener listener = new AllTestDataListener();
    MutationStatusTestPair mutationDetected;
    if ((allTests == null) || allTests.isEmpty()) {
      LOG.info(
          "No test coverage for mutations " + mutation.getIds()
              + " in " + mutation.getClassName());
      mutationDetected = new MutationStatusTestPair(0,
          DetectionStatus.RUN_ERROR);
    } else {
      mutationDetected = runMutation(mutation, allTests, listener);
    }

    if (mutation.getOrder() == 1) {
      try {
        r.describe(mutation.getIds().get(0));
        r.report(mutation.getIds().get(0), mutationDetected);
      } catch (IOException e) {

      }
    }


    LOG.fine(mutation.toString());
//    LOG.fine("Killed Tests: " + listener.getKilledTests());
//    LOG.fine("Survived Tests: " + listener.getSurvivedTests());
    return listener;
  }

  private MutationStatusTestPair runMutation(final HigherOrderMutation mutation,
      final List<TestUnit> tests, AllTestDataListener listener) {
    MutationStatusTestPair mutationDetected;
    if (DEBUG) {
      LOG.fine(
          "" + tests.size() + " relevant test for " + mutation.getClassName());
    }

    final long t0 = System.currentTimeMillis();
    if (this.hotswapHOM.apply(mutation.getClassName(), this.loader,
        mutation.getBytes(this.mutater))) {

      if (DEBUG) {
        LOG.fine(
            "replaced class with mutant in " + (System.currentTimeMillis() - t0)
                + " ms");
      }
      Container c = createNewContainer();
      mutationDetected = runTests(c, tests, listener);
    } else {
      LOG.warning("Mutation " + mutation + " was not viable ");
      mutationDetected = new MutationStatusTestPair(0,
          DetectionStatus.NON_VIABLE);
    }
    return mutationDetected;
  }

  private MutationStatusTestPair runTests(Container c, List<TestUnit> tests, AllTestDataListener listener) {
    try {
      final Pitest pit = new Pitest(listener);
      pit.run(c, tests);

      int numberOfTestsRun =
          listener.getKilledTests().size() + listener.getSurvivedTests().size();
      return new MutationStatusTestPair(
          numberOfTestsRun,
          listener.getKilledTests().size() > 0 ?
              DetectionStatus.KILLED :
              DetectionStatus.SURVIVED);
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }
  }
}
