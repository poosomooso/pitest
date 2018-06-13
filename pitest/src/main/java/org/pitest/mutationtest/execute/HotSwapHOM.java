package org.pitest.mutationtest.execute;

import org.pitest.boot.HotSwapAgent;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.F3;
import org.pitest.util.Unchecked;

import java.util.List;

public class HotSwapHOM implements
    F3<ClassName,ClassLoader,List<byte[]>,Boolean> {

  private final ClassByteArraySource byteSource;
  private byte[]                     lastClassPreMutation;
  private ClassName                  lastMutatedClass;
  private ClassLoader                lastUsedLoader;

  HotSwapHOM(final ClassByteArraySource byteSource) {
    this.byteSource = byteSource;
  }

  @Override
  public Boolean apply(ClassName className, ClassLoader classLoader,
      List<byte[]> bytes) {
    Class<?> clazz;
    try {
      restoreLastClass(this.byteSource, className, classLoader);
      this.lastUsedLoader = classLoader;
      clazz = Class.forName(className.asJavaName(), false, classLoader);
      boolean success = true;
      for (byte[] b : bytes) {
        success = success && HotSwapAgent.hotSwap(clazz, b);
      }
      return success;
    } catch (final ClassNotFoundException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  private void restoreLastClass(final ClassByteArraySource byteSource,
      final ClassName clazzName, final ClassLoader loader)
      throws ClassNotFoundException {
    if ((this.lastMutatedClass != null)
        && !this.lastMutatedClass.equals(clazzName)) {
      restoreForLoader(this.lastUsedLoader);
      restoreForLoader(loader);
    }

    if ((this.lastMutatedClass == null)
        || !this.lastMutatedClass.equals(clazzName)) {
      this.lastClassPreMutation = byteSource.getBytes(clazzName.asJavaName())
          .get();
    }

    this.lastMutatedClass = clazzName;
  }

  private void restoreForLoader(ClassLoader loader)
      throws ClassNotFoundException {
    final Class<?> clazz = Class.forName(this.lastMutatedClass.asJavaName(), false,
        loader);
    HotSwapAgent.hotSwap(clazz, this.lastClassPreMutation);
  }
}
