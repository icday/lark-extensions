package com.daiyc.lark.spring.boot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author daiyc
 * @date 2019/9/14
 */
@ConfigurationProperties(prefix = "lark.server")
@Data
public class LarkServerProperties {
    private String host;

    private int port;
}
