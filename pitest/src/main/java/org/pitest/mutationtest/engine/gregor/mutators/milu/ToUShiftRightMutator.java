package org.pitest.mutationtest.engine.gregor.mutators.milu;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.*;

import java.util.HashMap;
import java.util.Map;

public enum ToUShiftRightMutator implements MethodMutatorFactory {

  TO_U_SHIFT_RIGHT_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ToUShiftRightVisitor(this, methodInfo, context, methodVisitor);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String getName() {
    return name();
  }

}

class ToUShiftRightVisitor extends AbstractInsnMutator {

  ToUShiftRightVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

  static {
    MUTATIONS.put(Opcodes.ISHL, new InsnSubstitution(Opcodes.IUSHR,
        "Replaced Shift Left with Unsigned Shift Right"));
    MUTATIONS.put(Opcodes.ISHR, new InsnSubstitution(Opcodes.IUSHR,
        "Replaced Shift Right with Unsigned Shift Right"));
//    MUTATIONS.put(Opcodes.IUSHR, new InsnSubstitution(Opcodes.ISHR,
//        "Replaced Unsigned Shift Right with Shift Right"));

    // longs
    MUTATIONS.put(Opcodes.LSHL, new InsnSubstitution(Opcodes.LUSHR,
        "Replaced Shift Left within Unsigned Shift Right"));
    MUTATIONS.put(Opcodes.LSHR, new InsnSubstitution(Opcodes.LUSHR,
        "Replaced Shift Right with Unsigned Shift Right"));
//    MUTATIONS.put(Opcodes.LUSHR, new InsnSubstitution(Opcodes.LSHR,
//        "Replaced Unsigned Shift Right with Shift Right"));

  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return MUTATIONS;
  }

}
