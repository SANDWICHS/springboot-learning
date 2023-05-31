package cn.zwq.redis.bo;

import java.util.UUID;

import com.alibaba.fastjson.JSON;

/**
 * @author zhangwenqia
 * @create 2022-02-17 10:22
 */
public class Message {
	private String id;
	private Integer retryCount;
	private String content;
	private Integer status;
	private String topic;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(Integer retryCount) {
		this.retryCount = retryCount;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public Message() {
		init();
	}

	public Message(String topic, Object object) {
		init();
		this.content = JSON.toJSONString(object);
		this.topic = topic;
	}

	private void init() {
		this.id = UUID.randomUUID().toString().replace("-", "");
		this.retryCount = 0;
		this.status = 0;
	}

}
