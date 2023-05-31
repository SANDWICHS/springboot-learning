package cn.zwq.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cn.zwq.cat.CatProperties;
import cn.zwq.cat.context.CatContext;
import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import cn.zwq.util.SpringBeanUtils;
import cn.zwq.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

@Service("restInterceptor")
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class CatRestInterceptor implements ClientHttpRequestInterceptor {
	private static Logger logger = LoggerFactory.getLogger(CatRestInterceptor.class);

	private CatProperties catProperties;

	@Autowired
	public void setCatProperties(CatProperties catProperties) {
		this.catProperties = catProperties;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
			ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		final long startTime = System.currentTimeMillis();
		ThreadLocalUtil.put(CatUtils.KEY_REST_START_TIME, startTime);

		String catName = httpRequest.getURI().getPath();
		if (catProperties == null) {
			catProperties = SpringBeanUtils.getBean(CatProperties.class);
		}
		List<String> allInOnePaths = Optional.ofNullable(catProperties).map(value -> value.getAllInOnePaths()).orElseGet(() -> new ArrayList<>());
		catName = CatUtils.getPathName(catName, allInOnePaths);

		Transaction t = CatUtils.getPigeonCallTransaction(catProperties, catName);
		try {
			ThreadLocalUtil.put(CatUtils.KEY_REST_TRANSACTION, t);
			ThreadLocalUtil.put(CatUtils.KEY_REST_URI, httpRequest.getURI().toString());
			if (bytes.length > 0 && bytes.length < 1024) {
				String params = new String(bytes, StandardCharsets.UTF_8);
				logger.info("Rest-uri:{},params：{}", httpRequest.getURI().toString(), params);
			} else {
				logger.info("Rest-uri:{}", httpRequest.getURI().toString());
			}

			final HttpHeaders headers = httpRequest.getHeaders();

			CatContext context = new CatContext();
			String domain = Optional.ofNullable(catProperties).map(value -> value.getDomain()).orElse("zwq");
			Cat.logRemoteCallClient(context, domain); // 声明子节点
			// Cat.logRemoteCallClient(context); // 声明子节点
			headers.add(CatServiceLogUtils.KEY_ROOT, context.getProperty(Cat.Context.ROOT));
			headers.add(CatServiceLogUtils.KEY_PARENT, context.getProperty(Cat.Context.PARENT));
			headers.add(CatServiceLogUtils.KEY_CHILD, context.getProperty(Cat.Context.CHILD));
			// headers.add(CatServiceLogUtils.KEY_TRACE_MODE, "true");
			headers.add(CatServiceLogUtils.KEY_CLIENT_SYSTEM, CatServiceLogUtils.getClientSystem());

			final ClientHttpResponse execute = clientHttpRequestExecution.execute(httpRequest, bytes);
			// long elapsedTime = System.currentTimeMillis() - startTime;
			// logger.info("Rest-uri-end，耗时：{}ms", elapsedTime);

			return execute;
		} catch (Exception e) {
			// Cat.getProducer().logError(e);
			CatUtils.setStatus(t, e);
			CatUtils.closeTransaction(t);
			ThreadLocalUtil.remove(CatUtils.KEY_REST_TRANSACTION);
			ThreadLocalUtil.remove(CatUtils.KEY_REST_START_TIME);
			ThreadLocalUtil.remove(CatUtils.KEY_REST_URI);
			throw e;
		} finally {
			// CatUtils.closeTransaction(t);
		}
	}

}
