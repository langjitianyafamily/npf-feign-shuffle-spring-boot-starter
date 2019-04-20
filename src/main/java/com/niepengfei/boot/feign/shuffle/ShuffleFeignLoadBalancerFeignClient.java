package com.niepengfei.boot.feign.shuffle;

import feign.Client;
import feign.Request;
import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.feign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.netflix.feign.ribbon.LoadBalancerFeignClient;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.util.List;

/**
 * @author Jack
 */
@Slf4j
public class ShuffleFeignLoadBalancerFeignClient extends LoadBalancerFeignClient {

    private DiscoveryClient discoveryClient;

    private Environment environment;

    public ShuffleFeignLoadBalancerFeignClient(Client delegate, CachingSpringLoadBalancerFactory lbClientFactory, SpringClientFactory clientFactory, DiscoveryClient discoveryClient,Environment environment) {
        super(delegate, lbClientFactory, clientFactory);
        this.discoveryClient = discoveryClient;
        this.environment = environment;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        String url = request.url();
        String originalProtocol = url.substring(0, 7);
        String apiUrl = url.substring(7, url.length() - 1);
        String serviceId = StringUtils.split(apiUrl,"/")[0];
        String address = StringUtils.split(apiUrl,"/")[1];
        String targetUrl = originalProtocol + "#{serviceId}"+"/"+address;
        String constructorServiceId = request.url();
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (CollectionUtils.isEmpty(instances)) {
            String[] serviceAndEnv = StringUtils.split(serviceId,".");
            if (serviceAndEnv.length > 1) {
                String standardEnvName = environment.getProperty("shuffle.standard-env-name","stable");
                constructorServiceId = serviceAndEnv[0] +"." +standardEnvName;
            }
            List<ServiceInstance> standardInstances = discoveryClient.getInstances(constructorServiceId);
            if (CollectionUtils.isEmpty(standardInstances)){
                log.error("ShuffleFeignLoadBalancerFeignClient#execute, not found server: "+constructorServiceId);
            } else {
                constructorServiceId = targetUrl.replace("#{serviceId}",constructorServiceId);
            }
        }
        request = Request.create(request.method(), constructorServiceId, request.headers(),request.body(), request.charset());
        return super.execute(request, options);
    }
}
