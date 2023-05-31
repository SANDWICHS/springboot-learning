package cn.zwq.redis.tran;

import javax.annotation.Resource;

import cn.zwq.exection.TransactionCommitException;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.zwq.redis.bo.RedisContext;
import cn.zwq.redis.bo.RedisMessage;

@Component
public class RedisMessageTranManager {
	private ThreadLocal<RedisContext> context = init();
	private Logger logger = org.slf4j.LoggerFactory.getLogger(RedisMessageTranManager.class);

	@Resource
	private RedisTemplate<String, Object> redisTemplate;

	private RedisMessageTranManager() {
	}

	private ThreadLocal<RedisContext> init() {
		if (context == null) {
			context = new ThreadLocal<>();
			context.set(new RedisContext());
		} else if (context.get() == null) {
			context.set(new RedisContext());
		}
		return context;
	}

	public void doBegin() {
		init();
		RedisContext ctx = context.get();
		// 防止调用doBegin的次数多于调用doSubmit的次数
		ctx.setBeginSize(ctx.getBeginSize() + 1);
		if (ctx.getBeginSize() >= 5) {
			context.remove();
			throw new TransactionCommitException("redis消息事务嵌套超过5次，可能遗漏调用doSummit！");
		}
		try {
			ctx.getTransationStack().push(1);
			if (ctx.isOpenTransation()) {
				return;
			}
			ctx.setOpenTransation(true);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

	public void doRollback() {
		try {
			context.remove();
		} catch (Exception e) {
			logger.error("消息提交失败：", e);
		}
	}

	public void doCommit() {
		try {
			RedisContext ctx = context.get();
			if (!ctx.isOpenTransation()) {
				return;
			}
			ctx.getTransationStack().pop();
			if (ctx.getTransationStack().isEmpty()) {
				ctx.commit();
				context.remove();
			} else {
				logger.info("非空stack");
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			context.remove();
		}
	}

	public void execute(String key, JSONObject messageObject) {
		execute(key, messageObject.toJSONString());
	}

	public void execute(String key, String message) {
		init();
		RedisContext ctx = context.get();
		ctx.setRedisTemplate(redisTemplate);
		ctx.execute(new RedisMessage(key, message));
	}

	public boolean isOpenTransation() {
		return context.get().isOpenTransation();
	}
}
