package com.dyc.lark.spring;

import com.dyc.lark.core.server.Server;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author daiyc
 */
public class ConsoleBean implements InitializingBean {
    private String host;

    private int port;

    private Server server;

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
