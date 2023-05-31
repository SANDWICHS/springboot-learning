package cn.zwq.redis.bo;

import java.io.Serializable;

/**
 * @author zhangwenqia
 * @create 2022-04-08 11:49
 * @description 类描述
 */
public class RedisExpireData implements Serializable {
	private Object storeData;
	private Long expire;
	private Long dataTime;

	public Object getStoreData() {
		long currentTimeMillis = System.currentTimeMillis();
		if (expire != null && currentTimeMillis - dataTime > expire) {
			return null;
		}
		return storeData;
	}

	public void setStoreData(Object storeData) {
		this.storeData = storeData;
	}

	public Long getExpire() {
		return expire;
	}

	public void setExpire(Long expire) {
		this.expire = expire;
	}

	public Long getDataTime() {
		return dataTime;
	}

	public void setDataTime(Long dataTime) {
		this.dataTime = dataTime;
	}

	public RedisExpireData() {
	}

	public RedisExpireData(Object storeData, Long expire) {
		setStoreData(storeData);
		setExpire(expire);
		setDataTime(System.currentTimeMillis());
	}
}
