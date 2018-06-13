package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationSource;
import org.pitest.mutationtest.build.TestPrioritiser;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;

public class AllTestsMutationSource extends MutationSource {
  private final CoverageDatabase coverageDatabase;

  public AllTestsMutationSource(MutationConfig mutationConfig,
      TestPrioritiser testPrioritiser, ClassByteArraySource source,
      MutationInterceptor interceptor, CoverageDatabase coverageDatabase) {
    super(mutationConfig, testPrioritiser, source, interceptor);
    this.coverageDatabase = coverageDatabase;
  }

  @Override
  protected void assignTestsToMutations(
      final Collection<MutationDetails> availableMutations) {
    for (final MutationDetails mutation : availableMutations) {
      final Collection<TestInfo> testDetails = this.coverageDatabase
          .getTestsForClass(mutation.getClassName());
      if (testDetails.isEmpty()) {
        LOG.fine("According to coverage no tests hit the class " + mutation.getClassName());
      }
      mutation.addTestsInOrder(testDetails);
    }
  }
}
