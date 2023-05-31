package cn.zwq.util;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import cn.zwq.cat.CatProperties;
import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.redis.RedisUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MdcUtils {

	public static final String MDC_KEY_USER = "user";
	public static final String MDC_KEY_SESSINO_ID = "sessionid";
	public static final String MDC_KEY_CLIENT_IP = "client_ip";
	public static final String MDC_KEY_TOKEN = "token";
	public static final String MDC_TASK_ID = "task_id";

	private static final Logger logger = LoggerFactory.getLogger(MdcUtils.class);

	@PostConstruct
	public void init() {
	}

	private MdcUtils() {
		super();
	}

	public static void setMdc(HttpServletRequest httpRequest) {
		if (httpRequest == null) {
			return;
		}

		setSessionId(httpRequest);

		setUser(null, httpRequest);

		setClientIp(httpRequest);
	}

	public static void setMdc(HttpServletRequest httpRequest, String token) {
		if (httpRequest == null) {
			return;
		}

		setSessionId(httpRequest);

		setUser(token, httpRequest);

		setClientIp(httpRequest);
	}

	private static void setUser(String token, HttpServletRequest httpRequest) {
		if (StringUtils.isEmpty(MDC.get(MDC_KEY_USER))) {
			final String user;
			if (StringUtils.isEmpty(token)) {
				user = getUser(httpRequest);
			} else {
				user = getUser(token);
			}
			if (StringUtils.isNotEmpty(user)) {
				MDC.put(MDC_KEY_USER, user);
			}
		}
	}

	private static void setClientIp(HttpServletRequest httpRequest) {
		if (StringUtils.isEmpty(MDC.get(MDC_KEY_CLIENT_IP))) {
			final String clientIp = getClientIp(httpRequest);
			if (StringUtils.isNotEmpty(clientIp)) {
				MDC.put(MDC_KEY_CLIENT_IP, clientIp);
			}
		}
	}

	private static void setSessionId(HttpServletRequest httpRequest) {
		if (StringUtils.isEmpty(MDC.get(MDC_KEY_SESSINO_ID))) {
			final String sessionId = getSessionId(httpRequest);
			if (StringUtils.isNotEmpty(sessionId)) {
				MDC.put(MDC_KEY_SESSINO_ID, sessionId);
			}
		}
	}

	public static String getClientIp(HttpServletRequest httpRequest) {
		if (httpRequest == null || httpRequest.getRemoteAddr() == null) {
			return "";
		}
		return httpRequest.getRemoteAddr();

	}

	private static String getUser(HttpServletRequest httpRequest) {
		if (httpRequest == null) {
			return "";
		}
		String token = RequestTokenGetUtils.getTokenFromRequest(httpRequest);
		return getUser(token);
	}

	private static String getUser(String token) {
		try {
			if (StringUtils.isEmpty(token)) {
				return "";
			}

			MDC.put(MDC_KEY_TOKEN, token);

			if (SpringBeanUtils.containsBean("redisTemplate")) {
				RedisTemplate redisTemplate = (RedisTemplate) SpringBeanUtils.getBean("redisTemplate");
				return (String) RedisUtils.get(redisTemplate, "token");
			}
		} catch (Exception e) {
			logger.warn("获取用户出错", e);
		}
		return "";

	}

	public static String getSessionId(HttpServletRequest httpRequest) {
		if (httpRequest == null) {
			return "";
		}

		HttpSession session = httpRequest.getSession();
		if (session == null || session.getId() == null) {
			return "";
		}
		return session.getId();
	}

	public static String getMdcToken() {
		return MDC.get(MdcUtils.MDC_KEY_TOKEN);
	}

	public static void cleanMdc() {
		MDC.remove(MdcUtils.MDC_KEY_USER);
		MDC.remove(MdcUtils.MDC_KEY_SESSINO_ID);
		MDC.remove(MdcUtils.MDC_KEY_CLIENT_IP);
		MDC.remove(MdcUtils.MDC_KEY_TOKEN);
	}

	public static void setTaskId(String taskId) {
		MDC.put(MDC_TASK_ID, taskId);
	}

	public static String getTaskId() {
		return MDC.get(MDC_TASK_ID);
	}

	public static void clearTaskId() {
		MDC.remove(MDC_TASK_ID);
	}

}
