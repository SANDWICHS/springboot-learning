package cn.zwq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * @author zhangwenqia
 * @create 2022-07-18 14:22
 * @description 类描述
 */
@Configuration
@EnableScheduling
@ConditionalOnExpression("${scheduled.enabled:false}")
public class ScheduleConfig implements SchedulingConfigurer {
	@Value("${scheduled.poolSize:10}")
	private int poolSize;

	@Bean(destroyMethod = "shutdown")
	public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(poolSize);
		// 设置线程名前缀
		scheduler.setThreadNamePrefix("scheduled-task-");
		// 等待所有线程执行完
		scheduler.setWaitForTasksToCompleteOnShutdown(true);
		return scheduler;
	}

	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = taskScheduler();
		taskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
	}
}
