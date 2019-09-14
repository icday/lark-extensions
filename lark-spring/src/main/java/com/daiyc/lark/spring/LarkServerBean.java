package com.daiyc.lark.spring;

import com.daiyc.lark.core.server.ConsoleConfig;
import com.daiyc.lark.core.server.Server;
import com.daiyc.lark.core.server.TelnetServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author daiyc
 */
public class LarkServerBean implements InitializingBean, DisposableBean {
    private Server server;

    private final ConsoleConfig config;

    public LarkServerBean(String host, int port, String prompt) {
        config = new ConsoleConfig();
        if (host != null && !host.isEmpty()) {
            config.setHost(host);
        }
        if (port > 0) {
            config.setPort(port);
        }

        if (prompt != null && !prompt.isEmpty()) {
            config.setPrompt(prompt);
        }
    }

    public LarkServerBean(String host, int port) {
        this(host, port, null);
    }

    public LarkServerBean(int port) {
        this(null, port, null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        server = new TelnetServer(config);
        server.start();
    }

    @Override
    public void destroy() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}
