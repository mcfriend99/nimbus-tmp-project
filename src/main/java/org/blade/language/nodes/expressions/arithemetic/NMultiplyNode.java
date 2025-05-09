package org.blade.language.nodes.expressions.arithemetic;

import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Fallback;
import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.strings.TruffleString;
import org.blade.language.BladeLanguage;
import org.blade.language.nodes.NBinaryNode;
import org.blade.language.runtime.ListObject;
import org.blade.language.runtime.BladeContext;
import org.blade.language.runtime.BladeRuntimeError;
import org.blade.language.shared.BuiltinClassesModel;

@ImportStatic(Integer.class)
public abstract class NMultiplyNode extends NBinaryNode {

  @Specialization(rewriteOn = ArithmeticException.class)
  protected long doLongs(long left, long right) {
    return Math.multiplyExact(left, right);
  }

  @Specialization(replaces = {"doLongs"})
  protected double doDoubles(double left, double right) {
    return left * right;
  }

  @Specialization
  protected TruffleString doStringMultiplication(TruffleString string, long count,
                                                 @Cached TruffleString.RepeatNode repeatNode) {
    return repeatNode.execute(string, (int)count, BladeLanguage.ENCODING);
  }

  @Specialization(guards = "count <= MAX_VALUE")
  protected ListObject doListMultiplication(ListObject list, long count) {
    BuiltinClassesModel objectModel = BladeContext.get(this).objectsModel;
    return new ListObject(
      objectModel.listShape,
      objectModel.listObject,
      repeatList(list, count)
    );
  }

  @Specialization(guards = "count > MAX_VALUE")
  protected ListObject doListMultiplicationOutOfBound(ListObject list, long count) {
    throw BladeRuntimeError.create("List multiplication count out of bounds (", count, " > ", Integer.MAX_VALUE, ")");
  }

  @ExplodeLoop
  private Object[] repeatList(ListObject list, long count) {
    int size = (int) list.getArraySize();
    int finalSize = (int)(size * count);

    Object[] objects = new Object[finalSize];

    for(int i = 0; i < count; i++) {
      System.arraycopy(list.items, 0, objects, i * size, size);
    }

    return objects;
  }

  @Fallback
  protected double doUnsupported(Object left, Object right) {
    throw BladeRuntimeError.argumentError(this, "*", left, right);
  }
}
