package com.voltron.router.gradle.autowired.asm;

import com.voltron.router.gradle.utils.Logger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AutowiredClassVisitor extends ClassVisitor {
    public AutowiredClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
//        MethodVisitor mv = this.cv.visitMethod(access, name, desc, signature, exceptions);
        if ("onCreate".equals(name) && "(Landroid/os/Bundle;)V".equals(desc)) {
            Logger.i("AutowiredClassVisitor visiting onCreate method");
            return new AutowiredMethodInjectionVistor(mv, access, name, desc);
        } else {
            return mv;
        }
    }
}
