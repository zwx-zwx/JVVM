package com.njuse.seecjvm.instructions.invoke;

import com.njuse.seecjvm.instructions.base.Index16Instruction;
import com.njuse.seecjvm.memory.jclass.InitState;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.Method;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.RuntimeConstantPool;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.Constant;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.InterfaceMethodRef;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.MemberRef;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.MethodRef;
import com.njuse.seecjvm.runtime.OperandStack;
import com.njuse.seecjvm.runtime.StackFrame;
import com.njuse.seecjvm.runtime.Vars;
import com.njuse.seecjvm.runtime.struct.Slot;

public class INVOKE_STATIC extends Index16Instruction {

    /**
     * TODO 实现这条指令，注意其中的非标准部分：
     * 1. TestUtil.equalInt(int a, int b): 如果a和b相等，则跳过这个方法，
     * 否则抛出`RuntimeException`, 其中，这个异常的message为
     * ：${第一个参数的值}!=${第二个参数的值}
     * 例如，TestUtil.equalInt(1, 2)应该抛出
     * RuntimeException("1!=2")
     *
     * 2. TestUtil.fail(): 抛出`RuntimeException`
     *
     * 3. TestUtil.equalFloat(float a, float b): 如果a和b相等，则跳过这个方法，
     * 否则抛出`RuntimeException`. 对于异常的message不作要求
     *
     */
    @Override
    public void execute(StackFrame frame) {
        RuntimeConstantPool constantPool = frame.getMethod().getClazz().getRuntimeConstantPool();
        MemberRef methodRef = (MemberRef)constantPool.getConstant(index);
        Method toInvoke;
        if(methodRef instanceof MethodRef) toInvoke = ((MethodRef) methodRef).resolveMethodRef();
        else toInvoke = ((InterfaceMethodRef) methodRef).resolveInterfaceMethodRef();


        if(methodRef.getClassName().startsWith("TestUtil")){
            OperandStack opStack = frame.getOperandStack();
            String name = toInvoke.getName();
            if(name.endsWith("equalInt")){
                int a = opStack.popInt();
                int b = opStack.popInt();
                if(a!=b){
                    throw new RuntimeException(""+b+"!="+a);
                }
                opStack.pushInt(b);
                opStack.pushInt(a);
            } else if(name.endsWith("equalFloat")){
                float a = opStack.popFloat();
                float b = opStack.popFloat();
                if((a-b)<-0.0001||(a-b)>0.0001){
                    throw new RuntimeException(""+b+"!="+a);
                }
                opStack.pushFloat(b);
                opStack.pushFloat(a);
            }else if(name.endsWith("fail")){
                throw new RuntimeException();
            }
        }

        // 如果类没有初始化，触发类的初始化过程
        JClass currentClass = toInvoke.getClazz();
        if(currentClass.getInitState()== InitState.PREPARED){
            currentClass.initClass(frame.getThread(), currentClass);
        }

        StackFrame newFrame = new StackFrame(frame.getThread(), toInvoke, toInvoke.getMaxStack(), toInvoke.getMaxLocal());
        int amount = toInvoke.getArgc();
        Slot[] argv = new Slot[amount];
        OperandStack opStack = frame.getOperandStack();
        for(int index = 0; index<amount;index++){
            argv[index] = opStack.popSlot();
        }
        Vars newVars = new Vars(toInvoke.getMaxLocal());
        for(int index=1; index<=amount; index++){
            newVars.setSlot(index-1, argv[amount-index]);
        }
        newFrame.setLocalVars(newVars);
        frame.getThread().pushFrame(newFrame);
    }


}
