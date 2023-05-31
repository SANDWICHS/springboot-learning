package cn.zwq.cat.util;

import java.net.URL;
import java.util.*;

import cn.zwq.cat.CatProperties;
import cn.zwq.cat.context.CatContext;
import cn.zwq.cat.context.CatSingleConfig;
import com.dianping.cat.message.MessageProducer;
import org.apache.commons.lang3.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.Cat.Context;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class CatUtils {

	private CatUtils() {

	}

	public static final String TYPE_TASK_SERVICE = "TaskService";
	public static final String TYPE_FILE_SERVICE = "FileService";
	public static final String TYPE_AUTH_SERVICE = "AuthService";
	public static final String TYPE_SYS_SERVICE = "SysService";
	public static final String TYPE_SERVICE = "Service";
	public static final String TYPE_PIGEON_SERVICE = "PigeonService";
	public static final String TYPE_URL = "URL";
	public static final String TYPE_CALL = "Call";
	public static final String TYPE_PIGEON_CALL = "PigeonCall";
	public static final String TYPE_SQL = "SQL";
	public static final String TYPE_CACHE = "Cache.";
	public static final String KEY_TRANSACTION = "CatTransaction";
	public static final String KEY_REST_TRANSACTION = "Rest-CatTransaction";
	public static final String KEY_REST_START_TIME = "Rest-uri-start-time";
	public static final String KEY_REST_URI = "Rest-uri";

	public static final String TYPE_GETCARDS = "getCards";
	public static final String TYPE_GETCARDS_DB = "getCards_db";
	public static final String TYPE_GETCARD = "getCard";
	public static final String TYPE_EVENT_EXECUTE_SQL = "execute_sql";
	public static final String TYPE_QUERYDATA = "queryData";

	/**
	 * 初始化当前MessageTree的context
	 *
	 * @return 返回childId，这个ID传给需要把消息加入到当前消息子节点的
	 */
	public static String initClientContext() {
		CatContext context = new CatContext();
		Cat.logRemoteCallClient(context, CatServiceLogUtils.getClientSystem());
		return context.getProperty(Context.CHILD);
	}

	/**
	 * 根据childId构建context，初始化MessageTree
	 *
	 * @param childId
	 */
	public static void loadContextByChildId(String childId) {
		CatContext context = new CatContext();
		context.addProperty(Context.CHILD, childId);
		Cat.logRemoteCallServer(context);
	}

	/**
	 * 创建cat Transaction 创建完成之后必须调用closeTransaction关闭
	 *
	 * @param type
	 * @param name
	 * @param clientMessageTreeId
	 * @return
	 */
	public static Transaction newTransaction(String type, String name, String clientMessageTreeId) {
		Transaction t = Cat.newTransaction(type, name);
		if (clientMessageTreeId != null) {
			CatUtils.loadContextByChildId(clientMessageTreeId);
		}
		// t.setStatus(Message.SUCCESS);
		return t;
	}

	public static Transaction newTransaction(String type, String name) {
		Transaction t = newTransaction(type, name, null);
		// t.setStatus(Message.SUCCESS);
		return t;
	}

	public static Transaction newPigeonCallTransaction(String catName) {
		// 系统内部分布式接口调用方打点为：PigeonCall
		return newTransaction(TYPE_PIGEON_CALL, catName);
	}

	public static Transaction getPigeonCallTransaction(CatProperties catProperties, String catName) {
		Transaction t = null;
		if (StringUtils.isNotEmpty(catName) && !hasPigeonCallTransaction()) {
			Map<String, List<String>> customizeTypes = Optional.ofNullable(catProperties).map(value -> value.getCustomizeTypes())
					.orElse(new HashMap<>());
			String catType = customizeTypes.entrySet().stream().filter(item -> item.getValue().stream().anyMatch(value -> catName.contains(value)))
					.map(Map.Entry::getKey).findFirst().orElse(TYPE_PIGEON_CALL);
			if (TYPE_PIGEON_SERVICE.equals(catType)) {
				final MessageProducer producer = Cat.getProducer();
				t = producer.newTransaction(catType, catName);
			} else {
				t = newTransaction(catType, catName);
			}
		}
		return t;
	}

	public static void setStatus(Transaction t) {
		if (t == null) {
			return;
		}
		t.setStatus(Message.SUCCESS);
	}

	/**
	 * 关闭创建cat Transaction
	 */
	public static void setStatus(Transaction t, Throwable e) {
		if (t == null) {
			return;
		}
		t.setStatus(e);
	}

	/**
	 * 关闭创建cat Transaction
	 */
	public static void closeTransaction(Transaction t) {
		if (t == null) {
			return;
		}
		t.complete();
	}

	/**
	 * 关闭出现异常的Transaction
	 */
	public static void closeTransaction(Transaction t, Throwable e) {
		if (t == null) {
			return;
		}
		t.setStatus(e);
		t.complete();
	}

	/**
	 * 用于通过反射记录cat日志
	 *
	 * @param transactionType
	 * @param transactionName
	 * @param eventType
	 * @param eventContent
	 * @param error
	 * @param errorEventType
	 * @param errorEventContent
	 */
	public static void logInvoke(String transactionType, String transactionName, String eventType,
			String eventContent,
			Throwable error, String errorEventType, String errorEventContent) {

		CatSingleConfig catConfig = CatSingleConfig.getConfig();

		boolean enable = catConfig.enable();

		if (enable) {

			Transaction t = CatUtils.newTransaction(transactionType, transactionName);
			Cat.logEvent(eventType, transactionName, Message.SUCCESS, eventContent);

			if (error == null) {
				CatUtils.closeTransaction(t);
			} else {
				Cat.logError(error);
				Cat.logEvent(errorEventType, transactionName, error.getClass().getSimpleName(),
						errorEventContent);
				t.setStatus(error);
				CatUtils.closeTransaction(t);
			}
		}

	}

	public static boolean hasPigeonCallTransaction() {
		try {
			Transaction t = Cat.getManager().getPeekTransaction();
			return t != null && (TYPE_CALL.equals(t.getType()) || TYPE_PIGEON_CALL.equals(t.getType()));
		} catch (Exception e) {
			// ignore
			return false;
		}
	}

	public static String callType(String url) {
		if (url.contains("zwq") || url.contains(CatServiceLogUtils.getClientSystem())) {
			return TYPE_PIGEON_CALL;
		}
		return TYPE_CALL;
	}

	public static String callName(URL url, String callType) {
		String pathName = getPathName(url.getPath(), CatServiceLogUtils.getAllInOnePaths());
		if (CatUtils.TYPE_CALL.equals(callType)) {
			int index = url.toString().indexOf(url.getPath());
			return String.format("%s%s", url.toString().substring(0, index), pathName);
		}
		return pathName;
	}

	public static String getPathName(String path, List<String> allInOnePaths) {
		if (allInOnePaths == null || allInOnePaths.isEmpty()) {
			return path;
		}
		Optional<String> matchAllInOneUrl = allInOnePaths.stream().filter(value -> path.startsWith(value)).findAny();
		if (matchAllInOneUrl.isPresent()) {
			return matchAllInOneUrl.get();
		}
		if (path.startsWith("/home/") && path.length() == 42) {
			return "/home";
		}
		return path;
	}

	public static String getTransactionType(CatProperties catProperties, String uri, String clientSystem) {
		if (catProperties == null) {
			return TYPE_URL;
		}

		List<String> services = Optional.ofNullable(catProperties.getServices()).orElse(new ArrayList<>());
		Optional<String> serviceValue = services.stream().filter(value -> uri.contains(value)).findAny();
		if (serviceValue.isPresent()) {
			return TYPE_SERVICE;
		}

		// 网页和APP不会传clientSystem这个http-header
		if (clientSystem == null || clientSystem.isEmpty() || StringUtils.isEmpty(catProperties.getClientSystem())) {
			return TYPE_URL;
		}

		if (clientSystem.equalsIgnoreCase(catProperties.getClientSystem())) {
			return TYPE_PIGEON_SERVICE;
		} else {
			return TYPE_SERVICE;
		}
	}

	public static void setCatHeader(Map<String, String> headerParams) {
		CatContext context = getCatContext();
		headerParams.put(CatServiceLogUtils.KEY_CHILD, context.getProperty(Context.CHILD));
		headerParams.put(CatServiceLogUtils.KEY_PARENT, context.getProperty(Context.PARENT));
		headerParams.put(CatServiceLogUtils.KEY_ROOT, context.getProperty(Context.ROOT));
	}

	public static void setCatHeaderObj(Map<String, Object> headerParams) {
		CatContext context = getCatContext();
		headerParams.put(CatServiceLogUtils.KEY_CHILD, context.getProperty(Context.CHILD));
		headerParams.put(CatServiceLogUtils.KEY_PARENT, context.getProperty(Context.PARENT));
		headerParams.put(CatServiceLogUtils.KEY_ROOT, context.getProperty(Context.ROOT));
		// headerParams.put(CatServiceLogUtils.KEY_TRACE_MODE, "true");
		headerParams.put(CatServiceLogUtils.KEY_CLIENT_SYSTEM, CatServiceLogUtils.getClientSystem());
	}

	public static CatContext getCatContext() {
		CatContext context = new CatContext();
		Cat.logRemoteCallClient(context, CatServiceLogUtils.getClientSystem()); // 声明子节点
		return context;
	}
}
