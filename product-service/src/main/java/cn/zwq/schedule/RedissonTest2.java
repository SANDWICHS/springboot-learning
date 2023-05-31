package cn.zwq.schedule;

import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import cn.hutool.core.date.DateUtil;

/**
 * @author zhangwenqia
 * @create 2022-07-15 11:26
 * @description 类描述
 */
@Service
public class RedissonTest2 {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(RedissonTest2.class);

	private RedissonClient redissonClient;

	@Autowired
	public void setRedissonClient(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}

	@Scheduled(cron = "*/5 5 1 * * ?")
	public void execute() {
		logger.info("RedissonTest2检查是否能执行调度：{}", DateUtil.date());
		String key = String.format("%s", this.getClass().getName());
		boolean isLock = false;
		RLock lock = redissonClient.getLock(key);

		try {
			isLock = lock.tryLock(5, TimeUnit.SECONDS);
			if (isLock) {
				logger.info("RedissonTest2开始执行调度：{}", DateUtil.date());
				int executeTime = 10;
				while (executeTime > 0) {
					logger.info("RedissonTest2执行{}批次调度：{}", executeTime, DateUtil.date());
					Thread.sleep(5000);
					executeTime--;
				}
			}
		} catch (Exception e) {
			logger.error("RedissonTest2调度执行异常", e);
		} finally {
			if (isLock) {
				lock.unlock();
			}
		}
	}

}
