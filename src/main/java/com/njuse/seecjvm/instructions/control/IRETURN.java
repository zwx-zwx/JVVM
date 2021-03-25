package com.njuse.seecjvm.instructions.control;

import com.njuse.seecjvm.instructions.base.NoOperandsInstruction;
import com.njuse.seecjvm.runtime.JThread;
import com.njuse.seecjvm.runtime.StackFrame;

public class IRETURN extends NoOperandsInstruction {

    /**
     * TODO： 实现这条指令
     */
    @Override
    public void execute(StackFrame frame) {
        // get current thread
        JThread thread = frame.getThread();
        // get the return value
        int value = frame.getOperandStack().popInt();
        // remove the current stack frame
        thread.popFrame();
        // push the return value into the OperandStack of the new top stack frame
        thread.getTopFrame().getOperandStack().pushInt(value);
    }
}
