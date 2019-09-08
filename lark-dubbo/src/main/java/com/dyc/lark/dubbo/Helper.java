package com.dyc.lark.dubbo;

import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol;
import com.dyc.lark.core.session.Context;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author daiyc
 * @date 2019/8/27
 */
public class Helper {
    public static List<String> listAllServiceName() {
        return listAllExportedService()
                .stream()
                .map(ClassInfo::getCanonicalName)
                .collect(Collectors.toList());
    }

    /**
     * 列出所有暴露的Dubbo服务的接口
     *
     * @return Interfaces
     */
    public static List<ClassInfo> listAllExportedService() {
        return DubboProtocol.getDubboProtocol().getExporters().stream().map(Exporter::getInvoker)
                .map(ClassInfo::new).collect(Collectors.toList());
    }

    public static Optional<ClassInfo> getCurrentService() {
        return getExportedService(getCurrentPath());
    }

    public static Optional<ClassInfo> getExportedService(String serviceName) {
        if (StringUtils.isBlank(serviceName)) {
            return Optional.empty();
        }
        return listAllExportedService()
                .stream()
                .filter(c -> c.getCanonicalName().equals(serviceName) || c.getCanonicalName().endsWith(serviceName))
                .findFirst();
    }

    public static String getCurrentPath() {
        return Context.getValue(Constants.CTX_WD, Constants.ROOT_PATH);
    }

    public static void setCurrentPath(String path) {
        Context.setValue(Constants.CTX_WD, path);
    }

    public static void resetCurrentPath() {
        Context.remove(Constants.CTX_WD);
    }
}
