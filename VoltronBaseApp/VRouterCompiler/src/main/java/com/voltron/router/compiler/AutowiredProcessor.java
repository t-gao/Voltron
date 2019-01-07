package com.voltron.router.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.voltron.router.annotation.Autowired;
import com.voltron.router.base.TypeKind;
import com.voltron.router.compiler.utils.Logger;
import com.voltron.router.compiler.utils.TypeUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.voltron.router.compiler.Constants.ACTIVITY;
import static com.voltron.router.compiler.Constants.FRAGMENT;
import static com.voltron.router.compiler.Constants.FRAGMENT_V4;
import static com.voltron.router.compiler.Constants.METHOD_INJECT;
import static com.voltron.router.compiler.Constants.NAME_OF_AUTOWIRED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@AutoService(Processor.class)
public class AutowiredProcessor extends AbstractProcessor {

    // File util, write class file into disk.
    private Filer mFiler;

    private Logger logger;
    private Types types;
    private TypeUtils typeUtils;
    private Elements elements;

    // Contain field need autowired and his super class.
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnv.getFiler();                  // Generate class.
        types = processingEnv.getTypeUtils();            // Get type utils.
        elements = processingEnv.getElementUtils();      // Get class meta.
        typeUtils = new TypeUtils(types, elements);
        logger = new Logger(processingEnv.getMessager());   // Package the log utils.

        logger.i("AutowiredProcessor init. <<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        logger.i("AutowiredProcessor start process...");
        if (CollectionUtils.isNotEmpty(set)) {
            try {
                logger.i("Found autowired field, start... <<<");
                categories(roundEnvironment.getElementsAnnotatedWith(Autowired.class));
                processElements();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    /**
     * process Elements to create a file that can recieve the param automatically
     * @throws IllegalAccessException
     * @throws IOException
     */
    private void processElements() throws IllegalAccessException, IOException {
        TypeMirror activityTm = elements.getTypeElement(Constants.TypeName.ACTIVITY).asType();
        TypeMirror fragmentTm = elements.getTypeElement(Constants.TypeName.FRAGMENT).asType();
        TypeMirror fragmentTmV4 = elements.getTypeElement(Constants.TypeName.FRAGMENT_V4).asType();
        //构造生成的方法的参数，Object target （实际上是Activity或者Fragment）
        ParameterSpec objectParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build();

        if (MapUtils.isNotEmpty(parentAndChild)) {
            logger.i("parentAndChild not empty");
            for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {
                TypeElement parent = entry.getKey();
                List<Element> childs = entry.getValue();

                String qualifiedName = parent.getQualifiedName().toString();
                //生成类文件的包名
                String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));

                //生成文件命名规则：类名 + __Autowired
                String fileName = parent.getSimpleName() + NAME_OF_AUTOWIRED;

                //创建public static inject(){ } 方法
                MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder(METHOD_INJECT)
                        .addModifiers(PUBLIC , STATIC)
                        .addParameter(objectParamSpec);

                //类名public
                TypeSpec.Builder helper = TypeSpec.classBuilder(fileName)
                        .addModifiers(PUBLIC);

                //生命Activity对象
                injectMethodBuilder.addStatement("$T substitute = ($T)target", ClassName.get(parent), ClassName.get(parent));

                // Generate method body, start inject.
                for (Element element : childs) {
                    Autowired fieldConfig = element.getAnnotation(Autowired.class);
                    String fieldName = element.getSimpleName().toString();
                    String originalValue = "substitute." + fieldName;
                    String statement = "substitute." + fieldName + " = " + buildCastCode(element) + "substitute.";
                    boolean isActivity = false;
                    if (types.isSubtype(parent.asType(), activityTm)) {  // Activity, then use getIntent()
                        isActivity = true;
                        statement += "getIntent().";
                    } else if (types.isSubtype(parent.asType(), fragmentTm) || types.isSubtype(parent.asType(), fragmentTmV4)) {   // Fragment, then use getArguments()
                        statement += "getArguments().";
                    } else {
                        throw new IllegalAccessException("The field [" + fieldName + "] need autowired from intent, its parent must be activity or fragment!");
                    }

                    statement = buildStatement(originalValue, statement, typeUtils.typeExchange(element), isActivity);
                    injectMethodBuilder.addStatement(statement, StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name());
                    // Validator
                    if (fieldConfig.required() && !element.asType().getKind().isPrimitive()) {  // Primitive wont be check.
                        injectMethodBuilder.beginControlFlow("if (null == substitute." + fieldName + ")");
                        injectMethodBuilder.endControlFlow();
                    }
                }

                helper.addMethod(injectMethodBuilder.build());

                // Generate autowire helper
                JavaFile.builder(packageName, helper.build()).build().writeTo(mFiler);

                logger.i("Start process " + childs.size() + " field in " + fileName +" , packageName = "+ packageName +  " ... <<<");
            }
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported() ;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Autowired.class.getCanonicalName());
    }

    private String buildCastCode(Element element) {
        if (typeUtils.typeExchange(element) == TypeKind.SERIALIZABLE.ordinal()) {
            return CodeBlock.builder().add("($T) ", ClassName.get(element.asType())).build().toString();
        }
        return "";
    }

    /**
     * create the code for file
     * @param originalValue
     * @param statement
     * @param type
     * @param isActivity
     * @return
     */
    private String buildStatement(String originalValue, String statement, int type, boolean isActivity) {
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statement += (isActivity ? ("getBooleanExtra($S, " + originalValue + ")") : ("getBoolean($S)"));
        } else if (type == TypeKind.BYTE.ordinal()) {
            statement += (isActivity ? ("getByteExtra($S, " + originalValue + ")") : ("getByte($S)"));
        } else if (type == TypeKind.SHORT.ordinal()) {
            statement += (isActivity ? ("getShortExtra($S, " + originalValue + ")") : ("getShort($S)"));
        } else if (type == TypeKind.INT.ordinal()) {
            statement += (isActivity ? ("getIntExtra($S, " + originalValue + ")") : ("getInt($S)"));
        } else if (type == TypeKind.LONG.ordinal()) {
            statement += (isActivity ? ("getLongExtra($S, " + originalValue + ")") : ("getLong($S)"));
        }else if(type == TypeKind.CHAR.ordinal()){
            statement += (isActivity ? ("getCharExtra($S, " + originalValue + ")") : ("getChar($S)"));
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statement += (isActivity ? ("getFloatExtra($S, " + originalValue + ")") : ("getFloat($S)"));
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statement += (isActivity ? ("getDoubleExtra($S, " + originalValue + ")") : ("getDouble($S)"));
        } else if (type == TypeKind.STRING.ordinal()) {
            statement += (isActivity ? ("getStringExtra($S)") : ("getString($S)"));
        } else if (type == TypeKind.SERIALIZABLE.ordinal()) {
            statement += (isActivity ? ("getSerializableExtra($S)") : ("getSerializable($S)"));
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            statement += (isActivity ? ("getParcelableExtra($S)") : ("getParcelable($S)"));
        }

        return statement;
    }

    /**
     * save all of annotation with Autowired ,
     * the local fields need to be define as public or non
     *
     * @param elements Field need autowired
     */
    private void categories(Set<? extends Element> elements) throws IllegalAccessException {
        if (CollectionUtils.isNotEmpty(elements)) {
            for (Element element : elements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new IllegalAccessException("The inject fields CAN NOT BE 'private'!!! please check field ["
                            + element.getSimpleName() + "] in class [" + enclosingElement.getQualifiedName() + "]");
                }

                if (parentAndChild.containsKey(enclosingElement)) { // Has categries
                    parentAndChild.get(enclosingElement).add(element);
                } else {
                    List<Element> childs = new ArrayList<>();
                    childs.add(element);
                    parentAndChild.put(enclosingElement, childs);
                }
            }

            logger.i("categories finished.");
        }
    }
}