package com.daiyc.lark.dubbo.commands;

import com.daiyc.lark.core.command.CmdContext;
import com.daiyc.lark.core.command.PreparedCommand;
import com.daiyc.lark.core.command.Task;
import com.daiyc.lark.dubbo.Helper;

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
