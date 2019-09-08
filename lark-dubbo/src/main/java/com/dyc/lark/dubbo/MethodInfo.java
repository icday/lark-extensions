package com.dyc.lark.dubbo;

import com.alibaba.dubbo.common.utils.ReflectUtils;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author daiyc
 * @date 2019/8/27
 */
@Getter
@SuppressWarnings("all")
public class MethodInfo {
    private String canonicalName;

    private String simpleName;

    private String fullSignature;

    private Method method;

    private Class<?>[] parameterTypes;

    private Type[] genericParameterTypes;

    public MethodInfo(Class<?> type, Method method) {
        String classCanonicalName = type.getCanonicalName();
        this.method = method;
        simpleName = method.getName();
        canonicalName = String.format("%s.%s", classCanonicalName, simpleName);
        fullSignature = ReflectUtils.getName(method);

        parameterTypes = method.getParameterTypes();
        genericParameterTypes = method.getGenericParameterTypes();
    }

    public boolean match(List<Object> args) {
        if (args.size() != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < args.size(); i++) {
            if (!match(args.get(i), parameterTypes[i], i)) {
                return false;
            }
        }
        return true;
    }

    private boolean match(Object arg, Class<?> type, int i) {
        if (arg == null && type.isPrimitive()) {
            throw new NullPointerException(String.format(
                    "The type of No.%d parameter is primitive(%s), but the value passed is null.", i + 1, type.getName()));
        }
        if (arg == null) {
            return true;
        }
        if (ReflectUtils.isPrimitive(arg.getClass())) {
            if (!ReflectUtils.isPrimitive(type)) {
                return false;
            }
        } else if (arg instanceof Map) {
            String name = (String) ((Map<?, ?>) arg).get("class");
            Class<?> cls = arg.getClass();
            if (name != null && name.length() > 0) {
                cls = ReflectUtils.forName(name);
            }
            if (!type.isAssignableFrom(cls)) {
                return false;
            }
        } else if (arg instanceof Collection) {
            if (!type.isArray() && !type.isAssignableFrom(arg.getClass())) {
                return false;
            }
        } else {
            if (!type.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }

        return true;
    }
}
