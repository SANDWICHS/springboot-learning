package cn.zwq.cat.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import cn.zwq.cat.CatProperties;
import cn.zwq.cat.context.CatContext;
import cn.zwq.util.SpringBeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageTree;

public class CatServiceLogUtils {

	private CatServiceLogUtils() {
		super();
	}

	private static Logger logger = LoggerFactory.getLogger(CatServiceLogUtils.class);

	private static final MessageIdFactory CAT_MESSAGEID_FACTORY = new MessageIdFactory();

	public static final String TRACE_ID = getTraceIdKey();

	static {
		try {
			Cat.enableMultiInstances();
			String s = UUID.randomUUID().toString().replace("-", "");
			CAT_MESSAGEID_FACTORY.initialize(s);

		} catch (Exception e) {
			logger.error("初始化CAT ID生成器异常", e);
		}
	}

	public static final String KEY_CHILD = "X-CAT-ID";
	public static final String KEY_PARENT = "X-CAT-PARENT-ID";
	public static final String KEY_ROOT = "X-CAT-ROOT-ID";
	public static final String KEY_TRACE_MODE = "X-CAT-TRACE-MODE";
	public static final String KEY_CLIENT_SYSTEM = "X-CAT-CLIENT-SYSTEM";

	private static String getTraceIdKey() {
		CatProperties catProperties = SpringBeanUtils.getBean(CatProperties.class);
		if (catProperties != null && StringUtils.isNotEmpty(catProperties.getTraceId())) {
			return catProperties.getTraceId();
		}
		return "zwqTraceId";
	}

	public static String getClientSystem() {
		CatProperties catProperties = SpringBeanUtils.getBean(CatProperties.class);
		if (catProperties != null && StringUtils.isNotEmpty(catProperties.getClientSystem())) {
			return catProperties.getClientSystem();
		}
		return "zwq";
	}

	public static String getDomain() {
		CatProperties catProperties = SpringBeanUtils.getBean(CatProperties.class);
		if (catProperties != null && StringUtils.isNotEmpty(catProperties.getDomain())) {
			return catProperties.getClientSystem();
		}
		return "zwq";
	}

	public static List<String> getAllInOnePaths() {
		CatProperties catProperties = SpringBeanUtils.getBean(CatProperties.class);
		if (catProperties != null) {
			return catProperties.getAllInOnePaths();
		}
		return new ArrayList<>();
	}

	public static String initTraceId() {
		String messageId = gainMessageId();
		if (MDC.get(TRACE_ID) == null) {
			MDC.put(TRACE_ID, messageId);
			return messageId;
		}
		return "";
	}

	public static void clearTraceId(String id) {
		if (StringUtils.isNotBlank(id) && id.equals(MDC.get(TRACE_ID))) {
			MDC.remove(TRACE_ID);
		}
	}

	private static String gainMessageId() {
		try {
			MessageTree tree = Cat.getManager().getThreadLocalMessageTree();
			if (tree == null) {
				return CAT_MESSAGEID_FACTORY.getNextId();
			}
			if (tree.getRootMessageId() != null) {
				return tree.getRootMessageId();
			}
			String messageId = tree.getMessageId();
			if (messageId == null) {
				messageId = Cat.createMessageId();
				tree.setMessageId(messageId);
			}
			return messageId;
		} catch (Exception ignore) {
			logger.error("获取CAT的MESSAGEID异常", ignore);
		}
		return "UNKNOW";
	}

	public static void logRemoteCallServer(HttpServletRequest httpRequest) {
		if (httpRequest.getHeader(KEY_ROOT) == null) {
			Cat.logRemoteCallClient(new CatContext(), getDomain());
		} else {
			String catMyselfId = httpRequest.getHeader(KEY_CHILD);
			String catParentId = httpRequest.getHeader(KEY_PARENT);
			String catRootId = httpRequest.getHeader(KEY_ROOT);

			CatContext catContext = new CatContext();
			catContext.addProperty(Context.CHILD, catMyselfId);
			catContext.addProperty(Context.PARENT, catParentId);
			catContext.addProperty(Context.ROOT, catRootId);
			Cat.logRemoteCallServer(catContext);
		}
	}

}
