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
import com.voltron.router.base.AnnotationConsts;
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
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@AutoService(Processor.class)
public class AutowiredProcessor extends AbstractProcessor {

    private String moduleName = null;

    // File util, write class file into disk.
    private Filer mFiler;

    private Logger logger;
    private Types types;
    private TypeUtils typeUtils;
    private Elements elementUtils;

    private Constants.PrivateAutowiredPolicy privateAutowiredPolicy = Constants.PrivateAutowiredPolicy.ABORT;

    // Contain field need autowiored and his super class.
    private Map<TypeElement, List<Element>> parentAndChild = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        mFiler = processingEnv.getFiler();                  // Generate class.
        types = processingEnv.getTypeUtils();               // Get type utils.
        elementUtils = processingEnv.getElementUtils();      // Get class meta.
        typeUtils = new TypeUtils(types, elementUtils);

        // 从 build.gradle 里的配置读取 module name
        Map<String, String> options = processingEnv.getOptions();
        String privateAutowiredPolicyOption = null;
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(Constants.KEY_MODULE_NAME);
            try {
                privateAutowiredPolicyOption = options.get(Constants.KEY_PRIVATE_AUTOWIRED_POLICY);
                privateAutowiredPolicy = Constants.PrivateAutowiredPolicy.valueOf(privateAutowiredPolicyOption);
            } catch (Exception e) {
                privateAutowiredPolicy = Constants.PrivateAutowiredPolicy.ABORT;
            }
        }

        logger = new Logger(moduleName + "-AutowiredProcessor", processingEnv.getMessager());   // Package the log utils.

        logger.i("init <<<");
        logger.i("privateAutowiredPolicyOption: " + privateAutowiredPolicyOption);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        logger.i("start process...");
        if (CollectionUtils.isNotEmpty(set)) {
            try {
                Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Autowired.class);
                categories(elements);
                processElements();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                logger.e("process error: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
//                logger.e("process error: " + e.getMessage());
            }
        logger.i("process end");
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
        logger.i("processElements");

        TypeMirror activityTm = elementUtils.getTypeElement(Constants.TypeName.ACTIVITY).asType();
        TypeMirror fragmentTm = elementUtils.getTypeElement(Constants.TypeName.FRAGMENT).asType();
        TypeMirror fragmentTmV4 = elementUtils.getTypeElement(Constants.TypeName.FRAGMENT_V4).asType();
        //构造生成的方法的参数，Object target （实际上是Activity或者Fragment）
        ParameterSpec objectParamSpec = ParameterSpec.builder(TypeName.OBJECT, "target").build();

        printParentAndChild();

        if (MapUtils.isNotEmpty(parentAndChild)) {
            logger.i("parentAndChild not empty");
            for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {
                TypeElement parent = entry.getKey();

                TypeMirror parentTm = parent.asType();
                logger.i("type of parent: " + parentTm.toString());
                logger.i("type of activityTm: " + activityTm.toString());
                logger.i("type of fragmentTm: " + fragmentTm.toString());
                logger.i("type of fragmentTmV4: " + fragmentTmV4.toString());

                boolean isActivity = false;
                boolean isFragment = false;
                String statementPrefix = "";
                if (types.isSubtype(parentTm, activityTm)) {  // Activity, then use getIntent()
                    isActivity = true;
                    statementPrefix = "getIntent().";
                    logger.i("parent is an Activity");
                } else if (types.isSubtype(parentTm, fragmentTm) || types.isSubtype(parentTm, fragmentTmV4)) {   // Fragment, then use getArguments()
                    isFragment = true;
                    statementPrefix = "getArguments().";
                    logger.i("parent is a Fragment");
                } else {
                    logger.i("parent type unknown");
                    //throw new IllegalAccessException("The field [" + fieldName + "] need autowired from intent, its parent must be activity or fragment!");
                }

                if (!isActivity && !isFragment) {
                    continue;
                }

                List<Element> childs = entry.getValue();

                String qualifiedName = parent.getQualifiedName().toString();
                logger.i("autowired gen, parent: " + qualifiedName);
                //生成类文件的包名
                String packageName = qualifiedName.substring(0, qualifiedName.lastIndexOf("."));
                logger.i("autowired gen, packageName: " + packageName);

                //生成文件命名规则：类名 + __Autowired
                String fileName = parent.getSimpleName() + AnnotationConsts.AUTOWIRED_CLASS_SUFFIX;
                logger.i("autowired gen, fileName: " + fileName);

                //创建public static inject(){ } 方法
                MethodSpec.Builder injectMethodBuilder = MethodSpec.methodBuilder(AnnotationConsts.AUTOWIRED_METHOD_INJECT)
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
                    String extraKeyName = StringUtils.isEmpty(fieldConfig.name()) ? fieldName : fieldConfig.name();

                    boolean isPrivate = element.getModifiers().contains(Modifier.PRIVATE);
                    if (isPrivate && privateAutowiredPolicy != Constants.PrivateAutowiredPolicy.TRY_SETTER) {
                        throw new IllegalAccessException(privateAutowiredErrorMessage(fieldName, qualifiedName));
                    }
                    String originalValue = "substitute." + (isPrivate ? getGetterOfField(fieldName) : fieldName);

                    // (CastToSomeClass) substitute.
                    String valueStatement = buildCastCode(element) + "substitute." + statementPrefix;
                    valueStatement += buildStatement(originalValue, typeUtils.typeExchange(element), isActivity);

                    String statement = null;
                    if (isPrivate) {
                        // call the setter of the field
                        statement = "substitute." + getSetterOfField(fieldName) + "(" + valueStatement + ")";
                    } else {
                        // assign value directly
                        statement = "substitute." + fieldName + " = " + valueStatement;
                    }

                    injectMethodBuilder.addStatement(statement, extraKeyName);
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

    private String getSetterOfField(String filed) {
        return "set" + Character.toUpperCase(filed.charAt(0)) + filed.substring(1);
    }

    private String getGetterOfField(String filed) {
        return "get" + Character.toUpperCase(filed.charAt(0)) + filed.substring(1) + "()";
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Autowired.class.getCanonicalName());
    }

    private String buildCastCode(Element element) {
        int type = typeUtils.typeExchange(element);
        if (type == TypeKind.SERIALIZABLE.ordinal() || type == TypeKind.PARCELABLE.ordinal()) {
            return CodeBlock.builder().add("($T) ", ClassName.get(element.asType())).build().toString();
        }
        return "";
    }

    /**
     * create the code for file
     * @param originalValue
     * @param type
     * @param isActivity
     * @return
     */
    private String buildStatement(String originalValue, int type, boolean isActivity) {
        String statement = "";
        if (type == TypeKind.BOOLEAN.ordinal()) {
            statement = (isActivity ? ("getBooleanExtra($S, " + originalValue + ")") : ("getBoolean($S)"));
        } else if (type == TypeKind.BYTE.ordinal()) {
            statement = (isActivity ? ("getByteExtra($S, " + originalValue + ")") : ("getByte($S)"));
        } else if (type == TypeKind.SHORT.ordinal()) {
            statement = (isActivity ? ("getShortExtra($S, " + originalValue + ")") : ("getShort($S)"));
        } else if (type == TypeKind.INT.ordinal()) {
            statement = (isActivity ? ("getIntExtra($S, " + originalValue + ")") : ("getInt($S)"));
        } else if (type == TypeKind.LONG.ordinal()) {
            statement = (isActivity ? ("getLongExtra($S, " + originalValue + ")") : ("getLong($S)"));
        }else if(type == TypeKind.CHAR.ordinal()){
            statement = (isActivity ? ("getCharExtra($S, " + originalValue + ")") : ("getChar($S)"));
        } else if (type == TypeKind.FLOAT.ordinal()) {
            statement = (isActivity ? ("getFloatExtra($S, " + originalValue + ")") : ("getFloat($S)"));
        } else if (type == TypeKind.DOUBLE.ordinal()) {
            statement = (isActivity ? ("getDoubleExtra($S, " + originalValue + ")") : ("getDouble($S)"));
        } else if (type == TypeKind.STRING.ordinal()) {
            statement = (isActivity ? ("getStringExtra($S)") : ("getString($S)"));
        } else if (type == TypeKind.SERIALIZABLE.ordinal()) {
            statement = (isActivity ? ("getSerializableExtra($S)") : ("getSerializable($S)"));
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            statement = (isActivity ? ("getParcelableExtra($S)") : ("getParcelable($S)"));
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
        logger.i("categories start");

        if (CollectionUtils.isNotEmpty(elements)) {
            logger.i("elements not empty");

            for (Element element : elements) {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                Name parentName = enclosingElement.getQualifiedName();
                Name fieldName = element.getSimpleName();
                logger.i("element, parent: " + parentName + ", field: " + fieldName);

                if (element.getModifiers().contains(Modifier.PRIVATE)) {
                    if (privateAutowiredPolicy == Constants.PrivateAutowiredPolicy.TRY_SETTER) {
                        logger.w("field " + fieldName + " is private!");
                    } else {
                        throw new IllegalAccessException(privateAutowiredErrorMessage(fieldName.toString(), parentName.toString()));
                    }
                }

                if (parentAndChild.containsKey(enclosingElement)) { // Has categries
                    parentAndChild.get(enclosingElement).add(element);
                } else {
                    List<Element> childs = new ArrayList<>();
                    childs.add(element);
                    parentAndChild.put(enclosingElement, childs);
                }
            }

        } else {
            logger.i("elements empty");
        }

        logger.i("categories finished.");
    }

    private void printParentAndChild() {
        logger.i("printParentAndChild");
        for (Map.Entry<TypeElement, List<Element>> entry : parentAndChild.entrySet()) {
            logger.i("parent: " + entry.getKey().getQualifiedName());
            for (Element element : entry.getValue()) {
                logger.i("    child: " + element.getSimpleName());
            }
        }
    }

    private String privateAutowiredErrorMessage(String fieldName, String parentName) {
        return "Injected autowired fields CAN NOT be 'private'!!! Please check field ["
                + fieldName + "] in class [" + parentName + "] of module [" + moduleName + "]. If it is Kotlin, you can add @JvmField to it";
    }
}