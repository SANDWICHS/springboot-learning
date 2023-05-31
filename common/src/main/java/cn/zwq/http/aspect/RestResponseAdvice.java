package cn.zwq.http.aspect;

import cn.zwq.cat.util.CatUtils;
import cn.zwq.util.ThreadLocalUtil;
import com.dianping.cat.message.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Optional;

/**
 * @author zhangwenqia
 * @create 2023-05-22 18:26
 * @description 类描述
 */
@RestControllerAdvice
public class RestResponseAdvice implements ResponseBodyAdvice<Object> {
	private static final Logger logger = LoggerFactory.getLogger(RestResponseAdvice.class);

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		logger.info("support");
		return ThreadLocalUtil.get(CatUtils.KEY_REST_START_TIME) != null;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
		Long startTime = (Long) ThreadLocalUtil.get(CatUtils.KEY_REST_START_TIME);
		String uri = (String) ThreadLocalUtil.get(CatUtils.KEY_REST_URI);
		final String currentUri = request.getURI().toString();
		if (startTime != null) {
			long costTime = System.currentTimeMillis() - Optional.ofNullable(startTime).orElse(0l);
			logger.info("Rest-uri-end-advice:{}，耗时：{}ms", request.getURI().toString(), costTime);
			Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_REST_TRANSACTION);
			CatUtils.closeTransaction(catTransaction);

			ThreadLocalUtil.remove(CatUtils.KEY_REST_START_TIME);
			ThreadLocalUtil.remove(CatUtils.KEY_REST_TRANSACTION);
		}
		return body;
	}

}
