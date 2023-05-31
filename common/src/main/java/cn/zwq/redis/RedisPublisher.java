package cn.zwq.redis;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import cn.hutool.core.util.CharsetUtil;
import cn.zwq.redis.bo.Message;

/**
 * @author zhangwenqia
 * @create 2022-02-17 10:47
 * @Description Redis发布者。
 */
@Component
public class RedisPublisher {
	private RedisTemplate<String, Object> redisTemplate;
	private TopicSubscriber topicSubscriber;

	@Autowired
	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Autowired
	public void setTopicSubscriber(TopicSubscriber topicSubscriber) {
		this.topicSubscriber = topicSubscriber;
	}

	@PostConstruct
	public void init() {

	}

	public void publish(String topic, Object content) {
		Message message = new Message(topic, content);
		topicSubscriber.broadcast(topic, message.getId());
		redisTemplate.getConnectionFactory().getConnection().publish(topic.getBytes(CharsetUtil.CHARSET_UTF_8),
				JSON.toJSONString(message).getBytes());
	}
}
