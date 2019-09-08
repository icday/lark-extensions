package com.dyc.lark.dubbo.commands;

import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.dyc.lark.core.command.CmdContext;
import com.dyc.lark.core.command.RawCommand;
import com.dyc.lark.core.complete.ArgCompleter;
import com.dyc.lark.core.complete.DefaultSingleArgCompleter;
import com.dyc.lark.dubbo.ClassInfo;
import com.dyc.lark.dubbo.Helper;
import com.dyc.lark.dubbo.MethodInfo;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author daiyc
 */
@SuppressWarnings("all")
public class InvokeCommand extends RawCommand {
    private static SerializeConfig prettyConfig = new SerializeConfig();

    public InvokeCommand() {
        super("invoke", "Invoke a Dubbo service");
    }

    @AllArgsConstructor
    private static class Invocation {
        private ClassInfo service;

        private MethodInfo method;

        private Object[] args;
    }

    @Override
    protected String doExecution(String argStr, CmdContext context) {
        Invocation invocation = parseInput(argStr);
        return invoke(invocation);
    }

    @Override
    protected ArgCompleter initArgCompleter() {
        return DefaultSingleArgCompleter.builder()
                .datasource(() -> Helper.listAllExportedService().stream()
                        .map(ClassInfo::getMethods)
                        .flatMap(Collection::stream)
                        .map(MethodInfo::getCanonicalName)
                        .collect(Collectors.toList())
                )
                .partial(true)
                .build();
    }

    private String invoke(Invocation invocation) {
        long start = System.currentTimeMillis();
        ClassInfo service = invocation.service;
        MethodInfo method = invocation.method;
        Object[] args = invocation.args;
        Object result = null;
        StringBuilder sb = new StringBuilder();
        try {
            result = service.getInvoker().invoke(new RpcInvocation(method.getMethod(), args)).recreate();
        } catch (Throwable throwable) {
            throw new RuntimeException(String.format("Failed to invoke method %s, cause: %s", method.getSimpleName(), com.alibaba.dubbo.common.utils.StringUtils.toString(throwable)));
        }
        long end = System.currentTimeMillis();
        sb.append(JSON.toJSONString(result, SerializerFeature.PrettyFormat));
        sb.append("\r\nelapsed: ");
        sb.append(end - start);
        sb.append(" ms.");
        return sb.toString();
    }

    private Invocation parseInput(String input) {
        input = StringUtils.strip(input, " ;");
        int i = input.indexOf('(');

        if (i < 0 || !input.endsWith(")")) {
            throw new IllegalArgumentException("Invalid parameters, format: service.method(args)");
        }

        String service;
        String method = input.substring(0, i).trim();
        String args = input.substring(i + 1, input.length() - 1).trim();

        i = method.lastIndexOf('.');
        if (i < 0) {
            service = Helper.getCurrentPath();
        } else {
            service = method.substring(0, i).trim();
            method = method.substring(i + 1).trim();
        }

        List<Object> argList;

        try {
            argList = JSON.parseArray("[" + args + "]", Object.class);
        } catch (Throwable t) {
            throw new IllegalArgumentException(String.format("Invalid json argument, cause: %s", t.getMessage()));
        }

        String methodName = method;
        ClassInfo classInfo = Helper.getExportedService(service)
                .orElseThrow(() -> new IllegalArgumentException(String.format("No such service %s", service)));

        MethodInfo methodInfo = classInfo.getMethods()
                .stream()
                .filter(m -> m.getSimpleName().equals(methodName) && m.match(argList))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("No such method %s in service %s", methodName, service)));

        Object[] array = PojoUtils.realize(argList.toArray(), methodInfo.getParameterTypes(), methodInfo.getGenericParameterTypes());

        return new Invocation(classInfo, methodInfo, array);
    }
}
