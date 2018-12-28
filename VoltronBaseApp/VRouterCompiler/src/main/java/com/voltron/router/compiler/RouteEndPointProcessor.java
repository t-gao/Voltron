package com.voltron.router.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.voltron.router.annotation.EndPoint;
import com.voltron.router.base.AnnotationUtil;
import com.voltron.router.base.EndPointMeta;
import com.voltron.router.compiler.utils.Logger;
import com.voltron.router.compiler.utils.PoetUtil;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;


@AutoService(Processor.class)
public class RouteEndPointProcessor extends AbstractProcessor {

    private String moduleName;

    private Logger logger;
    private Elements elementUtils;
    private Filer filer;

    // 分组信息
    private HashMap<String, Set<EndPointMeta>> groups = new HashMap<>();
    // group name 为空的分组
    private Set<EndPointMeta> noNameGroup = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();

        logger = new Logger(processingEnvironment.getMessager());

        // 从 build.gradle 里的配置读取 module name
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(Constants.KEY_MODULE_NAME);
        }

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
        }

        if (StringUtils.isEmpty(moduleName)) {
            logger.e(Constants.MODULE_NAME_NOT_CONFIGED_ERR_MSG);
            throw new RuntimeException(Constants.LOG_PREFIX + Constants.MODULE_NAME_NOT_CONFIGED_ERR_MSG);
        }
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
        logger.i("process started...");
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(EndPoint.class);
        if (!roundEnvironment.processingOver()) {
            logger.i("processing not over");
            if (elements != null && !elements.isEmpty()) {
                for (Element element : elements) {
                    logger.i("processing annotated Element: ", element);
                    processElement(element);
                }
            }
        } else {
            logger.i("processing over");
            if (!groups.isEmpty()) {
                for (Map.Entry<String, Set<EndPointMeta>> entry : groups.entrySet()) {
                    try {
                        generateGroupFile(entry.getKey(), entry.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.e(e.getMessage());
                    }
                }

                try {
                    generateGroupFile("", noNameGroup);
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }
            }
        }
        logger.i("process end");
        return true;
    }

    private void processElement(Element element) {
        if (element.getKind() == ElementKind.CLASS) {
            EndPointMeta endPointMeta = AnnotationUtil.buildEndPointMetaFromAnnotation(element.getAnnotation(EndPoint.class), element);
            if (endPointMeta == null) {
                return;
            }

            String path = endPointMeta.getPath();
            if (StringUtils.isEmpty(path)) {
                return;
            }

            String groupName = endPointMeta.getGroup();
            if (StringUtils.isEmpty(groupName)) {
                noNameGroup.add(endPointMeta);
            } else {
                Set<EndPointMeta> groupEndPoints = groups.get(groupName);
                if (groupEndPoints == null) {
                    groupEndPoints = new HashSet<>();
                    groups.put(groupName, groupEndPoints);
                }
                groupEndPoints.add(endPointMeta);
            }
        }
    }

    /**
     * Generates a java source file like below:
     *
     * package com.voltron.router.routes;
     *
     * import com.voltron.demo.app.SecondActivity;
     * import com.voltron.router.base.EndPointMeta;
     * import java.lang.String;
     * import java.util.Map;
     *
     *
     * public class VRouter__M__app__G__main {
     *   public static void load(Map<String, EndPointMeta> routes) {
     *     routes.put("/main/second", EndPointMeta.build("main", "/main/second", SecondActivity.class));
     *   }
     *
     *   public static String myName() {
     *     return "com.voltron.router.routes.VRouter__M__app__G__main";
     *   }
     * }
     *
     * @param groupName 分组名
     * @param endPointMetas 分组内的路由端点集合
     * @throws IOException
     */
    private void generateGroupFile(String groupName, Set<EndPointMeta> endPointMetas) throws IOException {
        if (groupName == null) {
            groupName = "";
        }
        logger.i("generateGroupFile groupName: " + groupName);

        if (endPointMetas == null || endPointMetas.isEmpty()) {
            return;
        }

        /*
         * Group 类的 load 方法的参数类型：
         * Map<String, EndPointMeta>
         */
        ParameterizedTypeName groupMethodParamType = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(EndPointMeta.class)
        );
        ParameterSpec groupParamSpec = ParameterSpec.builder(groupMethodParamType, "routes").build();

        /*
         * Group 类的 load 方法：
         *
            public static void load(Map<String, EndPointMeta> routes) {
                routes.put("/group/somepath1", EndPointMeta.build());
                routes.put("/group2/somepath2", EndPointMeta.build());
            }
         */
        MethodSpec.Builder initMethod = MethodSpec.methodBuilder("load")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(groupParamSpec);

        for (EndPointMeta endPointMeta : endPointMetas) {
            ClassName className = ClassName.get((TypeElement) endPointMeta.getElement());
            String path = endPointMeta.getPath();
            initMethod.addStatement("routes.put($S, $T.build($S, $S, $T.class))",
                    path, EndPointMeta.class, groupName, path, className);
        }

        String pkgName = Constants.GENERATED_PACKAGE;
        String clazzName = PoetUtil.getGroupJavaFileName(moduleName, groupName);

        MethodSpec.Builder myNameMethod = MethodSpec.methodBuilder("myName")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class)
                .addStatement("return $S", pkgName + "." + clazzName);
        /*
         * Group 类
         */
        JavaFile.builder(pkgName,
                TypeSpec.classBuilder(clazzName)
                        .addJavadoc(Constants.GENERATED_FILE_JAVA_DOC)
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(initMethod.build())
                        .addMethod(myNameMethod.build())
                        .build())
                .build().writeTo(filer);
    }
}
