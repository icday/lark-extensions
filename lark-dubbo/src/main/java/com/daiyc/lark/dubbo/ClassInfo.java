package com.daiyc.lark.dubbo;

import com.alibaba.dubbo.rpc.Invoker;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author daiyc
 */
public class ClassInfo {
    private Class<?> clazz;

    @Getter
    private String canonicalName;

    @Getter
    private String simpleName;

    @Getter
    private List<MethodInfo> methods;

    @Getter
    private Invoker<?> invoker;

    private static Function<MethodInfo, String> MR_HIDDEN = (i) -> null;

    private static Function<MethodInfo, String> MR_NAME = MethodInfo::getSimpleName;

    private static Function<MethodInfo, String> MR_SIGNATURE = MethodInfo::getFullSignature;

    private static final int DEFAULT_PADDING = 2;

    public ClassInfo(Invoker<?> invoker) {
        this(invoker.getInterface());
        this.invoker = invoker;
    }

    private ClassInfo(Class<?> clazz) {
        this.clazz = clazz;

        init();
    }

    private void init() {
        canonicalName = clazz.getCanonicalName();
        simpleName = clazz.getSimpleName();

        Method[] methods = clazz.getMethods();

        this.methods = Stream.of(methods)
                .map(m -> parseMethod(clazz, m))
                .collect(Collectors.toList());
    }

    private MethodInfo parseMethod(Class<?> type, Method method) {
        return new MethodInfo(type, method);
    }

    public String desc() {
        return render(MR_HIDDEN, DEFAULT_PADDING);
    }

    public String desc(boolean showMethod) {
        if (!showMethod) {
            return desc();
        }
        return render(MR_NAME, DEFAULT_PADDING);
    }

    public String detailedDesc() {
        return render(MR_SIGNATURE, DEFAULT_PADDING);
    }

    private String render(Function<MethodInfo, String> r, int pad) {
        StringBuilder sb = new StringBuilder();
        sb.append(canonicalName);
        if (r != MR_HIDDEN) {
            methods.stream()
                    .map(r)
                    .filter(StringUtils::isNotBlank)
                    .forEach(desc -> {
                        sb.append("\n");
                        sb.append(pad("-", pad));
                        sb.append(desc);
                    });
        }
        return sb.toString();
    }

    private static String pad(String s, int pad) {
        return pad <= 0 ? s : String.join("", StringUtils.repeat(' ', pad), s);
    }
}
