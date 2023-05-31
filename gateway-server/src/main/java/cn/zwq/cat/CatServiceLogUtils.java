package cn.zwq.cat;

import cn.zwq.util.SpringUtils;
import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageTree;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CatServiceLogUtils {

	private CatServiceLogUtils() {
		super();
	}

	private static Logger logger = LoggerFactory.getLogger(CatServiceLogUtils.class);

	private static final MessageIdFactory CAT_MESSAGEID_FACTORY = new MessageIdFactory();

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
	public static final String DOMAIN_UUID = UUID.randomUUID().toString().replace("-", "");

	public static String initTraceId(String traceId) {
		String messageId = gainMessageId();
		if (StringUtils.isEmpty(traceId)) {
			traceId = "TraceId";
		}
		if (MDC.get(traceId) == null) {
			MDC.put(traceId, messageId);
			return messageId;
		}
		return "";
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

	public static void clearTraceId(String traceId) {
		if (org.apache.commons.lang.StringUtils.isNotBlank(traceId)) {
			MDC.remove(traceId);
		}
	}

	public static String getDomain() {
		CatProperties catProperties = SpringUtils.getBean(CatProperties.class);
		if (catProperties != null && StringUtils.isNotEmpty(catProperties.getDomain())) {
			return catProperties.getClientSystem();
		}
		return "zwq";
	}

	public static void logRemoteCallServer(HttpHeaders httpHeaders) {
		List<String> roots = Optional.ofNullable(httpHeaders.get(KEY_ROOT)).orElseGet(() -> new ArrayList<>());
		if (roots.isEmpty()) {
			Cat.logRemoteCallClient(new CatContext(), getDomain());
		} else {
			List<String> childes = Optional.ofNullable(httpHeaders.get(KEY_CHILD)).orElseGet(() -> new ArrayList<>());
			List<String> parents = Optional.ofNullable(httpHeaders.get(KEY_PARENT)).orElseGet(() -> new ArrayList<>());
			Optional<String> child = childes.stream().findAny();
			Optional<String> parent = parents.stream().findAny();

			CatContext catContext = new CatContext();

			catContext.addProperty(Context.ROOT, roots.get(0));
			catContext.addProperty(Context.CHILD, child.orElse(""));
			catContext.addProperty(Context.PARENT, parent.orElse(""));

			Cat.logRemoteCallServer(catContext);
		}
	}

	public static String getPathName(String path, CatProperties catProperties) {
		if (catProperties == null || catProperties.getAllInOnePaths() == null || catProperties.getAllInOnePaths().isEmpty()) {
			return path;
		}
		Optional<String> matchAllInOneUrl = catProperties.getAllInOnePaths().stream().filter(value -> path.startsWith(value)).findAny();
		if (matchAllInOneUrl.isPresent()) {
			return matchAllInOneUrl.get();
		}
		if (path.startsWith("/home/") && path.length() == 42) {
			// "/home/bfdfc19c-b43b-4685-92ce-a4c7141c3c75"
			return "/home";
		}
		return path;
	}
}
