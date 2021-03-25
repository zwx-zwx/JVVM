package com.njuse.seecjvm.memory.jclass.runtimeConstantPool.constant.ref;

import com.njuse.seecjvm.classloader.classfileparser.constantpool.info.MethodrefInfo;
import com.njuse.seecjvm.memory.jclass.JClass;
import com.njuse.seecjvm.memory.jclass.Method;
import com.njuse.seecjvm.memory.jclass.runtimeConstantPool.RuntimeConstantPool;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class MethodRef extends MemberRef {
    private Method method;

    public MethodRef(RuntimeConstantPool runtimeConstantPool, MethodrefInfo methodrefInfo) {
        super(runtimeConstantPool, methodrefInfo);
    }

    /**
     * TODO：实现这个方法
     * 这个方法用来实现对象方法的动态查找
     * 方法：从该类到父类，所有的接口中找到方法
     *
     * @param clazz 对象的引用
     *
     */
    public Method resolveMethodRef(JClass clazz) {
        // 从继承树中查找
        Optional targetMethod;
        for(JClass now = this.clazz; now!=null; now = now.getSuperClass()){
            targetMethod = now.resolveMethod(this.name,this.descriptor);
            if(targetMethod.isPresent()){
                this.method = (Method)targetMethod.get();
                return this.method;
            }
        }
        // 接口中查找
        JClass[] interfaces = clazz.getInterfaces();
        for(int index=0; index<interfaces.length;index++){
            targetMethod = interfaces[index].resolveMethod(this.name,this.descriptor);
            if(targetMethod.isPresent()){
                this.method = (Method) targetMethod.get();
                return this.method;
            }
        }
        return null;
    }

    /**
     * TODO: 实现这个方法
     * 这个方法用来解析methodRef对应的方法
     * 与上面的动态查找相比，这里的查找始终是从这个Ref对应的class开始查找的
     */
    public Method resolveMethodRef() {
        if(this.method==null){
            try{
                this.resolveClassRef();
                this.resolveMethodRef(this.clazz);
            }catch(ClassNotFoundException e){
                e.printStackTrace();;
            }
        }
        return this.method;
    }


    @Override
    public String toString() {
        return "MethodRef to " + className;
    }
}
