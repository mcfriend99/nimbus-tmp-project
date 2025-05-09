package org.blade.language.builtins;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.*;
import com.oracle.truffle.api.interop.InteropLibrary;
import com.oracle.truffle.api.library.CachedLibrary;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.strings.TruffleString;
import org.blade.language.BaseBuiltinDeclaration;
import org.blade.language.nodes.functions.NBuiltinFunctionNode;
import org.blade.language.runtime.*;
import org.blade.utility.RegulatedMap;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class BuiltinFunctions implements BaseBuiltinDeclaration {
  @Override
  public RegulatedMap<String, Boolean, NodeFactory<? extends NBuiltinFunctionNode>> getDeclarations() {
    return new RegulatedMap<>() {{
      add("time", false, BuiltinFunctionsFactory.TimeFunctionNodeFactory.getInstance());
      add("print", true, BuiltinFunctionsFactory.PrintFunctionNodeFactory.getInstance());
      add("microtime", false, BuiltinFunctionsFactory.MicroTimeFunctionNodeFactory.getInstance());
      add("abs", false, BuiltinFunctionsFactory.AbsFunctionNodeFactory.getInstance());
      add("bin", false, BuiltinFunctionsFactory.BinFunctionNodeFactory.getInstance());
      add("chr", false, BuiltinFunctionsFactory.ChrFunctionNodeFactory.getInstance());
      add("hex", false, BuiltinFunctionsFactory.HexFunctionNodeFactory.getInstance());
      add("id", false, BuiltinFunctionsFactory.IdFunctionNodeFactory.getInstance());
    }};
  }

  public abstract static class TimeFunctionNode extends NBuiltinFunctionNode {
    @Specialization
    protected long doAny() {
      return time() / 1000;
    }

    @CompilerDirectives.TruffleBoundary
    private long time() {
      return System.currentTimeMillis();
    }
  }

  public abstract static class PrintFunctionNode extends NBuiltinFunctionNode {

    @Specialization
    public Object doList(ListObject object,
                         @CachedLibrary(limit = "3") InteropLibrary interopLibrary,
                         @Cached(value = "languageContext()", neverDefault = false) BladeContext context) {
      print(context, interopLibrary, object.items);
      return BladeNil.SINGLETON;
    }

    @Fallback
    protected Object fallback(Object object) {
      BladeContext.get(this).println(BString.concatString("Something not working right: ", object));
      return BladeNil.SINGLETON;
    }

    @ExplodeLoop
    private void print(BladeContext context, InteropLibrary interopLibrary, Object[] arguments) {
      int length = arguments.length;

      for (int i = 0; i < length - 1; i++) {
        if (arguments[i] != BladeNil.SINGLETON) {
          context.print(BString.fromObject(interopLibrary, arguments[i]));
          context.print(" ");
        }
      }

      if (arguments[length - 1] != BladeNil.SINGLETON) {
        context.print(BString.fromObject(interopLibrary, arguments[length - 1]));
      }

      context.flushOutput();
    }
  }

  public abstract static class MicroTimeFunctionNode extends NBuiltinFunctionNode {
    @Specialization
    protected long doAny() {
      return microTime();
    }

    @CompilerDirectives.TruffleBoundary
    private long microTime() {
      return ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now());
    }
  }

  public abstract static class AbsFunctionNode extends NBuiltinFunctionNode {
    @Specialization(rewriteOn = ArithmeticException.class)
    protected long doLong(long arg) {
      return arg < 0 ? Math.negateExact(arg) : arg;
    }

    @Specialization(replaces = "doLong")
    protected double doDouble(double arg) {
      return Math.abs(arg);
    }

    @Fallback
    protected double notANumber(Object object) {
      return Double.NaN;
    }
  }

  public abstract static class BinFunctionNode extends NBuiltinFunctionNode {
    @Specialization
    protected TruffleString doLong(long arg,
                                   @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
      return BString.fromObject(fromJavaStringNode, Long.toBinaryString(arg));
    }

    @Fallback
    protected double doInvalid(Object object) {
      throw BladeRuntimeError.argumentError(this, "bin", object);
    }
  }

  public abstract static class ChrFunctionNode extends NBuiltinFunctionNode {
    @Specialization
    protected TruffleString doLong(long arg,
                                   @Cached TruffleString.FromCodePointNode fromCodePointNode,
                                   @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
      return BString.fromObject(fromJavaStringNode, BString.fromCodePoint(fromCodePointNode, (int)arg));
    }

    @Fallback
    protected double doInvalid(Object object) {
      throw BladeRuntimeError.argumentError(this, "chr", object);
    }
  }

  public abstract static class HexFunctionNode extends NBuiltinFunctionNode {
    @Specialization
    protected TruffleString doLong(long arg,
                                   @Cached TruffleString.FromJavaStringNode fromJavaStringNode) {
      return BString.fromObject(fromJavaStringNode, Long.toHexString(arg));
    }

    @Fallback
    protected double doInvalid(Object object) {
      throw BladeRuntimeError.argumentError(this, "hex", object);
    }
  }

  public abstract static class IdFunctionNode extends NBuiltinFunctionNode {
    @Specialization
    protected long doNimObject(BladeObject arg) {
      return arg.hash();
    }

    @Fallback
    protected long doOthers(Object object) {
      return hash(object);
    }

    @CompilerDirectives.TruffleBoundary
    protected long hash(Object object) {
      return object.hashCode();
    }
  }
}
