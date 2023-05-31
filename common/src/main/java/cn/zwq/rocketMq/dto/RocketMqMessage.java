package cn.zwq.rocketMq.dto;

import java.io.Serializable;

/**
 * @author zhangwenqia
 * @create 2022-02-28 15:56
 * @description 类描述
 */
public class RocketMqMessage<T> implements Serializable {
	/**
	 * 消息内容
	 */
	private T content;

	/**
	 * 消息的KEY
	 */
	private String msgKey;

	/**
	 * topic
	 */
	private String producerTopic;

	/**
	 * group
	 */
	private String producerGroup;

	/**
	 * tag
	 */
	private String producerTag;

	private String method;

	public T getContent() {
		return content;
	}

	public void setContent(T content) {
		this.content = content;
	}

	public String getMsgKey() {
		return msgKey;
	}

	public void setMsgKey(String msgKey) {
		this.msgKey = msgKey;
	}

	public String getProducerGroup() {
		return producerGroup;
	}

	public void setProducerGroup(String producerGroup) {
		this.producerGroup = producerGroup;
	}

	public String getProducerTopic() {
		return producerTopic;
	}

	public void setProducerTopic(String producerTopic) {
		this.producerTopic = producerTopic;
	}

	public String getProducerTag() {
		return producerTag;
	}

	public void setProducerTag(String producerTag) {
		this.producerTag = producerTag;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
