package cn.zwq.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author zhangwenqia
 * @create 2022-04-27 17:38
 * @description 类描述
 */
public class CustomGatewayFilter implements GatewayFilter, Ordered {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		System.out.println("自定义网关过滤器被执行");
		return chain.filter(exchange);
	}

	/**
	 * 过滤器执行顺序,数值越小，优先级越高
	 */
	@Override
	public int getOrder() {
		return 2;
	}
}
