package cn.zwq.redis.bo;

public class RedisMessage {
	private String key;
	private String message;

	public String getKey() {
		return key;
	}

	public String getMessage() {
		return message;
	}

	public RedisMessage(String key, String message) {
		this.key = key;
		this.message = message;
	}
}
