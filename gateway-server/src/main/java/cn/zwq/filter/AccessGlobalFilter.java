package cn.zwq.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * @author zhangwenqia
 * @create 2022-04-27 17:48
 * @description 鉴权过滤器
 */
//@Component
public class AccessGlobalFilter implements GlobalFilter, Ordered {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(AccessGlobalFilter.class);

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 获取请求参数
		String token = exchange.getRequest().getQueryParams().getFirst("token");
		// 业务逻辑处理
		if (StringUtils.isBlank(token)) {
			token = exchange.getRequest().getHeaders().getFirst("token");
			if (StringUtils.isBlank(token)) {
				logger.warn("token is null...");
				ServerHttpResponse response = exchange.getResponse();
				// 响应类型
				response.getHeaders().add("Content-Type", "application/json;charset=utf-8");
				// 响应状态码，HTTP401错误代表用户没有访问权限
				response.setStatusCode(HttpStatus.UNAUTHORIZED);
				// 响应内容
				String message = "{\"message\":\"" + HttpStatus.UNAUTHORIZED.getReasonPhrase() + "\"}";
				DataBuffer buffer = response.bufferFactory().wrap(message.getBytes());
				// 请求结束,不在继续往下请求
				return response.writeWith(Mono.just(buffer));
			}
		}
		logger.info("token is OK!");
		return chain.filter(exchange);// 继续往下执行
	}

	@Override
	public int getOrder() {
		return 1;
	}
}
