package cn.zwq.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import cn.zwq.util.ThreadLocalUtil;
import com.dianping.cat.message.Transaction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import static cn.zwq.cat.util.CatUtils.*;

@Configuration
@ConditionalOnWebApplication
public class RestTemplateAutoConfiguration {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(RestTemplateAutoConfiguration.class);

	@Bean
	@Primary
	@LoadBalanced
	public CustomRestTemplate clusterRestTemplate() {
		return createRestTemplate();
	}

	@Bean
	public CustomRestTemplate singleRestTemplate() {
		return createRestTemplate();
	}

	@Bean
	public CustomRestTemplate singleProxyRestTemplate() {
		return createProxyRestTemplate();
	}

	@Value("${restTemplate.connectTimeOut:-1}")
	private int connectTimeOut;

	@Value("${restTemplate.readTimeOut:-1}")
	private int readTimeOut;

	@Qualifier("restInterceptor")
	@Autowired(required = false)
	private ClientHttpRequestInterceptor interceptor;

	private void setInterceptor(RestTemplate restTemplate) {
		if (interceptor != null) {
			restTemplate.setInterceptors(Collections.singletonList(interceptor));
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
				@Override
				public boolean hasError(ClientHttpResponse response) throws IOException {
					Transaction t = (Transaction) ThreadLocalUtil.get(KEY_REST_TRANSACTION);
					boolean hasError = true;
					try {
						hasError = super.hasError(response);
						if (!hasError && t != null) {
							Long startTime = (Long) ThreadLocalUtil.get(KEY_REST_START_TIME);
							long costTime = System.currentTimeMillis() - Optional.ofNullable(startTime).orElse(0l);
							String uri = (String) ThreadLocalUtil.get(KEY_REST_URI);
							String info = String.format("Rest-uri-end:%s，耗时：%sms", uri, costTime);
							logger.info(info);
							t.setSuccessStatus();
							t.complete();
						}
						return hasError;
					} catch (Exception e) {
						return true;
					} finally {
						if (!hasError) {
							ThreadLocalUtil.remove(KEY_REST_TRANSACTION);
							ThreadLocalUtil.remove(KEY_REST_START_TIME);
							ThreadLocalUtil.remove(KEY_REST_URI);
						}
					}
				}

				@Override
				public void handleError(ClientHttpResponse response) throws IOException {
					Transaction t = (Transaction) ThreadLocalUtil.get(KEY_REST_TRANSACTION);
					try {
						super.handleError(response);
					} catch (Exception e) {
						if (t != null) {
							Long startTime = (Long) ThreadLocalUtil.get(KEY_REST_START_TIME);
							long costTime = System.currentTimeMillis() - Optional.ofNullable(startTime).orElse(0l);
							String uri = (String) ThreadLocalUtil.get(KEY_REST_URI);
							String info = String.format("Rest-uri-end:%s，耗时：%sms", uri, costTime);
							logger.info(info);
							t.setStatus(e);
							t.complete();
						} else {
							logger.error("Execption [" + e.getMessage() + "] occurred while trying to send the request", e);
						}
					} finally {
						ThreadLocalUtil.remove(KEY_REST_TRANSACTION);
						ThreadLocalUtil.remove(KEY_REST_START_TIME);
						ThreadLocalUtil.remove(KEY_REST_URI);
					}
				}
			});
		}
	}

	public CustomRestTemplate createRestTemplate() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(connectTimeOut);
		requestFactory.setReadTimeout(readTimeOut);
		CustomRestTemplate restTemplate = new CustomRestTemplate(requestFactory);
		setInterceptor(restTemplate);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		return restTemplate;
	}

	public CustomRestTemplate createProxyRestTemplate() {
		CustomRestTemplate restTemplate;
		if (StringUtils.isEmpty(proxyHostName)) {
			restTemplate = new CustomRestTemplate();
		} else {
			SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
			httpRequestFactory.setConnectTimeout(connectTimeOut);
			httpRequestFactory.setReadTimeout(readTimeOut);
			httpRequestFactory.setProxy(
					new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHostName, proxyPort)));
			restTemplate = new CustomRestTemplate(httpRequestFactory);
		}
		setInterceptor(restTemplate);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		return restTemplate;
	}

	public String getProxyHostName() {
		return proxyHostName;
	}

	public void setProxyHostName(String proxyHostName) {
		this.proxyHostName = proxyHostName;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	@Value("${proxy.hostName:}")
	private String proxyHostName;

	@Value("${proxy.port:}")
	private Integer proxyPort;

}
