package com.daiyc.lark.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author daiyuancheng
 * @date 2019/9/14
 */
@ConfigurationProperties(prefix = "lark.server")
@Component
@Data
public class LarkServerProperties {
    private String host;

    private int port;

    private String prompt = "lark > ";
}
