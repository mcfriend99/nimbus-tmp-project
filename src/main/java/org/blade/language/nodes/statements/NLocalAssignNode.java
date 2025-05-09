package org.blade.language.nodes.statements;

import com.oracle.truffle.api.dsl.ImportStatic;
import com.oracle.truffle.api.dsl.NodeChild;
import com.oracle.truffle.api.dsl.NodeField;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import org.blade.language.nodes.NNode;
import org.blade.language.nodes.NStmtNode;

@NodeChild(value = "value", type = NNode.class)
@NodeField(name = "slotName", type = String.class)
@NodeField(name = "slot", type = int.class)
@ImportStatic(FrameSlotKind.class)
public abstract class NLocalAssignNode extends NStmtNode {
  public abstract String getSlotName();
  public abstract int getSlot();

  @Specialization(guards = "frame.getFrameDescriptor().getSlotKind(getSlot()) == Illegal || " +
    "frame.getFrameDescriptor().getSlotKind(getSlot()) == Boolean")
  protected boolean doBoolean(VirtualFrame frame, boolean value) {
    int frameSlot = getSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Boolean);
    frame.setBoolean(frameSlot, value);
    return value;
  }

  @Specialization(guards = "frame.getFrameDescriptor().getSlotKind(getSlot()) == Illegal || " +
    "frame.getFrameDescriptor().getSlotKind(getSlot()) == Long")
  protected long doLong(VirtualFrame frame, long value) {
    int frameSlot = getSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Long);
    frame.setLong(frameSlot, value);
    return value;
  }

  @Specialization(replaces = "doLong",
    guards = "frame.getFrameDescriptor().getSlotKind(getSlot()) == Illegal || " +
      "frame.getFrameDescriptor().getSlotKind(getSlot()) == Double")
  protected double doDouble(VirtualFrame frame, double value) {
    int frameSlot = getSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Double);
    frame.setDouble(frameSlot, value);
    return value;
  }

  @Specialization(replaces = {"doLong", "doDouble", "doBoolean"})
  protected Object doObject(VirtualFrame frame, Object value) {
    int frameSlot = getSlot();
    frame.getFrameDescriptor().setSlotKind(frameSlot, FrameSlotKind.Object);
    frame.setObject(frameSlot, value);
    return value;
  }
}
