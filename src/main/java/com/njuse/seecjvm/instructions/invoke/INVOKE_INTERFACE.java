package com.njuse.seecjvm.instructions.invoke;

import com.njuse.seecjvm.instructions.base.Index16Instruction;
import com.njuse.seecjvm.memory.MethodArea;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.Method;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.RuntimeConstantPool;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.InterfaceMethodRef;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.MethodRef;
import com.njuse.seecjvm.runtime.OperandStack;
import com.njuse.seecjvm.runtime.StackFrame;
import com.njuse.seecjvm.runtime.Vars;
import com.njuse.seecjvm.runtime.struct.JObject;
import com.njuse.seecjvm.runtime.struct.Slot;

import java.nio.ByteBuffer;


public class INVOKE_INTERFACE extends Index16Instruction {
    private byte count;
    private byte end;
    /**
     * TODO：实现这个方法
     * 这个方法用于读取这条指令操作码以外的部分
     *
     * index 指向一个接口方法的符号引用，解析的方法不能是实例初始化方法或类或接口的初始化方法
     *
     */
    @Override
    public void fetchOperands(ByteBuffer reader) {
        super.fetchOperands(reader);
        // count 用于确定参数数量，但是可以通过描述符计算参数占的数量单位。历史原因
        count = reader.get();
        end = reader.get();
        assert end == 0;
    }

    /**
     * TODO：实现这条指令
     */
    @Override
    public void execute(StackFrame frame) {
        RuntimeConstantPool constantPool = frame.getMethod().getClazz().getRuntimeConstantPool();
        InterfaceMethodRef methodRef = (InterfaceMethodRef) constantPool.getConstant(index);
        Method method = methodRef.resolveInterfaceMethodRef();

        // for bonus
        if(method.getName().equals("getMyNumber")){
            // n个参数和objectref将从操作数栈中弹出
            int amount = method.getArgc();
            Slot[] argv = new Slot[amount];
            OperandStack opStack = frame.getOperandStack();
            for(int index=0; index<amount; index++){
                argv[index] = opStack.popSlot();
            }
            JObject objectRef = opStack.popObjectRef();

            if(objectRef==null) throw new NullPointerException();

            Method toInvoke = methodRef.resolveInterfaceMethodRef(MethodArea.getInstance().findClass("WYM"));
            // 创建新栈帧, objectref 和 参数 都存入局部变量表中
            StackFrame newFrame = new StackFrame(frame.getThread(), toInvoke, toInvoke.getMaxStack(), toInvoke.getMaxLocal());
            Vars newVars = new Vars(toInvoke.getMaxLocal());
            newVars.setObjectRef(0, objectRef);
            for(int index=1; index<=amount; index++){
                newVars.setSlot(index, argv[amount-index]);
            }
            newFrame.setLocalVars(newVars);
            // 将新的栈帧压入当前线程
            frame.getThread().pushFrame(newFrame);
            return;
        }
        // 如果解析的方法是个static或private，抛出InC
        if(method.isPrivate()||method.isStatic()){
            throw new IncompatibleClassChangeError();
        }

        // 如果调用的不是本地方法，
        if(!method.isNative()){
            // n个参数和objectref将从操作数栈中弹出
            int amount = method.getArgc();
            Slot[] argv = new Slot[amount];
            OperandStack opStack = frame.getOperandStack();
            for(int index=0; index<amount; index++){
                argv[index] = opStack.popSlot();
            }
            JObject objectRef = opStack.popObjectRef();

            if(objectRef==null) throw new NullPointerException();

            Method toInvoke = methodRef.resolveInterfaceMethodRef(objectRef.getClazz());
            // 创建新栈帧, objectref 和 参数 都存入局部变量表中
            StackFrame newFrame = new StackFrame(frame.getThread(), toInvoke, toInvoke.getMaxStack(), toInvoke.getMaxLocal());
            Vars newVars = new Vars(toInvoke.getMaxLocal());
            newVars.setObjectRef(0, objectRef);
            for(int index=1; index<=amount; index++){
                newVars.setSlot(index, argv[amount-index]);
            }
            newFrame.setLocalVars(newVars);
            // 将新的栈帧压入当前线程
            frame.getThread().pushFrame(newFrame);
        }

    }


}
