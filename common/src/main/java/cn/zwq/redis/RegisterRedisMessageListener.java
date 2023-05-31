package cn.zwq.redis;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

@Component("myRedisRegisterMessageListener")
@ConditionalOnProperty(name = "redis.listener.config", havingValue = "true")
public class RegisterRedisMessageListener extends MessageListenerAdapter {
	private static Logger log = org.slf4j.LoggerFactory.getLogger(RegisterRedisMessageListener.class);

	@Autowired
	RedisTemplate<String, Object> redisTemplate;

	@Autowired
	@Qualifier("redisMessageReceiver")
	private RedisMessageReceiver redisMessageReceiver;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		RedisSerializer<String> serialize = redisTemplate.getStringSerializer();
		String info = serialize.deserialize(message.getBody());
		String channel = serialize.deserialize(message.getChannel());
		log.info("收到redis通道{}的消息：{}", channel, info);

		redisMessageReceiver.workflowChanged(info);

	}

}
