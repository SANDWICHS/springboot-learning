package cn.zwq.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import cn.zwq.cat.CatProperties;
import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import cn.zwq.constant.SessionConstants;
import cn.zwq.util.MdcUtils;
import cn.zwq.util.SpringBeanUtils;
import cn.zwq.util.ThreadLocalUtil;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.MessageProducer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

@Component
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class CustomCatFilter implements Filter, EnvironmentAware {

	private static final Logger logger = LoggerFactory.getLogger(CustomCatFilter.class);

	private CatProperties catProperties;

	@Autowired
	public void setCatProperties(CatProperties catProperties) {
		this.catProperties = catProperties;
	}

	// 用于创建MultipartHttpServletRequest
	private MultipartResolver multipartResolver = null;

	private String[] urlPatterns = new String[0];

	@Override
	public void init(FilterConfig filterConfig) {
		// 注入bean
		try {
			multipartResolver = ((MultipartResolver) SpringBeanUtils.getBean("multipartResolver"));
		} catch (Exception e) {
			// donothing
		}
		final String catHttpModuleUrlPatterns = filterConfig.getInitParameter("CatHttpModuleUrlPatterns");
		if (StringUtils.isNotEmpty(catHttpModuleUrlPatterns)) {
			urlPatterns = catHttpModuleUrlPatterns.trim().split(",");
			for (int i = 0; i < urlPatterns.length; i++) {
				urlPatterns[i] = urlPatterns[i].trim();
			}
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		CatServiceLogUtils.logRemoteCallServer(httpRequest);
		if (catProperties == null) {
			catProperties = SpringBeanUtils.getBean(CatProperties.class);
		}
		// String catName = httpRequest.getRequestURI();
		String catName = httpRequest.getRequestURL().toString();
		for (String urlPattern : urlPatterns) {
			if (catName.startsWith(urlPattern)) {
				catName = urlPattern;
			}
		}
		String clientSystem = httpRequest.getHeader(CatServiceLogUtils.KEY_CLIENT_SYSTEM);

		String type = CatUtils.getTransactionType(catProperties, catName, clientSystem);
		List<String> allInOnePaths = Optional.ofNullable(catProperties).map(value -> value.getAllInOnePaths()).orElseGet(() -> new ArrayList<>());
		catName = CatUtils.getPathName(catName, allInOnePaths);

		Transaction t;
		if (CatUtils.TYPE_PIGEON_SERVICE.equals(type)) {
			final MessageProducer producer = Cat.getProducer();
			t = producer.newTransaction(type, catName);
		} else {
			t = Cat.newTransaction(type, catName);
		}
		String traceId = "";
		try {
			Cat.logEvent("service.method", ((HttpServletRequest) request).getMethod(), Message.SUCCESS,
					((HttpServletRequest) request).getRequestURL().toString());
			Cat.logEvent("service.client", request.getRemoteHost());

			traceId = CatServiceLogUtils.initTraceId();
			// 记录一个事件
			Cat.logEvent("URL.Server", "", Event.SUCCESS, "");
			// 记录一个业务指标，记录次数
			Cat.logMetricForCount("URL");
			// 记录一个业务指标
			Cat.logMetricForSum("URL.Sum", 1);

			String contentType = request.getContentType();

			StringBuffer url = httpRequest.getRequestURL();
			String query = httpRequest.getQueryString();
			Optional.ofNullable(query).ifPresent(value -> url.append("?").append(value));
			logger.info("url:{}", url.toString());

			// 2021年7月27日 修复form-data传参丢失参数bug
			if (multipartResolver != null && contentType != null && contentType.contains("multipart/form-data")) {
				logger.info("开始接收文件流");
				MultipartHttpServletRequest multipartRequest = getMultipartRequest(request);
				logger.info("结束接收文件流");
				String token = getTokenFromRequest(multipartRequest);
				MdcUtils.setMdc(httpRequest, token);
				// 把multipartRequest让请求继续执行，之后的所有拦截器和controller都能继续get参数
				chain.doFilter(multipartRequest, response);
			} else {
				MdcUtils.setMdc(httpRequest);
				chain.doFilter(request, response);
			}
			t.setSuccessStatus();
		} catch (Exception e) {
			errorControllerComplete(e);
			CatUtils.setStatus(t, e);
			Cat.logError(e);
			throw e;
		} finally {
			CatUtils.closeTransaction(t);
			CatServiceLogUtils.clearTraceId(traceId);
			MdcUtils.cleanMdc();
		}
	}

	private void errorControllerComplete(Exception e) {
		Long startTime = (Long) ThreadLocalUtil.get(CatUtils.KEY_REST_START_TIME);
		if (startTime != null) {
			String uri = (String) ThreadLocalUtil.get(CatUtils.KEY_REST_URI);
			long costTime = System.currentTimeMillis() - Optional.ofNullable(startTime).orElse(0l);
			logger.info("uri:{}，耗时：{}ms", uri, costTime);
			Transaction catTransaction = (Transaction) ThreadLocalUtil.get(CatUtils.KEY_REST_TRANSACTION);
			CatUtils.setStatus(catTransaction, e);
			CatUtils.closeTransaction(catTransaction);

			ThreadLocalUtil.remove(CatUtils.KEY_REST_START_TIME);
			ThreadLocalUtil.remove(CatUtils.KEY_REST_TRANSACTION);
			ThreadLocalUtil.remove(CatUtils.KEY_REST_URI);
		}
	}

	private MultipartHttpServletRequest getMultipartRequest(ServletRequest request) {
		Transaction t = Cat.newTransaction(CatUtils.TYPE_FILE_SERVICE, "getMultipartRequest");
		t.setSuccessStatus();
		try {
			logger.info("开始获取文件流数据");
			return multipartResolver.resolveMultipart((HttpServletRequest) request);
		} catch (Exception e) {
			t.setStatus(e);
			throw e;
		} finally {
			logger.info("结束获取文件流数据");
			t.complete();
		}
	}

	private String getTokenFromRequest(MultipartHttpServletRequest multipartRequest) {
		String token = multipartRequest.getParameter(SessionConstants.TOKEN);
		if (StringUtils.isBlank(token)) {
			token = multipartRequest.getHeader(SessionConstants.TOKEN);
		}
		return token;
	}

	@Override
	public void setEnvironment(Environment environment) {
		String applicationName = environment.getProperty("spring.application.name");
		logger.info("项目名称：{}", applicationName);
	}
}
