package com.voltron.router.gradle.autowired.preprocess;

import com.voltron.router.gradle.utils.Logger;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.ALOAD;

/**
 * This class is used to check if a subclass of android.app.Activity which contains @Autowired
 * annotated fields has override onCreate() method or not. If not, this visitor will add an onCreate()
 * method which contains only a call to super.onCreate() to the class. This check happens before
 * the insertion of the VRouter.inject() call, because that has to be inserted inside onCreate()
 *
 * 这个类的作用是，检查包含了被 @Autowired 注解的字段的 Activity 是否已经覆盖了父类的 onCreate() 方法。如果没有，
 * 自动加一个 onCreate() 方法给这个类，方法体只有一句对 super.onCreate() 的调用。这个类发挥作用是在 VRouter.inject()
 * 被自动插入之前，因为这个插入需要插入到 onCreate() 方法里。
 */
public class PreProcessAutowiredClassVisitor extends ClassVisitor {

    private boolean hasOnCreateMethod = false;
    private ClassWriter cw;
    private String superName;

    public PreProcessAutowiredClassVisitor(ClassWriter cv) {
        super(Opcodes.ASM5, cv);
        this.cw = cv;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.superName = superName;
        Logger.i("PreProcessAutowiredClassVisitor visit, name: " + name + ", superName: " + superName);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        if ("onCreate".equals(name) && "(Landroid/os/Bundle;)V".equals(desc)) {
            hasOnCreateMethod = true;
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        if (!hasOnCreateMethod) { // if there isn't an onCreate method, create one.
            MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "onCreate", "(Landroid/os/Bundle;)V", null, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, superName, "onCreate", "(Landroid/os/Bundle;)V", false);
            mv.visitMaxs(2, 2);
            mv.visitInsn(Opcodes.RETURN);
            mv.visitEnd();
        }
        super.visitEnd();
    }
}
