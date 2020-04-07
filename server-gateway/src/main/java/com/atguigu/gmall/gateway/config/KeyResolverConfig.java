package com.atguigu.gmall.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * @author 张善涛
 * @create 2020-04-07 18:07
 * @DataType:{ attribute:
 * method:
 * returnType:
 * }
 */
@Configuration
public class KeyResolverConfig {
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName());
    }
}
