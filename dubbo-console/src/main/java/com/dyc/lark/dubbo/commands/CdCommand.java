package com.dyc.lark.dubbo.commands;

import com.dyc.lark.core.command.CmdContext;
import com.dyc.lark.core.command.PreparedCommand;
import com.dyc.lark.core.command.Task;
import com.dyc.lark.core.complete.ArgCompleter;
import com.dyc.lark.core.complete.DefaultSingleArgCompleter;
import com.dyc.lark.dubbo.ClassInfo;
import com.dyc.lark.dubbo.Helper;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author daiyc
 * @date 2019/8/27
 */
public class CdCommand extends PreparedCommand {
    public CdCommand() {
        super("cd", "Change current work directory");
    }

    @Override
    public String doExecution(Task task, CmdContext context) {
        List<String> argList = task.getCommandLine().getArgList();
        if (argList.size() != 1) {
            throw new RuntimeException("Not support arg size");
        }

        String serviceName = argList.get(0);
        serviceName = StringUtils.strip(serviceName, " /");
        if (StringUtils.isBlank(serviceName)) {
            Helper.resetCurrentPath();
            return "";
        }
        if (!Helper.listAllServiceName().contains(serviceName)) {
            throw new RuntimeException("Not a valid service name");
        }
        Helper.setCurrentPath(serviceName);
        return "";
    }

    @Override
    protected ArgCompleter initArgCompleter() {
        return DefaultSingleArgCompleter.builder()
                .datasource(() -> Helper.listAllExportedService()
                        .stream()
                        .map(ClassInfo::getCanonicalName)
                        .collect(Collectors.toList()))
                .build();
    }
}
