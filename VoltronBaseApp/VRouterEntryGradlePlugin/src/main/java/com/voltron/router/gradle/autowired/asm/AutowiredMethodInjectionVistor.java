package com.voltron.router.gradle.autowired.asm;

import com.voltron.router.gradle.utils.Logger;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

public class AutowiredMethodInjectionVistor extends AdviceAdapter {
    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param mv     the method visitor to which this adapter delegates calls.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name   the method's name.
     * @param desc   the method's descriptor (see {@link Type Type}).
     */
    protected AutowiredMethodInjectionVistor(MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM5, mv, access, name, desc);
    }

    @Override
    protected void onMethodEnter() {
        Logger.i("AutowiredMethodInjectionVistor onMethodEnter");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESTATIC, "com/voltron/router/api/VRouter", "inject", "(Ljava/lang/Object;)V", false);
//        mv.visitMethodInsn(INVOKESTATIC, "com.voltron.router.base/AnnotationUtil", "autowiredInject", "(Ljava/lang/Object;)V", false);
    }
}
