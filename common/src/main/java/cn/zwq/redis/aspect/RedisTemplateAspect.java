package cn.zwq.redis.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;

import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dianping.cat.message.Transaction;

/**
 * @author zhangwenqia
 * @create 2022-07-12 19:52
 * @description RedisTemplate注解的调用Cat埋点处理
 */
@Aspect
@Component
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class RedisTemplateAspect {
	Logger logger = org.slf4j.LoggerFactory.getLogger(RedisTemplateAspect.class);

	@Pointcut(value = "execution(* org.springframework.data.redis.core.*RedisTemplate.*(..))")
	public void redisTemplateCut() {
	}

	@Around("redisTemplateCut()")
	public Object feignAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		String traceId = "";
		String name = getClassMethodName(proceedingJoinPoint);
		String params = getParamsAry(proceedingJoinPoint);
		Transaction transaction = CatUtils.newTransaction("Cache.Redis", name);
		try {
			traceId = CatServiceLogUtils.initTraceId();
			if (Boolean.FALSE.equals(StringUtils.isEmpty(params) || "[]".equals(params))) {
				logger.info("参数:{}", params);
			}
			return proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
		} catch (Throwable e) {
			CatUtils.setStatus(transaction, e);
			throw e;
		} finally {
			CatUtils.closeTransaction(transaction);
			CatServiceLogUtils.clearTraceId(traceId);
		}
	}

	private String getParamsAry(ProceedingJoinPoint joinPoint) {
		try {
			return Arrays.toString(joinPoint.getArgs());
		} catch (Exception e) {
			logger.info("获取Redis调用参数异常", e);
		}
		return "";
	}

	private String getClassMethodName(ProceedingJoinPoint joinPoint) {
		try {
			Signature sig = joinPoint.getSignature();
			if ((sig instanceof MethodSignature)) {
				MethodSignature msg = (MethodSignature) sig;
				Object target = joinPoint.getTarget();
				Class<?> targetClass = target.getClass();
				Method currentMethod = targetClass.getMethod(msg.getName(), msg.getParameterTypes());
				return currentMethod.getName();
			}
		} catch (Exception e) {
			logger.info("获取类方法名异常", e);
		}
		return "default";
	}

}
