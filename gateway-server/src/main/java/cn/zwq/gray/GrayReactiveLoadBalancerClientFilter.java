package cn.zwq.gray;

import org.slf4j.Logger;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;

/**
 * @author zhangwenqia
 * @create 2023-03-09 16:18
 * @description 类描述
 */
@Component
public class GrayReactiveLoadBalancerClientFilter extends ReactiveLoadBalancerClientFilter {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(GrayReactiveLoadBalancerClientFilter.class);

	private final static String SCHEME = "lb";

	private final static int LOAD_BALANCER_CLIENT_FILTER_ORDER = 10150;
	@Resource
	private GrayLoadBalancer grayLoadBalancer;
	private final LoadBalancerProperties loadBalancerProperties;

	public GrayReactiveLoadBalancerClientFilter(org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory clientFactory,
			LoadBalancerProperties properties) {
		super(clientFactory, properties);
		this.loadBalancerProperties = properties;
	}

	@Override
	public int getOrder() {
		return LOAD_BALANCER_CLIENT_FILTER_ORDER;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		URI url = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
		String schemePrefix = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_SCHEME_PREFIX_ATTR);

		// 直接放行
		if (url == null || (!SCHEME.equals(url.getScheme()) && !SCHEME.equals(schemePrefix))) {
			return chain.filter(exchange);
		}

		// 保留原始url
		ServerWebExchangeUtils.addOriginalRequestUrl(exchange, url);

		return choose1(exchange).doOnNext(serviceInstanceResponse -> {
			if (!serviceInstanceResponse.hasServer()) {
				throw NotFoundException.create(loadBalancerProperties.isUse404(), "Unable to find instance for " + url.getHost());
			}

			URI uri = exchange.getRequest().getURI();
			// if the `lb:<scheme>` mechanism was used, use `<scheme>` as the default,
			// if the loadbalancer doesn't provide one.
			String overrideScheme = null;
			if (schemePrefix != null) {
				overrideScheme = url.getScheme();
			}

			DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(serviceInstanceResponse.getServer(),
					overrideScheme);

			URI requestUrl = LoadBalancerUriTools.reconstructURI(serviceInstance, uri);

			if (logger.isTraceEnabled()) {
				logger.info("LoadBalancerClientFilter url chosen: " + requestUrl);
			}
			exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, requestUrl);
		}).then(chain.filter(exchange));
	}

	/**
	 * 获取实例
	 *
	 * @param exchange ServerWebExchange
	 * @return ServiceInstance
	 */
	private Mono<Response<ServiceInstance>> choose(ServerWebExchange exchange) {
		URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

		ServiceInstance serviceInstance = grayLoadBalancer.choose(uri.getHost(), exchange.getRequest());

		return Mono.just(new DefaultResponse(serviceInstance));
	}

	private Mono<org.springframework.cloud.client.loadbalancer.DefaultResponse> choose1(ServerWebExchange exchange) {
		URI uri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);

		ServiceInstance serviceInstance = grayLoadBalancer.choose(uri.getHost(), exchange.getRequest());
		return Mono.just(new org.springframework.cloud.client.loadbalancer.DefaultResponse(serviceInstance));
	}
}
