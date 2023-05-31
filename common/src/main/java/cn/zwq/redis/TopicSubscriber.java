package cn.zwq.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author zhangwenqia
 * @create 2022-02-17 10:36 RedisPublisher
 * @Description Redis订阅管理，采用观察者模式。
 */
@Component
public class TopicSubscriber {
	private final Map<String, Set<String>> subscriberMap = new HashMap<>();
	private RedisTemplate<String, Object> redisTemplate;

	@Autowired
	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// 观察者模式实现消费者注册。
	public Boolean addConsumer(String topic, String consumer) {
		Set<String> consumerList = subscriberMap.get(topic);
		if (consumerList == null) {
			consumerList = new HashSet<>();
			subscriberMap.put(topic, consumerList);
		}
		return consumerList.add(consumer);
	}

	// 观察者模式实现消费者删除。
	public Boolean removeConsumer(String topic, String consumer) {
		Set<String> consumerList = subscriberMap.get(topic);
		if (consumerList == null) {
			return false;
		}
		return consumerList.remove(consumer);
	}

	// 消息广播
	public void broadcast(String topic, String id) {
		if (subscriberMap.get(topic) != null) {
			for (String consumer : subscriberMap.get(topic)) {
				String key = String.join("_", topic, consumer, id);
				if (!redisTemplate.hasKey("fail_" + key)) {
					redisTemplate.opsForValue().set(key, id);
					redisTemplate.opsForList().leftPush(topic + "_" + consumer, topic);
				}
			}
		}
	}
}
