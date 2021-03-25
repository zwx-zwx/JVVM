package com.njuse.seecjvm.instructions.references;

import com.njuse.seecjvm.instructions.base.Index16Instruction;
import com.njuse.seecjvm.memory.jclass.Field;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.RuntimeConstantPool;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref.FieldRef;
import com.njuse.seecjvm.runtime.OperandStack;
import com.njuse.seecjvm.runtime.StackFrame;
import com.njuse.seecjvm.runtime.Vars;
import com.njuse.seecjvm.runtime.struct.JObject;
import com.njuse.seecjvm.runtime.struct.NonArrayObject;
import jdk.nashorn.internal.scripts.JO;


public class PUTFIELD extends Index16Instruction {
    /**
     * TODO 实现这条指令
     * 其中 对应的index已经读取好了
     * 目标： 将objectref指向的对象中的index指向的字段设为预计值
     */
    @Override
    public void execute(StackFrame frame) {
        /* index 指向 field
           objectref 指对象
         */

        // 从当前类的常量池中找field
        RuntimeConstantPool constantPool = frame.getMethod().getClazz().getRuntimeConstantPool();
        FieldRef fieldRef = (FieldRef) constantPool.getConstant(this.index);
        try {
            // 得到当前类
            Field field = fieldRef.getResolvedFieldRef();

            // 静态抛出IncompatibleClassChangeError
            if(field.isStatic()) throw new IncompatibleClassChangeError();
            // final 字段 只能在当前类的<init>合法
            String currentName = field.getClazz().getName();
            String initName = frame.getMethod().getClazz().getName();
            if(field.isFinal()&&
                !(currentName.equals(initName)&&frame.getMethod().getName().equals("<init>")))
                throw new IllegalAccessException();

            // 根据描述符，操作数栈中的object 和 value 设置类中的值
            setFieldOfObject(field, frame.getOperandStack());

        }catch(ClassNotFoundException | IllegalAccessException e){
            e.printStackTrace();
        }
    }

    private void setFieldOfObject(Field field, OperandStack opStack){
        // 得到描述符和slotID
        int slotID = field.getSlotID();
        String descriptor = field.getDescriptor();
        switch(descriptor){
            case"I":
            case"S":
            case"C":
            case"B":
            case"Z":
                int value = opStack.popInt();
                NonArrayObject object = (NonArrayObject) opStack.popObjectRef();
                // 空指针
                if(object == null) throw new NullPointerException();
                Vars fields = object.getFields();
                fields.setInt(slotID, value);
                break;
            case"J":
                long valueL = opStack.popLong();
                NonArrayObject objectL = (NonArrayObject) opStack.popObjectRef();
                if(objectL == null) throw new NullPointerException();
                Vars fieldsL = objectL.getFields();
                fieldsL.setLong(slotID, valueL);
                break;
            case"F":
                float valueF = opStack.popFloat();
                NonArrayObject objectF = (NonArrayObject) opStack.popObjectRef();
                if(objectF == null) throw new NullPointerException();
                Vars fieldsF = objectF.getFields();
                fieldsF.setFloat(slotID, valueF);
                break;
            case"D":
                double valueD = opStack.popDouble();
                NonArrayObject objectD = (NonArrayObject) opStack.popObjectRef();
                if(objectD == null) throw new NullPointerException();
                Vars fieldsD = objectD.getFields();
                fieldsD.setDouble(slotID, valueD);
                break;
            case"L":
                JObject objectRef = opStack.popObjectRef();
                NonArrayObject objectR = (NonArrayObject) opStack.popObjectRef();
                if(objectR == null) throw new NullPointerException();
                Vars fieldsR = objectR.getFields();
                fieldsR.setObjectRef(slotID, objectRef);
                break;
            default:
                break;
        }
    }
}
