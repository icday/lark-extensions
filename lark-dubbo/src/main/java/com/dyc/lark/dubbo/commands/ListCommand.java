package com.dyc.lark.dubbo.commands;

import com.dyc.lark.core.command.CmdContext;
import com.dyc.lark.core.command.PreparedCommand;
import com.dyc.lark.core.command.Task;
import com.dyc.lark.dubbo.ClassInfo;
import com.dyc.lark.dubbo.Helper;
import com.dyc.lark.dubbo.MethodInfo;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author daiyc
 * @date 2019/8/20
 */
public class ListCommand extends PreparedCommand {

    private enum PrintMode {
        /**
         * 只打印service名称
         */
        NONE,
        /**
         * 列出方法名
         */
        METHOD,

        /**
         * 列出每个方法的签名
         */
        METHOD_AND_SIGNATURE;

        public static PrintMode max(PrintMode a, PrintMode b) {
            return a.compareTo(b) > 0 ? a : b;
        }
    }

    private static final String METHOD_PREFIX = "  - ";

    public ListCommand() {
        super("ls", "List exported dubbo services");
    }

    @Override
    protected Options initOptions() {
        Options options = new Options();
        Option serviceOpt = new Option("s", "service", true, "list methods of service");
        serviceOpt.setArgName("serviceName");
        options
//                .addOption(serviceOpt)
                .addOption("m", "method", false, "list methods of every service")
                .addOption("d", "detail", false, "show method signature");
        return options;
    }

    @Override
    public String doExecution(Task task, CmdContext cmdContext) {
        CommandLine commandLine = task.getCommandLine();
        PrintMode mode = PrintMode.NONE;
        List<String> serviceNames = commandLine.getArgList();
        List<ClassInfo> services;
        if (serviceNames.isEmpty()) {
            Optional<ClassInfo> currentService = Helper.getCurrentService();
            if (currentService.isPresent()) {
                services = Collections.singletonList(currentService.get());
                mode = PrintMode.METHOD;
            } else {
                services = Helper.listAllExportedService();
            }
        } else {
            services = serviceNames
                    .stream()
                    .map(s -> Helper.getExportedService(s).orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        mode = PrintMode.max(detectPrintMode(commandLine), mode);

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (ClassInfo service : services) {
            if (!first) {
                sb.append('\n');
            } else {
                first = false;
            }
            formatService(sb, service, mode, 2);
        }

        return sb.toString();
    }

    private PrintMode detectPrintMode(CommandLine commandLine) {
        if (commandLine.hasOption("d")) {
            return PrintMode.METHOD_AND_SIGNATURE;
        } else if (commandLine.hasOption("m")) {
            return PrintMode.METHOD;
        }
        return PrintMode.NONE;
    }

    private void formatService(StringBuilder sb, ClassInfo classInfo, PrintMode mode, int padding) {
        if (mode == PrintMode.NONE) {
            sb.append(classInfo.getCanonicalName());
            return;
        }
        sb.append(classInfo.getCanonicalName());
        List<MethodInfo> methods = classInfo.getMethods();
        for (int i = 0; i < methods.size(); i++) {
            MethodInfo methodInfo = methods.get(i);
            sb.append('\n');
            sb.append(StringUtils.repeat(' ', padding));
            sb.append(METHOD_PREFIX);
            sb.append(mode == PrintMode.METHOD ? methodInfo.getSimpleName() : methodInfo.getFullSignature());
        }
    }
}
