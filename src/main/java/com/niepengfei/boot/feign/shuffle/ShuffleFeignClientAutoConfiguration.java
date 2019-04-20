package com.niepengfei.boot.feign.shuffle;

import feign.Client;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.feign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.netflix.feign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

/**
 * @author Jack
 */
@Configuration
@AutoConfigureAfter({FeignRibbonClientAutoConfiguration.class})
public class ShuffleFeignClientAutoConfiguration {

    @Configuration
    @ConditionalOnProperty(value = "shuffle.turnOn", havingValue = "true", matchIfMissing = false)
    public static class ShuffleComponentFeignLoadBalancerFeignClient {

        @Bean
        @Primary
        public Client feignClient(CachingSpringLoadBalancerFactory cachingFactory, SpringClientFactory clientFactory, DiscoveryClient discoveryClient, Environment environment) {
            return new ShuffleFeignLoadBalancerFeignClient(new Client.Default(null, null), cachingFactory, clientFactory,discoveryClient,environment);
        }
    }
}
