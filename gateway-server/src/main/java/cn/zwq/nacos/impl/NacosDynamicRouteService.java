package cn.zwq.nacos.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import cn.zwq.filter.AccessGlobalFilter;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author zhangwenqia
 * @create 2022-04-28 10:23
 * @description 使用nacos实现动态路由，以上两种方式都是实现的静态配置路径，只能应对部分场景，接下来配置nacos实现动态配置以及配置的存储，由于gateWay并没有适配nacos，需要自定义监听器：
 */
@Component
public class NacosDynamicRouteService implements ApplicationEventPublisherAware {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(AccessGlobalFilter.class);
	private String dataId = "gateway-router.json";
	private String group = "DEFAULT_GROUP";
	@Value("${spring.cloud.nacos.config.server-addr}")
	private String serverAddr;

	@Autowired
	private RouteDefinitionWriter routeDefinitionWriter;

	private static final List<String> ROUTE_LIST = new ArrayList<>();

	@PostConstruct
	public void dynamicRouteByNacosListener() {
		try {
			ConfigService configService = NacosFactory.createConfigService(serverAddr);
			configService.getConfig(dataId, group, 5000);
			configService.addListener(dataId, group, new Listener() {
				@Override
				public void receiveConfigInfo(String configInfo) {
					clearRoute();
					try {
						if (StringUtil.isNullOrEmpty(configInfo)) {// 配置被删除
							return;
						}
						List<RouteDefinition> gatewayRouteDefinitions = JSONObject.parseArray(configInfo, RouteDefinition.class);
						for (RouteDefinition routeDefinition : gatewayRouteDefinitions) {
							addRoute(routeDefinition);
						}
						publish();
					} catch (Exception e) {
						logger.error("receiveConfigInfo error" + e);
					}
				}

				@Override
				public Executor getExecutor() {
					return null;
				}
			});
		} catch (NacosException e) {
			logger.error("dynamicRouteByNacosListener error" + e);
		}
	}

	private void clearRoute() {
		for (String id : ROUTE_LIST) {
			this.routeDefinitionWriter.delete(Mono.just(id)).subscribe();
		}
		ROUTE_LIST.clear();
	}

	private void addRoute(RouteDefinition definition) {
		try {
			routeDefinitionWriter.save(Mono.just(definition)).subscribe();
			ROUTE_LIST.add(definition.getId());
		} catch (Exception e) {
			logger.error("addRoute error" + e);
		}
	}

	private void publish() {
		this.applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this.routeDefinitionWriter));
	}

	private ApplicationEventPublisher applicationEventPublisher;

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
}
