package com.dyc.lark.dubbo.commands;

import com.dyc.lark.core.command.CmdContext;
import com.dyc.lark.core.command.PreparedCommand;
import com.dyc.lark.core.command.Task;
import com.dyc.lark.dubbo.Helper;

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
