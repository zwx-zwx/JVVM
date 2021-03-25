package com.njuse.seecjvm.instructions.references;

import com.njuse.seecjvm.instructions.base.Index16Instruction;
import com.njuse.seecjvm.memory.jclass.Field;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.RuntimeConstantPool;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.FieldRef;
import com.njuse.seecjvm.runtime.OperandStack;
import com.njuse.seecjvm.runtime.StackFrame;
import com.njuse.seecjvm.runtime.Vars;
import com.njuse.seecjvm.runtime.struct.NonArrayObject;

public class GETFIELD extends Index16Instruction {

    /**
     * TODO 实现这条指令
     * 其中 对应的index已经读取好了
     * 目的：从堆中的相应对象 得到 指定字段的值，并压入栈中
     */
    @Override
    public void execute(StackFrame frame) {
        // pop the objectref
        OperandStack opStack = frame.getOperandStack();
        NonArrayObject object = (NonArrayObject) opStack.popObjectRef();

        // if object is null, throw NullPointerException
        if(object == null) throw new NullPointerException();

        try{
            // get the target field
            Field targetField = getField(frame.getMethod().getClazz(),this.index);

            // if field is static, throw IncompatibleClassChangeError
            if(targetField.isStatic()) throw new IncompatibleClassChangeError();

            // push the value according to the field and object
            pushTheValue(object, targetField, opStack);
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    private Field getField(JClass currentClass, int index) throws ClassNotFoundException {
        // get RTCP
        RuntimeConstantPool constantPool = currentClass.getRuntimeConstantPool();
        // get Fieldref
        FieldRef ref = (FieldRef) constantPool.getConstant(index);
        // resolve ref to get field
        Field targetField = ref.getResolvedFieldRef();
        // return
        return targetField;
    }

    private void pushTheValue(NonArrayObject object, Field field, OperandStack opStack){
        // get the slotID & descriptor
        int slotID = field.getSlotID();
        String descriptor = field.getDescriptor();

        // search the object
        Vars fields = object.getFields();
        switch(descriptor){
            // int short byte char boolean use pushInt
            case"I":
            case"S":
            case"B":
            case"C":
            case"Z":
                opStack.pushInt(fields.getInt(slotID));
                break;
            // long
            case"J":
                opStack.pushLong(fields.getLong(slotID));
                break;
            case"F":
                opStack.pushFloat(fields.getFloat(slotID));
            // double
            case"D":
                opStack.pushDouble(fields.getDouble(slotID));
                break;
            // object
            case"L":
                opStack.pushObjectRef(fields.getObjectRef(slotID));
                break;
            default:
                break;
        }
    }
}
