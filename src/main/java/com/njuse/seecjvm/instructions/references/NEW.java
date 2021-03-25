package com.njuse.seecjvm.instructions.references;

import com.njuse.seecjvm.instructions.base.Index16Instruction;
import com.njuse.seecjvm.memory.JHeap;
import com.njuse.seecjvm.memory.jclass.InitState;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.RuntimeConstantPool;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.ClassRef;
import com.njuse.seecjvm.runtime.OperandStack;
import com.njuse.seecjvm.runtime.StackFrame;
import com.njuse.seecjvm.runtime.struct.NonArrayObject;

public class NEW extends Index16Instruction {
    /**
     * TODO 实现这条指令
     * 其中 对应的index已经读取好了
     * 目标：根据index指向的类或接口的符号索引
     * 创建一个实例并存入GC堆，将objectref压入操作数栈中
     */
    @Override
    public void execute(StackFrame frame) {
        // 得到常量池
        RuntimeConstantPool constantPool = frame.getMethod().getClazz().getRuntimeConstantPool();
        // 得到类的ClassRef
        ClassRef classRef = (ClassRef) constantPool.getConstant(this.index);
        try{
            // 得到目标类
            JClass targetClass = classRef.getResolvedClass();
            // 若类为接口或抽象类， throw InstantiationError
            if(targetClass.isAbstract()||targetClass.isInterface()) throw new InstantiationException();
            // 若类没有被初始化 初始化
            if(targetClass.getInitState()!= InitState.PREPARED){
                targetClass.initClass(frame.getThread(),targetClass);
            }
            // 将新实例分配到GC堆
            NonArrayObject newObject = new NonArrayObject(targetClass);
            JHeap heap = JHeap.getInstance();
            heap.addObj(newObject);
            // push the objectRef
            OperandStack opStack = frame.getOperandStack();
            opStack.pushObjectRef(newObject);
        }catch(ClassNotFoundException | InstantiationException e){
            e.printStackTrace();
        }
    }
}
