package com.voltron.router.gradle.entrycreator;

import com.android.SdkConstants;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;
import com.voltron.router.gradle.utils.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.json.JSONObject;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class VRouterEntryCreatorTransform extends Transform {

    private static final String GENERATED_PACKAGE = "com.voltron.router.routes";

    public static final String GENERATED_PACKAGE_PATH = GENERATED_PACKAGE.replace('.', '/');
    public static final String GENERATED_PACKAGE_DIR = GENERATED_PACKAGE.replace('.', File.separatorChar);

    @Override
    public String getName() {
        return "VRouterEntryCreator";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_JARS;
    }

    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        Logger.i("VRouterEntryCreatorTransform transform BEGIN...");

        Set<String> initClasses = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

        for (TransformInput input : transformInvocation.getInputs()) {
            Logger.i("Looping TransformInput");

            for (JarInput jarInput : input.getJarInputs()) {
//            input.getJarInputs().parallelStream().forEach(jarInput -> {
                File src = jarInput.getFile();
                File dst = transformInvocation.getOutputProvider().getContentLocation(
                        jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(),
                        Format.JAR);

                Logger.i("Traverse JarInput: name: " + jarInput.getName() + ", src: " + src.getAbsolutePath() + ", det: " + dst.getAbsolutePath());

                try {
                    scanJarFile(src, initClasses);
                    FileUtils.copyFile(src, dst);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//            });
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
//            input.getDirectoryInputs().parallelStream().forEach(directoryInput -> {
                File src = directoryInput.getFile();
                File dst = transformInvocation.getOutputProvider().getContentLocation(
                        directoryInput.getName(), directoryInput.getContentTypes(),
                        directoryInput.getScopes(), Format.DIRECTORY);

                Logger.i("Traverse DirectoryInput: name: " + directoryInput.getName() + ", src: " + src.getAbsolutePath() + ", det: " + dst.getAbsolutePath());

                try {
                    scanDir(src, initClasses);
                    FileUtils.copyDirectory(src, dst);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//            });
            }
        }

        File dest = transformInvocation.getOutputProvider().getContentLocation(
                "VRouterEntryCreator", TransformManager.CONTENT_CLASS,
                ImmutableSet.of(QualifiedContent.Scope.PROJECT), Format.DIRECTORY);
        generateVRouterEntryClass(dest.getAbsolutePath(), initClasses);

        Logger.i("VRouterEntryCreatorTransform transform END");
    }

    private void scanJarFile(File file, Set<String> initClasses) throws IOException {
        Logger.i("scanJarFile, file: " + file.getAbsolutePath());

        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            Logger.i("  checking class: " + name);

            if (name.endsWith(SdkConstants.DOT_CLASS) && name.startsWith(GENERATED_PACKAGE_PATH)) {
                String className = trimName(name, 0).replace('/', '.');
                initClasses.add(className);

                Logger.i("    find generated class: " + className);
            }
        }
    }

    private void scanDir(File dir, Set<String> initClasses) throws IOException {
        Logger.i("scanDir, dir: " + dir.getAbsolutePath());

        File packageDir = new File(dir, GENERATED_PACKAGE_DIR);
        if (packageDir.exists() && packageDir.isDirectory()) {
            Collection<File> files = FileUtils.listFiles(packageDir,
                    new SuffixFileFilter(SdkConstants.DOT_CLASS, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
            for (File f : files) {
                Logger.i("  checking file: " + f.getAbsolutePath());

                String className = trimName(f.getAbsolutePath(), dir.getAbsolutePath().length() + 1)
                        .replace(File.separatorChar, '.');
                initClasses.add(className);

                Logger.i("    find generated class: " + className);
            }
        }
    }

    /**
     * [prefix]com/xxx/aaa.class --> com/xxx/aaa
     * [prefix]com\xxx\aaa.class --> com\xxx\aaa
     */
    private String trimName(String s, int start) {
        return s.substring(start, s.length() - SdkConstants.DOT_CLASS.length());
    }

    /**
     * 生成格式如下的代码，其中ServiceInit_xxx由注解生成器生成。
     * <pre>
     * package com.sankuai.waimai.router.generated;
     *
     * public class VRouterEntry {
     *
     *     public static void init() {
     *         ServiceInit_xxx1.init();
     *         ServiceInit_xxx2.init();
     *     }
     * }
     * </pre>
     */
    private void generateVRouterEntryClass(String directory, Set<String> classes) {

        Logger.i("generateVRouterEntryClass, directory: " + directory);
        if (classes.isEmpty()) {
            Logger.i("skipped, no service found");
            return;
        }

        try {
            Logger.i("start...");
            long ms = System.currentTimeMillis();

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, writer) {};
            String className = GENERATED_PACKAGE_PATH + "/VRouterEntry";
            cv.visit(50, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);

            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                    "init", "(Ljava/util/ArrayList;)V", "(Ljava/util/ArrayList<Ljava/lang/String;>;)V", null);

            mv.visitCode();

//            mv.visitIntInsn();
            for (String clazz : classes) {
                String owner = clazz.replace('.', '/');
                Logger.i("  visiting method myName() of " + owner);

                mv.visitVarInsn(Opcodes.ALOAD, 0);

                mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                        owner,
                        "myName",
                        "()Ljava/lang/String;",
                        false);

                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                        "java/util/ArrayList",
                        "add",
                        "(Ljava/lang/Object;)Z",
                        false);

                mv.visitInsn(Opcodes.POP);
            }

            mv.visitMaxs(0, 0);
            mv.visitInsn(Opcodes.RETURN);
//            mv.visitLdcInsn("helllo");
//            mv.visitInsn(Opcodes.ARETURN);
            mv.visitEnd();
            cv.visitEnd();

            File dest = new File(directory, className + SdkConstants.DOT_CLASS);
            dest.getParentFile().mkdirs();
            new FileOutputStream(dest).write(writer.toByteArray());

            Logger.i("end, cost " + (System.currentTimeMillis() - ms) + " ms");

        } catch (IOException e) {
            e.printStackTrace();
            // TODO: 2018/12/25 handle exception, maybe abort building
        }
    }

//    /**
//     * 生成格式如下的代码，其中ServiceInit_xxx由注解生成器生成。
//     * <pre>
//     * package com.sankuai.waimai.router.generated;
//     *
//     * public class VRouterEntry {
//     *
//     *     public static void init() {
//     *         ServiceInit_xxx1.init();
//     *         ServiceInit_xxx2.init();
//     *     }
//     * }
//     * </pre>
//     */
//    private void generateVRouterEntryClass(String directory, Set<String> classes) {
//
//        Logger.i("generateVRouterEntryClass, directory: " + directory);
//        if (classes.isEmpty()) {
//            Logger.i("skipped, no service found");
//            return;
//        }
//
//        try {
//            Logger.i("start...");
//            long ms = System.currentTimeMillis();
//
//            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
//            ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, writer) {};
//            String className = GENERATED_PACKAGE_PATH + "/VRouterEntry";
//            cv.visit(50, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
//
//            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
//                    "init", "()Ljava/lang/String;", null, null);
//
//            mv.visitCode();
//
////            mv.visitIntInsn();
//            for (String clazz : classes) {
//                String owner = clazz.replace('.', '/');
//                Logger.i("  visiting method load() of " + owner);
//                mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner,
//                        "load",
//                        "()Ljava/lang/String;",
//                        false);
//            }
//
//            mv.visitMaxs(0, 0);
////            mv.visitInsn(Opcodes.RETURN);
//            mv.visitLdcInsn("helllo");
//            mv.visitInsn(Opcodes.ARETURN);
//            mv.visitEnd();
//            cv.visitEnd();
//
//            File dest = new File(directory, className + SdkConstants.DOT_CLASS);
//            dest.getParentFile().mkdirs();
//            new FileOutputStream(dest).write(writer.toByteArray());
//
//            Logger.i("end, cost " + (System.currentTimeMillis() - ms) + " ms");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            // TODO: 2018/12/25 handle exception, maybe abort building
//        }
//    }
}
