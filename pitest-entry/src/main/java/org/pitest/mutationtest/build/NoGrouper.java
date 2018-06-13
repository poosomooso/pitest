package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NoGrouper implements MutationGrouper {
  @Override
  public List<List<MutationDetails>> groupMutations(
      Collection<ClassName> codeClasses,
      Collection<MutationDetails> mutations) {
    return Collections.singletonList(new ArrayList<>(mutations));
  }
}
