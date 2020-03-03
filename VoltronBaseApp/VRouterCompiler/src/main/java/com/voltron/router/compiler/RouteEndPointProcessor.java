package com.voltron.router.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.voltron.router.EndPointType;
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
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;


@AutoService(Processor.class)
@SupportedOptions({Constants.KEY_MODULE_NAME, Constants.KEY_PRIVATE_AUTOWIRED_POLICY})
public class RouteEndPointProcessor extends AbstractProcessor {

    private String moduleName;

    private Logger logger;
    private Filer filer;
    private Types typeUtils;

    // 分组信息
    private HashMap<String, Set<EndPointMetaForProcessor>> groups = new HashMap<>();
    // group name 为空的分组
    private Set<EndPointMetaForProcessor> noNameGroup = new HashSet<>();

    private TypeMirror typeActivity, typeFragment, typeFragmentV4, typeService, typeParcelable;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        Elements elementUtils = processingEnvironment.getElementUtils();
        typeUtils = processingEnvironment.getTypeUtils();

        // 从 build.gradle 里的配置读取 module name
        Map<String, String> options = processingEnv.getOptions();
        if (MapUtils.isNotEmpty(options)) {
            moduleName = options.get(Constants.KEY_MODULE_NAME);
        }
        logger = new Logger(moduleName + "-RouteEndPointProcessor", processingEnvironment.getMessager());
        logger.w("init");

        if (StringUtils.isNotEmpty(moduleName)) {
            moduleName = moduleName.replaceAll("[^0-9a-zA-Z_]+", "");
        }

        if (StringUtils.isEmpty(moduleName)) {
            logger.e(Constants.MODULE_NAME_NOT_CONFIGED_ERR_MSG);
            throw new RuntimeException(Constants.LOG_PREFIX + Constants.MODULE_NAME_NOT_CONFIGED_ERR_MSG);
        }

        typeActivity = elementUtils.getTypeElement(Constants.TypeName.ACTIVITY).asType();
        typeFragment = elementUtils.getTypeElement(Constants.TypeName.FRAGMENT).asType();
        typeFragmentV4 = elementUtils.getTypeElement(Constants.TypeName.FRAGMENT_V4).asType();
        typeService = elementUtils.getTypeElement(Constants.TypeName.SERVICE).asType();
        typeParcelable = elementUtils.getTypeElement(Constants.TypeName.PARCELABLE).asType();
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
        logger.i("start process...");
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(EndPoint.class);
        if (!roundEnvironment.processingOver()) {
            logger.i("processing not over");
            if (elements != null && !elements.isEmpty()) {
                logger.i("elements not empty!");
                for (Element element : elements) {
                    logger.i("processing annotated Element: " + element.getSimpleName());
                    processElement(element);
                }
            } else {
                logger.i("elements empty!");
            }
        } else {
            logger.i("processing over");

            try {
                generateGroupFile("", noNameGroup);
            } catch (IOException e) {
                logger.e(e.getMessage());
            }

            if (!groups.isEmpty()) {
                for (Map.Entry<String, Set<EndPointMetaForProcessor>> entry : groups.entrySet()) {
                    try {
                        generateGroupFile(entry.getKey(), entry.getValue());
                    } catch (IOException e) {
                        logger.e(e.getMessage());
                    }
                }
            } else {
                logger.i("GROUPS EMPTY");
            }
        }
        logger.i("process end");
        return true;
    }

    private EndPointType getEndPointType(Element element) {
        TypeMirror typeMirror = element.asType();
        if (typeUtils.isSubtype(typeMirror, typeActivity)) {
            return EndPointType.ACTIVITY;
        } else if (typeUtils.isSubtype(typeMirror, typeService)) {
            return EndPointType.SERVICE;
        } else if (typeUtils.isSubtype(typeMirror, typeFragmentV4)) {
            return EndPointType.FRAGMENT_V4;
        } else if (typeUtils.isSubtype(typeMirror, typeFragment)) {
            return EndPointType.FRAGMENT;
        } else if (typeUtils.isSubtype(typeMirror, typeParcelable)) {
            return EndPointType.PARCELABLE;
        } else {
            return EndPointType.OTHER;
        }
    }

    private void processElement(Element element) {
        logger.i("processElement: " + element.getSimpleName());

        if (element.getKind() == ElementKind.CLASS) {
            EndPointMetaForProcessor endPointMeta = buildEndPointMetaFromAnnotation(
                    element.getAnnotation(EndPoint.class), element, getEndPointType(element));

            if (endPointMeta == null) {
                logger.w("processElement built endPointMeta is NULL!" );
                return;
            }

            if (StringUtils.isEmpty(endPointMeta.getRoute())) {
                logger.w("processElement built endPointMeta's route is empty!" );
                return;
            }

            String groupName = endPointMeta.getGroup();
            if (StringUtils.isEmpty(groupName)) {
                noNameGroup.add(endPointMeta);
            } else {
                Set<EndPointMetaForProcessor> groupEndPoints = groups.get(groupName);
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
     * @throws IOException IOException
     */
    private void generateGroupFile(String groupName, Set<EndPointMetaForProcessor> endPointMetas) throws IOException {
        if (groupName == null) {
            groupName = "";
        }
        logger.i("generateGroupFile groupName: " + groupName);

        if (endPointMetas == null || endPointMetas.isEmpty()) {
            logger.i("generateGroupFile endPointMetas empty!");
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

        for (EndPointMetaForProcessor endPointMeta : endPointMetas) {
            ClassName className = ClassName.get((TypeElement) endPointMeta.getElement());
            String route = endPointMeta.getRoute();
            String endPointKey = AnnotationUtil.getEndPointKeyFromRoute(route);
            initMethod.addStatement("routes.put($S, $T.build($S, $S, $S, $S, $S, $T.class, $T." + endPointMeta.getEndPointType() + "))",
                    endPointKey, EndPointMeta.class, endPointKey, groupName, endPointMeta.getScheme(),
                    endPointMeta.getValue(), route, className,
                    ClassName.get(EndPointType.class));
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

    private EndPointMetaForProcessor buildEndPointMetaFromAnnotation(EndPoint endPointAnno,
                                                                            Element element,
                                                                           EndPointType endPointType) {

        logger.i("buildEndPointMetaFromAnnotation: " + element.getSimpleName());

        if (endPointAnno == null) {
            return null;
        }

        String scheme = endPointAnno.scheme();
        String host = endPointAnno.host();
        String path = endPointAnno.path();

        String value = endPointAnno.value();

        logger.i("buildEndPointMetaFromAnnotation，scheme: " + scheme + ", host: " + host + ", path: " + path + ", value: " + value);

        String route = value;
        if (route.isEmpty()) {
            logger.i("buildEndPointMetaFromAnnotation, ROUTE EMPTY!");
            route = AnnotationUtil.buildRouteFromSchemeHostPath(scheme, host, path);
        }

        if (com.voltron.router.base.StringUtils.isEmpty(route)) {
            logger.w("buildEndPointMetaFromAnnotation, ROUTE STILL EMPTY!");
            return null;
        }

        String groupName = AnnotationUtil.extractGroupNameFromRoute(route);
        logger.i("buildEndPointMetaFromAnnotation, groupName: " + groupName);

        return new EndPointMetaForProcessor(groupName, scheme, host, path, value, route, element, endPointType);
    }
}
