package cn.zwq.http.aspect;

import cn.zwq.cat.util.CatUtils;
import cn.zwq.util.ThreadLocalUtil;
import com.dianping.cat.Cat;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.dianping.cat.message.Transaction;

/**
 * @author zhangwenqia
 * @create 2022-07-07 19:52
 * @description FeignClient注解的调用Cat埋点处理
 */
@Aspect
@Component
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class OpenFeignAspect {
	private static final Logger logger = LoggerFactory.getLogger(OpenFeignAspect.class);

	@Pointcut("@within(org.springframework.cloud.openfeign.FeignClient)")
	public void feignClientPointcut() {
	}

	// @Pointcut("execution(* cn.zwq.*(..))")
	public void feignApiClientPointcut() {
	}

	@Around("feignClientPointcut()")
	public Object feignAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		try {
			Object result = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
			logger.info("Feign-uri-end");
			Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_TRANSACTION);
			CatUtils.setStatus(catTransaction);
			return result;
		} catch (Throwable e) {
			Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_TRANSACTION);
			// Cat.getProducer().logError(e);
			CatUtils.setStatus(catTransaction, e);
			throw e;
		} finally {
			Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_TRANSACTION);
			CatUtils.closeTransaction(catTransaction);
			ThreadLocalUtil.remove(CatUtils.KEY_TRANSACTION);
			ThreadLocalUtil.checkClear();
		}
	}

	// @Around("feignApiClientPointcut()")
	public Object feignApiAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		try {
			Object result = proceedingJoinPoint.proceed(proceedingJoinPoint.getArgs());
			logger.info("Feign-api-uri-end");
			return result;
		} catch (Throwable e) {
			Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_TRANSACTION);
			Cat.getProducer().logError(e);
			CatUtils.setStatus(catTransaction, e);
			throw e;
		} finally {
			Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_TRANSACTION);
			CatUtils.closeTransaction(catTransaction);
			ThreadLocalUtil.remove(CatUtils.KEY_TRANSACTION);
			ThreadLocalUtil.checkClear();
		}
	}

}
