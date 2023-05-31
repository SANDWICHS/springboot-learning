package cn.zwq.cat;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class CatFilter implements GlobalFilter, Ordered {
	private static final Logger logger = LoggerFactory.getLogger(CatFilter.class);

	private CatProperties catProperties;

	@Autowired
	public void setCatProperties(CatProperties catProperties) {
		this.catProperties = catProperties;
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		HttpHeaders requestHeader = getRequestHeader(exchange);
		CatServiceLogUtils.logRemoteCallServer(requestHeader);

		String pathName = getPath(exchange);

		String type = catProperties.getClientSystem() + "-Gateway";

		Transaction t = Cat.newTransaction(type, CatServiceLogUtils.getPathName(pathName, catProperties));

		String traceId = "";
		try {
			Cat.logEvent("service.method", exchange.getRequest().getMethodValue(), Message.SUCCESS,
					exchange.getRequest().getURI().toString());
			Cat.logEvent("service.client", exchange.getRequest().getRemoteAddress().getHostString());

			traceId = CatServiceLogUtils.initTraceId(catProperties.getTraceId());

			logger.info("gateway 转发：{}", pathName);
			CatContext context = new CatContext();
			String domain = Optional.ofNullable(catProperties.getDomain()).orElseGet(() -> "zwq");
			Cat.logRemoteCallClient(context, domain); // 声明有子节点
			String catId = context.getProperty(Cat.Context.CHILD);
			String parent = context.getProperty(Cat.Context.PARENT);
			String catRootId = context.getProperty(Cat.Context.ROOT);
			List<String> strings = requestHeader.get(CatServiceLogUtils.KEY_CLIENT_SYSTEM);
			String clientSystem = Optional.ofNullable(strings).map(item -> item.get(0)).orElse(null);
			exchange = setCatHeader(exchange, catId, parent, catRootId, clientSystem);

			Mono<Void> result = chain.filter(exchange);
			t.setSuccessStatus();
			return result;
		} catch (Exception e) {
			t.setStatus(e);
			throw e;
		} finally {
			t.complete();
			CatServiceLogUtils.clearTraceId(traceId);
		}
	}

	private HttpHeaders getRequestHeader(ServerWebExchange exchange) {
		return exchange.getRequest().getHeaders();
	}

	private String getPath(ServerWebExchange exchange) {
		return exchange.getRequest().getPath().value();
	}

	private ServerWebExchange setCatHeader(ServerWebExchange exchange, String catId, String parent, String catRootId, String clientSystem) {
		ServerHttpRequest request = exchange.getRequest().mutate()
				.header(CatServiceLogUtils.KEY_CHILD, catId)
				.header(CatServiceLogUtils.KEY_PARENT, parent)
				.header(CatServiceLogUtils.KEY_ROOT, catRootId)
				.header(CatServiceLogUtils.KEY_TRACE_MODE, "true")
				.header(CatServiceLogUtils.KEY_CLIENT_SYSTEM, clientSystem)
				.build();
		return exchange.mutate().request(request).build();
	}

}
