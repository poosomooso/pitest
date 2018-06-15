package org.pitest.mutationtest.execute;

import org.pitest.testapi.Description;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;

import java.util.HashSet;
import java.util.Set;

public class AllTestDataListener implements TestListener {

  Set<Description> killedTests;
  Set<Description> survivedTests;

  @Override
  public void onRunStart() {
    killedTests = new HashSet<>();
    survivedTests = new HashSet<>();
  }

  @Override
  public void onTestStart(Description d) {

  }

  @Override
  public void onTestFailure(TestResult tr) {
    killedTests.add(tr.getDescription());
  }

  @Override
  public void onTestSkipped(TestResult tr) {
    // TODO: why would a test get skipped?
    survivedTests.add(tr.getDescription());
  }

  @Override
  public void onTestSuccess(TestResult tr) {
    survivedTests.add(tr.getDescription());
  }

  @Override
  public void onRunEnd() {

  }

  public Set<Description> getKilledTests() {
    return killedTests;
  }

  public Set<Description> getSurvivedTests() {
    return survivedTests;
  }
}
