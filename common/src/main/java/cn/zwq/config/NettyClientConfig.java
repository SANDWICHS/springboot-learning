package cn.zwq.config;

import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.NettyCustomizer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author zhangwenqia
 * @create 2023-05-29 09:58
 * @description 类描述
 */
@Component
@ConditionalOnProperty(value = "netty.client.config.enabled", havingValue = "true")
public class NettyClientConfig {

	private static Logger logger = LoggerFactory.getLogger(NettyClientConfig.class);

	@Value("${netty.client.config.readerIdleTimeSeconds:60}")
	private int readerIdleTimeSeconds;
	@Value("${netty.client.config.writerIdleTimeSeconds:30}")
	private int writerIdleTimeSeconds;
	@Value("${netty.client.config.allIdleTimeSeconds:0}")
	private int allIdleTimeSeconds;

	@Bean
	public ClientResources clientResources() {
		NettyCustomizer nettyCustomizer = new NettyCustomizer() {
			@Override
			public void afterBootstrapInitialized(Bootstrap bootstrap) {
				logger.info("Bootstrap初始化:{}", bootstrap.toString());
			}

			@Override
			public void afterChannelInitialized(Channel channel) {
				logger.info("{}-Channel初始化:{}", channel.localAddress().toString(), channel.id().toString());
				channel.pipeline().addLast(new IdleStateHandler(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds));
				channel.pipeline().addLast(new ChannelDuplexHandler() {
					@Override
					public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
						if (evt instanceof IdleStateEvent) {
							ctx.disconnect();
						}
					}
				});
			}
		};

		return ClientResources.builder().nettyCustomizer(nettyCustomizer).build();
	}
}
