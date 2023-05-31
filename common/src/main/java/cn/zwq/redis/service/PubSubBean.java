package cn.zwq.redis.service;

import javax.annotation.Resource;

import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PubSubBean {

	@Resource
	RedisTemplate<String, Object> redisTemplate;

	/*
	 * 发布
	 */
	public void publish(String key, String value) {
		redisTemplate.execute((RedisCallback<Object>) redisConnection -> {
			redisConnection.publish(key.getBytes(), value.getBytes());
			return null;
		});
	}

	/*
	 * 订阅
	 */
	public void subscribe(MessageListener messageListener, String key) {
		redisTemplate.execute((RedisCallback<Object>) redisConnection -> {
			redisConnection.subscribe(messageListener, key.getBytes());
			return null;
		});
	}
}
