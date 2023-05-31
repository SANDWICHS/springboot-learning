package cn.zwq.exection;

import cn.zwq.cat.util.CatUtils;
import cn.zwq.entities.CommonResults;
import cn.zwq.util.ThreadLocalUtil;
import com.dianping.cat.message.Transaction;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * @author zhangwenqia
 * @create 2023-05-26 14:52
 * @description 类描述
 */
@RestControllerAdvice
public class ResponseExceptionAdvice implements ResponseBodyAdvice {
	@Override
	public boolean supports(MethodParameter returnType, Class converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		return body;
	}

	@ExceptionHandler(value = Exception.class)
	@ResponseBody
	public CommonResults handlerGlobeException(HttpServletRequest request, Exception exception) {
		Long startTime = (Long) ThreadLocalUtil.get(CatUtils.KEY_REST_START_TIME);
		if (startTime != null) {
			Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_REST_TRANSACTION);
			CatUtils.setStatus(catTransaction, exception);
			CatUtils.closeTransaction(catTransaction);

			ThreadLocalUtil.remove(CatUtils.KEY_REST_START_TIME);
			ThreadLocalUtil.remove(CatUtils.KEY_REST_TRANSACTION);
		}
		return new CommonResults(-1, "fail", "调用异常：Globe");
	}
}
