package cn.zwq.redis;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import com.alibaba.fastjson.JSON;

public abstract class RedisListener implements MessageListener {
	protected static Logger log = org.slf4j.LoggerFactory.getLogger(RedisListener.class);

	@Resource
	protected RedisTemplate<String, Object> redisTemplate;

	@Resource
	protected RedisMessageListenerContainer messageListenerContainer;

	@Resource
	protected TopicSubscriber subscriber;

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String name = this.getClass().getSimpleName();
		String topic = new String(message.getChannel());
		String content = new String(message.getBody());

		cn.zwq.redis.bo.Message m = JSON.parseObject(content, cn.zwq.redis.bo.Message.class);
		String key = String.join("_", topic, name, m.getId());
		Object b = redisTemplate.opsForList().rightPop(topic + "_" + name);
		if (b != null && b.equals(m.getTopic())) {
			// T t = JSON.parseObject(m.getContent(), ((ParameterizedType)
			// this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
			handler(m);// 处理redis消息
			// set data expire 使用redis的expire接口直接丢弃消费过的数据
			redisTemplate.expire(key, 1, TimeUnit.NANOSECONDS);
		} else {
			// todo retry
			redisTemplate.opsForValue().set("fail_" + key, content);
		}
	}

	protected abstract void handler(cn.zwq.redis.bo.Message message);
}
