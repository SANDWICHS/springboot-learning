package cn.zwq.gray;

import cn.zwq.util.ConstantUtils;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author zhangwenqia
 * @create 2023-03-09 16:23
 * @description 类描述
 */
@Component
public class VersionGrayLoadBalancer implements GrayLoadBalancer {
	@Resource
	private DiscoveryClient discoveryClient;

	@Override
	public ServiceInstance choose(String serviceId, ServerHttpRequest request) {
		List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

		// 注册中心无示例抛出异常
		if (CollectionUtils.isEmpty(instances)) {
			throw new NotFoundException("No instance available for " + serviceId);
		}

		// 获取请求version,无则随机返回可用示例
		String reqVersion = request.getHeaders().getFirst(ConstantUtils.VERSION);
		if (StringUtils.isEmpty(reqVersion)) {
			return getServiceInstance(instances);
		}

		// 遍历可以实例元数据，若匹配则返回此实例
		List<ServiceInstance> availableList = instances.stream()
				.filter(instance -> reqVersion.equalsIgnoreCase(instance.getMetadata().get(ConstantUtils.VERSION))).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(availableList)) {
			return getServiceInstance(instances);
		}

		return getServiceInstance(availableList);
	}

	private ServiceInstance getServiceInstance(List<ServiceInstance> instances) {
		int i = RandomUtils.nextInt(0, instances.size());
		return instances.get(i);
	}
}
