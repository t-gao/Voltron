package com.voltron.router.compiler;

import com.google.auto.service.AutoService;
import com.voltron.router.annotation.EndPoint;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class RouteEndPointProcessor extends AbstractProcessor {

    private Messager messager;
    private Elements elementUtils;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(EndPoint.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "VOLTRON Router Compiler - process");
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(EndPoint.class);
        if (elements != null && !elements.isEmpty()) {
            for (Element element : elements) {
                messager.printMessage(Diagnostic.Kind.NOTE, "VOLTRON Router Compiler - Processing Annotated Element: ", element);
                generateSource(element);
            }
        }
        return true;
    }

    private void generateSource(Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            try {
                EndPoint endPointAnno = element.getAnnotation(EndPoint.class);
                String annoVal = endPointAnno.value();
                String clsName = ((TypeElement)element).getQualifiedName().toString() + "$$Generated";
                JavaFileObject javaFileObject = filer.createSourceFile(clsName, element);
                Writer writer = javaFileObject.openWriter();

                String simpleName = element.getSimpleName().toString() + "$$Generated";
//                PackageElement packageElement = elementUtils.getPackageOf(element);
                String packageName = "com.voltron.router.routes";//packageElement.getQualifiedName().toString();

                writer
                        .append("package ").append(packageName).append(";\n\n")
                        .append("public class ").append(simpleName).append(" {\n")
                        .append("    public static String getAnnoVal() {\n")
                        .append("        return ").append("\"").append(annoVal).append("\";\n")
                        .append("    }\n")
                        .append("}");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
            }

        }
    }
}
