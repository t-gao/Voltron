package com.voltron.router.gradle.autowired;

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
import com.voltron.router.gradle.autowired.asm.AutowiredClassVisitor;
import com.voltron.router.gradle.utils.Logger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class AutowiredTransform extends Transform {
    private static final String AUTOWIRED_SUFFIX = "__Autowired";
    private static final String AUTOWIRED_CLASS_SUFFIX = AUTOWIRED_SUFFIX + SdkConstants.DOT_CLASS;

    @Override
    public String getName() {
        return "VRouterAutowiredTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
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
        Logger.i("VRouterAutowiredTransform transform BEGIN...");

        Set<String> annotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
        findAutowiredOriginalClasses(transformInvocation, annotatedClasses);

        Logger.i("FOUND autowired annotated classes: ");
        for (String annoCls : annotatedClasses) {
            Logger.i(annoCls);
        }

        for (TransformInput input : transformInvocation.getInputs()) {
            Logger.i("Looping TransformInput");

            for (JarInput jarInput : input.getJarInputs()) {
                File src = jarInput.getFile();

                String hexName = DigestUtils.md5Hex(src.getAbsolutePath()).substring(0, 8);
                File dst = transformInvocation.getOutputProvider().getContentLocation(
                        src.getName() + "_" + hexName, jarInput.getContentTypes(),
                        jarInput.getScopes(), Format.JAR);

                Logger.i("Autowired Traverse JarInput: name: " + jarInput.getName()
                        + ", src: " + src.getAbsolutePath() + ", dest: " + dst.getAbsolutePath());

                File modifiedJar = null;
                if (jarNeedModification(src, annotatedClasses)) {
                    modifiedJar = modifyJar(src, transformInvocation.getContext().getTemporaryDir(), annotatedClasses);
                }
                if (modifiedJar == null) {
                    modifiedJar = src;
                }
                try {
                    FileUtils.copyFile(modifiedJar, dst);
                } catch (IOException e) {
                    Logger.e("VRouterAutowiredTransform transform, error: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File src = directoryInput.getFile();

                File dst = transformInvocation.getOutputProvider().getContentLocation(
                        directoryInput.getName(), directoryInput.getContentTypes(),
                        directoryInput.getScopes(), Format.DIRECTORY);

                Logger.i("Autowired Traverse DirectoryInput: name: " + directoryInput.getName()
                        + ", src: " + src.getAbsolutePath() + ", dest: " + dst.getAbsolutePath());

                Collection<File> files = FileUtils.listFiles(src,
                        new SuffixFileFilter(SdkConstants.DOT_CLASS, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
                HashMap<String, File> modifiedMap = new HashMap<>();
                for (File f : files) {

                    String className = f.getAbsolutePath()
                            .substring(src.getAbsolutePath().length() + 1,
                                    f.getAbsolutePath().length() - SdkConstants.DOT_CLASS.length())
                            .replace(File.separatorChar, '.');
                    File modifiedClassFile = null;
                    Logger.i("check class in dir, classFile: " + f.getAbsolutePath() + ", className: " + className);

                    if (annotatedClasses.contains(className)) {
                        modifiedClassFile = modifyClassFile(src, f, transformInvocation.getContext().getTemporaryDir());
                        if (modifiedClassFile == null) {
                            Logger.w("FAILED TO AUTO INJECT autowired for: " + className);
                        }
                    }
                    if (modifiedClassFile != null) {
                        modifiedMap.put(f.getAbsolutePath().replace(src.getAbsolutePath(), ""), modifiedClassFile);
                    }
                }

                try {
                    FileUtils.copyDirectory(src, dst);

                    for (Map.Entry<String, File> entry : modifiedMap.entrySet()) {
                        File target = new File(dst.getAbsolutePath() + entry.getKey());
                        if (target.exists()) {
                            target.delete();
                        }
                        FileUtils.copyFile(entry.getValue(), target);
                        entry.getValue().delete();
                    }
                } catch (IOException e) {
                    Logger.e("VRouterAutowiredTransform transform, error: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private byte[] getModifiedClassBytes(byte[] sourceClassBytes, String className) {
        Logger.i("getModifiedClassBytes, className: " + className);
        if (sourceClassBytes == null) {
            return null;
        }
        Logger.i("    sourceClassBytes length: " + sourceClassBytes.length);
        try {
            ClassReader cr = new ClassReader(sourceClassBytes);
            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
            AutowiredClassVisitor cv = new AutowiredClassVisitor(cw);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            byte[] modifiedClassBytes = cw.toByteArray();
            Logger.i("returning modifiedClassBytes, length: " + (modifiedClassBytes == null ? 0 : modifiedClassBytes.length));
            return modifiedClassBytes;
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e("getModifiedClassBytes, error: " + e.getMessage());
        }
        return null;
    }

    private File modifyClassFile(File dir, File classFile, File temporaryDir) {
        Logger.i("modifyClassFile: " + classFile.getAbsolutePath());
        File modified = null;
        try {
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile));
            byte[] modifiedClassBytes = getModifiedClassBytes(sourceClassBytes, classFile.getAbsolutePath());
            if (modifiedClassBytes != null) {
                String className = classFile.getAbsolutePath()
                        .replace(dir.getAbsolutePath() + File.separator, "")
                        .replace(File.separator, ".").replace(".class", "");

                modified = new File(temporaryDir, className.replace(".", "") + ".class");
                if (modified.exists()) {
                    modified.delete();
                }
                modified.createNewFile();
                new FileOutputStream(modified).write(modifiedClassBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e("modifyClassFile, error: " + e.getMessage());
            modified = null;
        }
        return modified;
    }

    private void findAutowiredOriginalClasses(TransformInvocation transformInvocation, Set<String> annotatedClasses) {

        Logger.i("findAutowiredOriginalClasses");
        for (TransformInput input : transformInvocation.getInputs()) {
            Logger.i("Looping TransformInput");

            for (JarInput jarInput : input.getJarInputs()) {
                File src = jarInput.getFile();
                try {
                    scanJarFile(src, annotatedClasses);
                } catch (IOException e) {
                    Logger.e("findAutowiredOriginalClasses, error: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }

            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                File src = directoryInput.getFile();

                try {
                    scanDir(src, annotatedClasses);
                } catch (IOException e) {
                    Logger.e("findAutowiredOriginalClasses, error: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private File modifyJar(File file, File temporaryDir, Set<String> annotatedClasses) {
        String fileAbsolutePath = file == null ? "" : file.getAbsolutePath();
        Logger.i("modifyJar, fileAbsolutePath: " + fileAbsolutePath);

        if (annotatedClasses == null || annotatedClasses.isEmpty() || file == null) {
            return null;
        }

        String hexName = DigestUtils.md5Hex(fileAbsolutePath).substring(0, 8);
        File optJar = new File(temporaryDir, hexName + file.getName());
        try {
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar));

            JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                InputStream inputStream = jarFile.getInputStream(entry);
                ZipEntry zipEntry = new ZipEntry(entryName);
                jarOutputStream.putNextEntry(zipEntry);

                byte[] modifiedClassBytes = null;
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream);
                if (entryName.endsWith(SdkConstants.DOT_CLASS)) {
                    String className = entryName.replace(File.separator, ".").replace(".class", "");
                    if (annotatedClasses.contains(className)) {
                        modifiedClassBytes = getModifiedClassBytes(sourceClassBytes, className);
                    }
                }
                if (modifiedClassBytes == null) {
                    jarOutputStream.write(sourceClassBytes);
                } else {
                    jarOutputStream.write(modifiedClassBytes);
                }
                jarOutputStream.closeEntry();
            }
            jarOutputStream.close();
            jarFile.close();
            return optJar;
        } catch (IOException e) {
            Logger.e("modifyJar, error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private boolean jarNeedModification(File file, Set<String> annotatedClasses) {
        Logger.i("jarNeedModification, file: " + (file == null ? "" : file.getAbsolutePath()));
        boolean needModification = false;
        if (file != null && annotatedClasses != null && !annotatedClasses.isEmpty()) {
            try {
                JarFile jarFile = new JarFile(file);
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    if (name.endsWith(SdkConstants.DOT_CLASS)) {
                        String className = name.substring(0, name.length() - SdkConstants.DOT_CLASS.length()).replace(File.separatorChar, '.');
                        if (annotatedClasses.contains(className)) {
                            needModification = true;
                            break;
                        }

                    }
                }
                jarFile.close();
            } catch (IOException e) {
                Logger.e("jarNeedModification, error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        Logger.i("jarNeedModification, needModification: " + needModification);
        return needModification;
    }

    private void scanJarFile(File file, Set<String> classes) throws IOException {
        Logger.i("scanJarFile, file: " + file.getAbsolutePath());

        JarFile jarFile = new JarFile(file);
        Enumeration<JarEntry> entries = jarFile.entries();
        String autowiredClassSuffix = AUTOWIRED_CLASS_SUFFIX;
        int autowiredClassSuffixLen = autowiredClassSuffix.length();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            Logger.i("  checking class: " + name);

            if (name.endsWith(autowiredClassSuffix)) {
                String className = name.substring(0, name.length() - autowiredClassSuffixLen).replace(File.separatorChar, '.');
                classes.add(className);

                Logger.i("     in jar, found Autowired annotated class: " + className);
            }
        }
    }

    private void scanDir(File dir, Set<String> classes) throws IOException {
        Logger.i("scanDir, dir: " + dir.getAbsolutePath());

        String autowiredClassSuffix = AUTOWIRED_CLASS_SUFFIX;
        int autowiredClassSuffixLen = autowiredClassSuffix.length();

        Collection<File> files = FileUtils.listFiles(dir,
                new SuffixFileFilter(autowiredClassSuffix, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
        for (File f : files) {
            Logger.i("  checking file: " + f.getAbsolutePath());

            String className = f.getAbsolutePath()
                    .substring(dir.getAbsolutePath().length() + 1,
                            f.getAbsolutePath().length() - autowiredClassSuffixLen)
                    .replace(File.separatorChar, '.');

            classes.add(className);

            Logger.i("     in dir, found Autowired annotated class: " + className);
        }
    }

}
