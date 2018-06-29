package org.pitest.mutationtest.engine.gregor.mutators.milu;

import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator;

public class ConstToZeroMutator extends InlineConstantMutator {
  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ConstToZeroVisitor(context, methodVisitor);
  }

  protected class ConstToZeroVisitor extends InlineConstantVisitor{

    ConstToZeroVisitor(MutationContext context, MethodVisitor delegateVisitor) {
      super(context, delegateVisitor);
    }
    @Override
    protected Number mutate(final Double constant) {
      // avoid addition to floating points as may yield same value

      final Double replacement = (constant != 0D) ? 0D : constant;
      return replacement;
    }

    protected Number mutate(final Float constant) {
      // avoid addition to floating points as may yield same value

      final Float replacement = (constant == 0F) ? 0F : constant;
      return replacement;
    }

    protected Number mutate(final Integer constant) {
      final Integer replacement;

      return constant != 0 ? 0 : constant;
    }

    protected Number mutate(final Long constant) {

      return constant != 0L ? 0L : constant;
    }
  }

}
