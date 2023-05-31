package cn.zwq.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author zhangwenqia
 * @create 2022-04-27 17:48
 * @description 自定义全局过滤器
 */
//@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		System.out.println("自定义全局过滤器被执行");
		return chain.filter(exchange);// 继续往下执行
	}

	@Override
	public int getOrder() {
		return 3;
	}
}
