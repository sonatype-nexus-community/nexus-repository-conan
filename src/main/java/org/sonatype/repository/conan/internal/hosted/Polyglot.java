package org.sonatype.repository.conan.internal.hosted;

import org.graalvm.polyglot.*;

public class Polyglot
{
  private Value array;

  public Polyglot() {
    Context context = Context.newBuilder().allowIO(true).build();
    array = context.eval("python", "[1,2,42,4]");
  }

  int getElement(int index) {
    return array.getArrayElement(index).asInt();
  }
}
