package org.pitest.mutationtest.engine.gregor.mutators.milu;

import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator;

public class ConstToNegOneMutator extends InlineConstantMutator {
  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ConstToNegOneVisitor(context, methodVisitor);
  }

  protected class ConstToNegOneVisitor extends InlineConstantVisitor{

    ConstToNegOneVisitor(MutationContext context, MethodVisitor delegateVisitor) {
      super(context, delegateVisitor);
    }
    @Override
    protected Number mutate(final Double constant) {
      // avoid addition to floating points as may yield same value

      return -1D;
    }

    protected Number mutate(final Float constant) {
      // avoid addition to floating points as may yield same value

      return -1F;
    }

    protected Number mutate(final Integer constant) {
      final Integer replacement;

      return -1;
    }

    protected Number mutate(final Long constant) {

      return -1L;
    }
  }

}
