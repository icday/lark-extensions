package com.dyc.tools.dubbo.console.commands;

import com.dyc.embed.console.command.CmdContext;
import com.dyc.embed.console.command.PreparedCommand;
import com.dyc.embed.console.command.Task;
import com.dyc.tools.dubbo.console.Helper;

/**
 * @author daiyc
 * @date 2019/9/6
 */
public class CurrentCommand extends PreparedCommand {
    public CurrentCommand() {
        super("pwd", "Print working default service.");
    }

    @Override
    protected String doExecution(Task task, CmdContext context) {
        return Helper.getCurrentPath();
    }
}
