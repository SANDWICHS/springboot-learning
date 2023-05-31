package cn.zwq.http;

import cn.zwq.cat.util.CatUtils;
import cn.zwq.util.ThreadLocalUtil;
import com.dianping.cat.message.Transaction;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

/**
 * @author zhangwenqia
 * @create 2023-05-29 11:04
 * @description 类描述
 */
public class CustomRestTemplate extends RestTemplate {
	public CustomRestTemplate() {
	}

	public CustomRestTemplate(ClientHttpRequestFactory requestFactory) {
		setRequestFactory(requestFactory);
	}

	@Override
	protected <T> T doExecute(URI url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor)
			throws RestClientException {
		try {
			final T t = super.doExecute(url, method, requestCallback, responseExtractor);

			return t;
		} catch (RestClientException e) {
			Long startTime = (Long) ThreadLocalUtil.get(CatUtils.KEY_REST_START_TIME);
			if (startTime != null) {
				Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_REST_TRANSACTION);
				CatUtils.setStatus(catTransaction, e);
				CatUtils.closeTransaction(catTransaction);

				ThreadLocalUtil.remove(CatUtils.KEY_REST_START_TIME);
				ThreadLocalUtil.remove(CatUtils.KEY_REST_TRANSACTION);
			}
			throw e;
		} finally {
			Long startTime = (Long) ThreadLocalUtil.get(CatUtils.KEY_REST_START_TIME);
			if (startTime != null) {
				Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_REST_TRANSACTION);
				CatUtils.closeTransaction(catTransaction);

				ThreadLocalUtil.remove(CatUtils.KEY_REST_START_TIME);
				ThreadLocalUtil.remove(CatUtils.KEY_REST_TRANSACTION);
			}
		}
	}
}
