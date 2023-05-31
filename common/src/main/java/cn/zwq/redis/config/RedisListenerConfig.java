package cn.zwq.redis.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@ConditionalOnProperty(name = "redis.listener.config", havingValue = "true")
public class RedisListenerConfig {
	@Value("${spring.redis.database}")
	private int dataBaseIndex;

	@Bean("nopRedisMessageListenerContainer")
	RedisMessageListenerContainer container(
			RedisConnectionFactory connectionFactory,
			@Qualifier("myRedisRegisterMessageListener") MessageListenerAdapter listenerAdapter) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		// 订阅离线任务下发消息通道
		container.addMessageListener(listenerAdapter, new PatternTopic("KEY_TASK_OFFLINE_MISSION_ID_LIST_" + dataBaseIndex));
		return container;
	}
}
