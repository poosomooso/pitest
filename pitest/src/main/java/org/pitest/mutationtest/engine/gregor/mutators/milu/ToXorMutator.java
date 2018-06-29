package org.pitest.mutationtest.engine.gregor.mutators.milu;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.*;

import java.util.HashMap;
import java.util.Map;

public enum ToXorMutator implements MethodMutatorFactory {

  TO_XOR_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ToXorVisitor(this, methodInfo, context, methodVisitor);
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

class ToXorVisitor extends AbstractInsnMutator {

  ToXorVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

  static {

    MUTATIONS.put(Opcodes.IOR, new InsnSubstitution(Opcodes.IXOR,
        "Replaced bitwise OR with XOR"));
    MUTATIONS.put(Opcodes.IAND, new InsnSubstitution(Opcodes.IXOR,
        "Replaced bitwise AND with XOR"));
//    MUTATIONS.put(Opcodes.IXOR, new InsnSubstitution(Opcodes.IOR,
//        "Replaced XOR with OR"));

    // longs
    MUTATIONS.put(Opcodes.LOR, new InsnSubstitution(Opcodes.LXOR,
        "Replaced bitwise OR with XOR"));
    MUTATIONS.put(Opcodes.LAND, new InsnSubstitution(Opcodes.LXOR,
        "Replaced bitwise AND with XOR"));
//    MUTATIONS.put(Opcodes.LXOR, new InsnSubstitution(Opcodes.LOR,
//        "Replaced XOR with OR"));

  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return MUTATIONS;
  }

}
