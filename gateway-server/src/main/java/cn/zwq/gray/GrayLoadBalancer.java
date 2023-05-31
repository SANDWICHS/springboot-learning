package cn.zwq.gray;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * @author zhangwenqia
 * @create 2023-03-09 15:09
 * @description 类描述
 */
public interface GrayLoadBalancer {
	/**
	 * 根据serviceId筛选可用服务
	 * 
	 * @param serviceId 服务ID
	 * @param request   当前请求
	 * @return ServiceInstance
	 */
	ServiceInstance choose(String serviceId, ServerHttpRequest request);
}
