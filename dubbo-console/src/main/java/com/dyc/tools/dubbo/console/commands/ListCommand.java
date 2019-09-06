package com.dyc.tools.dubbo.console.commands;

import com.dyc.embed.console.command.CmdContext;
import com.dyc.embed.console.command.PreparedCommand;
import com.dyc.embed.console.command.Task;
import com.dyc.tools.dubbo.console.ClassInfo;
import com.dyc.tools.dubbo.console.Helper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.List;
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
        METHOD_AND_SIGNATURE
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

        List<String> serviceNames = commandLine.getArgList();
        List<ClassInfo> services;
        if (serviceNames.isEmpty()) {
            services = Helper.listAllExportedService();
        } else {
            services = serviceNames
                    .stream()
                    .map(s -> Helper.getExportedService(s).orElse(null))
                    .collect(Collectors.toList());
        }

        PrintMode mode = detectPrintMode(commandLine);

        List<String> ss = services.stream()
                .map(s -> formatService(s, mode))
                .collect(Collectors.toList());
        return String.join("\n", ss);
    }

    private PrintMode detectPrintMode(CommandLine commandLine) {
        if (commandLine.hasOption("d")) {
            return PrintMode.METHOD_AND_SIGNATURE;
        } else if (commandLine.hasOption("m")) {
            return PrintMode.METHOD;
        }
        return PrintMode.NONE;
    }

    private String formatService(ClassInfo classInfo, PrintMode mode) {
        if (mode == PrintMode.NONE) {
            return classInfo.getCanonicalName();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(classInfo.getCanonicalName());

        List<String> methods = classInfo.getMethods().stream()
                .map(m -> mode == PrintMode.METHOD ? m.getSimpleName() : m.getFullSignature())
                .map(METHOD_PREFIX::concat)
                .collect(Collectors.toList());
        if (!methods.isEmpty()) {
            sb.append("\n");
        }
        sb.append(String.join("\n", methods));
        return sb.toString();
    }
}
