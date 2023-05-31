package cn.zwq.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.zwq.redis.RedisUtils;
import cn.zwq.service.CallInfoService;
import cn.zwq.util.GlobalVariableUtil;
import cn.zwq.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author zhangwenqia
 * @create 2022-05-11 9:54
 * @description 调用此类的方法必须加@CallInfo注解
 */
@Service
public class CallInfoServiceImpl implements CallInfoService {
	Logger logger = org.slf4j.LoggerFactory.getLogger(CallInfoServiceImpl.class);

	private String getRedisKey(String type, Long id) {
		return String.format("%s:%s", type, id);
	}

	/** redis 客户端 */
	private final RedisTemplate<String, Object> redisTemplate;

	@Autowired
	public CallInfoServiceImpl(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public boolean checkLock(String type, Long id) {
		String redisInfo = String.format("%s", id);
		try {
			String redisKey = getRedisKey(type, id);
			boolean lock = RedisUtils.setIfAbsent(redisTemplate, redisKey, redisInfo, 12, TimeUnit.HOURS);
			if (lock) {
				List<String> redisKeyList = (List<String>) ThreadLocalUtil.get(GlobalVariableUtil.KEY_REDIS_LIST);
				if (redisKeyList == null) {
					redisKeyList = new ArrayList<>(10);
					ThreadLocalUtil.put(GlobalVariableUtil.KEY_REDIS_LIST, redisKeyList);
				}
				redisKeyList.add(redisKey);
			}
			return lock;
		} catch (Exception e) {
			logger.error("尝试Redis加锁异常", e);
			throw e;
		}
	}

	@Override
	public void removeRedisKeys() {
		List<String> redisKeyList = (List<String>) ThreadLocalUtil.get(GlobalVariableUtil.KEY_REDIS_LIST);
		if (redisKeyList != null) {
			for (String redisKey : redisKeyList) {
				try {
					RedisUtils.remove(redisTemplate, redisKey);
				} catch (Exception e) {
					logger.warn("删除redis的key值异常", e);
				}
			}
		}
	}
}
