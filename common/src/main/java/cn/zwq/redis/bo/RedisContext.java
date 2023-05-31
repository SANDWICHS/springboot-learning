package cn.zwq.redis.bo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import cn.zwq.redis.tran.RedisMessageTranManager;

public class RedisContext {
	private int beginSize;
	private boolean openTransation;
	private Stack<Integer> transationStack;
	private List<RedisMessage> messages;
	private Logger logger = org.slf4j.LoggerFactory.getLogger(RedisMessageTranManager.class);
	private RedisTemplate<String, Object> redisTemplate;

	public RedisContext() {
		beginSize = 0;
		openTransation = false;
		transationStack = new Stack<>();
		messages = new ArrayList<>();
	}

	public void doSend(RedisMessage message) {
		if (redisTemplate != null) {
			redisTemplate.convertAndSend(message.getKey(), message.getMessage());
		}
	}

	public void commit() {
		for (RedisMessage message : messages) {
			try {
				doSend(message);
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	public void execute(RedisMessage mtMessage) {
		if (isOpenTransation()) {
			messages.add(mtMessage);
		} else {
			doSend(mtMessage);
		}
	}

	public boolean isOpenTransation() {
		return openTransation;
	}

	public void setOpenTransation(boolean openTransation) {
		this.openTransation = openTransation;
	}

	public Stack<Integer> getTransationStack() {
		return transationStack;
	}

	public void setTransationStack(Stack<Integer> transationStack) {
		this.transationStack = transationStack;
	}

	public List<RedisMessage> getMessages() {
		return messages;
	}

	public void setMessages(List<RedisMessage> messages) {
		this.messages = messages;
	}

	public int getBeginSize() {
		return beginSize;
	}

	public void setBeginSize(int beginSize) {
		this.beginSize = beginSize;
	}

	public RedisTemplate<String, Object> getRedisTemplate() {
		return redisTemplate;
	}

	public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
}
