package org.blade.language.nodes;


import com.oracle.truffle.api.dsl.NodeChild;

@NodeChild("leftNode")
@NodeChild("rightNode")
public abstract class NBinaryNode extends NNode {
  protected static boolean isDouble(Object object) {
    return object instanceof Double;
  }

  protected static boolean isLong(Object object) {
    return object instanceof Double;
  }
}
