package com.dyc.tools.dubbo.console.commands;

import com.alibaba.dubbo.common.utils.PojoUtils;
import com.alibaba.dubbo.common.utils.ReflectUtils;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import com.alibaba.fastjson.JSON;
import com.dyc.embed.console.command.CmdContext;
import com.dyc.embed.console.command.RawCommand;
import com.dyc.embed.console.complete.ArgCompleter;
import com.dyc.embed.console.complete.DefaultSingleArgCompleter;
import com.dyc.embed.console.session.Context;
import com.dyc.tools.dubbo.console.ClassInfo;
import com.dyc.tools.dubbo.console.Constants;
import com.dyc.tools.dubbo.console.Helper;
import com.dyc.tools.dubbo.console.MethodInfo;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author daiyc
 * @date 2019/8/7
 */
public class InvokeCommand extends RawCommand {
    public InvokeCommand() {
        super("invoke", "Invoke a Dubbo service");
    }

    private static Method findMethod(Exporter<?> exporter, String method, List<Object> args) {
        Invoker<?> invoker = exporter.getInvoker();
        Method[] methods = invoker.getInterface().getMethods();
        for (Method m : methods) {
            if (m.getName().equals(method) && isMatch(m.getParameterTypes(), args)) {
                return m;
            }
        }
        return null;
    }

    private static boolean isMatch(Class<?>[] types, List<Object> args) {
        if (types.length != args.size()) {
            return false;
        }
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            Object arg = args.get(i);

            if (arg == null) {
                // if the type is primitive, the method to invoke will cause NullPointerException definitely
                // so we can offer a specified error message to the invoker in advance and avoid unnecessary invoking
                if (type.isPrimitive()) {
                    throw new NullPointerException(String.format(
                            "The type of No.%d parameter is primitive(%s), but the value passed is null.", i + 1, type.getName()));
                }

                // if the type is not primitive, we choose to believe what the invoker want is a null value
                continue;
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
        }
        return true;
    }

    @Override
    protected String doExecution(String argStr, CmdContext context) {
        return invoke(argStr);
    }

    @Override
    protected ArgCompleter initArgCompleter() {
        return DefaultSingleArgCompleter.builder()
                .datasource(() -> Helper.listAllExportedService().stream()
                        .map(ClassInfo::getMethods)
                        .flatMap(Collection::stream)
                        .map(MethodInfo::getCanonicalName)
                        .collect(Collectors.toList())
                ).build();
    }

    public String invoke(String message) {
        if (message == null || message.length() == 0) {
            return "Please input method name, eg: \r\ninvoke xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})\r\ninvoke XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})\r\ninvoke com.xxx.XxxService.xxxMethod(1234, \"abcd\", {\"prop\" : \"value\"})";
        }
        StringBuilder buf = new StringBuilder();
        String service = Context.getValue(Constants.CTX_WD);
        if (StringUtils.isNotBlank(service)) {
            buf.append("Use default service ").append(service).append(".\r\n");
        }
        message = StringUtils.strip(message, " ;");
        int i = message.indexOf("(");
        if (i < 0 || !message.endsWith(")")) {
            return "Invalid parameters, format: service.method(args)";
        }
        String method = message.substring(0, i).trim();
        String args = message.substring(i + 1, message.length() - 1).trim();
        i = method.lastIndexOf(".");
        if (i >= 0) {
            service = method.substring(0, i).trim();
            method = method.substring(i + 1).trim();
        }
        List<Object> list;
        try {
            list = JSON.parseArray("[" + args + "]", Object.class);
        } catch (Throwable t) {
            return "Invalid json argument, cause: " + t.getMessage();
        }
        Invoker<?> invoker = null;
        Method invokeMethod = null;
        for (Exporter<?> exporter : DubboProtocol.getDubboProtocol().getExporters()) {
            if (service == null || service.length() == 0) {
                invokeMethod = findMethod(exporter, method, list);
                if (invokeMethod != null) {
                    invoker = exporter.getInvoker();
                    break;
                }
            } else {
                if (service.equals(exporter.getInvoker().getInterface().getSimpleName())
                        || service.equals(exporter.getInvoker().getInterface().getName())
                        || service.equals(exporter.getInvoker().getUrl().getPath())) {
                    invokeMethod = findMethod(exporter, method, list);
                    invoker = exporter.getInvoker();
                    break;
                }
            }
        }
        if (invoker != null) {
            if (invokeMethod != null) {
                try {
                    Object[] array = PojoUtils.realize(list.toArray(), invokeMethod.getParameterTypes(), invokeMethod.getGenericParameterTypes());
                    long start = System.currentTimeMillis();
                    Object result = invoker.invoke(new RpcInvocation(invokeMethod, array)).recreate();
                    long end = System.currentTimeMillis();
                    buf.append(JSON.toJSONString(result));
                    buf.append("\r\nelapsed: ");
                    buf.append(end - start);
                    buf.append(" ms.");
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
            } else {
                buf.append("No such method " + method + " in service " + service);
            }
        } else {
            buf.append("No such service " + service);
        }
        return buf.toString();
    }
}
