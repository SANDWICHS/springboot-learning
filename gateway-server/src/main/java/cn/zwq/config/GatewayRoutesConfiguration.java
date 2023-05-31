package cn.zwq.config;

import cn.zwq.filter.CustomGatewayFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhangwenqia
 * @create 2022-04-27 17:42
 * @description 类描述
 */
@Configuration
public class GatewayRoutesConfiguration {

	@Bean
	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
		return builder.routes().route(r -> r.
		// 断言（判断条件）
				path("/zwq1/product/**")
				// 目标URI，路由到微服务的地址
				.uri("lb://product-service")
				// 注册自定义网关过滤器
				.filter(new CustomGatewayFilter())
				// 路由ID，唯一
				.id("product-service-CustomGatewayFilter"))
				.route(// 路由ID，唯一
						"order-service-url", r -> r.path("/zwq/order/**").filters(f -> f.stripPrefix(1)).uri("lb://order-service"))
				.route(// 路由ID，唯一
						"product-service-url", r -> r.path("/zwq/product/**").filters(f -> f.stripPrefix(1)).uri("lb://product-service"))
				.build();
	}
}
