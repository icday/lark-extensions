package com.dyc.tools.dubbo.console;

import com.alibaba.dubbo.common.utils.ReflectUtils;
import lombok.Getter;

import java.lang.reflect.Method;

/**
 * @author daiyc
 * @date 2019/8/27
 */
@Getter
public class MethodInfo {
    private String canonicalName;

    private String simpleName;

    private String fullSignature;

    public MethodInfo(Class<?> type, Method method) {
        String classCanonicalName = type.getCanonicalName();
        simpleName = method.getName();
        canonicalName = String.format("%s.%s", classCanonicalName, simpleName);
        fullSignature = ReflectUtils.getName(method);
    }
}
