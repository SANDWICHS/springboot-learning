package cn.zwq.service;

/**
 * @author zhangwenqia
 * @create 2022-04-29 10:08
 * @description 类描述
 */
public interface CallInfoService {
	boolean checkLock(String type, Long id);

	void removeRedisKeys();
}
