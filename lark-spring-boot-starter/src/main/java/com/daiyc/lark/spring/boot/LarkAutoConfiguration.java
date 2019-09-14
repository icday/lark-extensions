package com.daiyc.lark.spring.boot;

import com.daiyc.lark.spring.LarkServerBean;
import com.daiyc.lark.spring.SpringContextHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author daiyuancheng
 * @date 2019/9/14
 */
@Configuration
@ConditionalOnProperty(prefix = "lark", value = "enable", havingValue = "true", matchIfMissing = true)
public class LarkAutoConfiguration {
    @Bean
    public LarkServerProperties larkServerProperties() {
        return new LarkServerProperties();
    }

    @Bean
    public LarkServerBean larkServerBean() {
        LarkServerProperties properties = larkServerProperties();
        return new LarkServerBean(properties.getHost(), properties.getPort(), properties.getPrompt());
    }

    @Bean
    @ConditionalOnMissingBean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }
}
