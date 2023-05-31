package cn.zwq.interceptor;

import java.net.URL;
import java.util.Map;

import cn.zwq.cat.CatNetTransaction;
import cn.zwq.cat.context.CatContext;
import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component("catNetDefaultTransaction")
@Primary
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class CatNetDefaultTransaction implements CatNetTransaction {
	private static Logger logger = LoggerFactory.getLogger(CatNetDefaultTransaction.class);

	private Transaction transaction;

	@Override
	public void init(URL url, Map<String, String> headerParams) {
		String callType = CatUtils.callType(url.getPath());
		String name = CatUtils.callName(url, callType);

		transaction = Cat.newTransaction(callType, name);
		transaction.setSuccessStatus();

		logger.info("url:{}", url.toString());

		CatContext context = new CatContext();

		Cat.logRemoteCallClient(context, CatServiceLogUtils.getDomain());
		String catId = context.getProperty(Cat.Context.CHILD);
		String parentId = context.getProperty(Cat.Context.PARENT);
		String catRootId = context.getProperty(Cat.Context.ROOT);
		headerParams.put(CatServiceLogUtils.KEY_CHILD, catId);
		headerParams.put(CatServiceLogUtils.KEY_PARENT, parentId);
		headerParams.put(CatServiceLogUtils.KEY_ROOT, catRootId);
		// headerParams.put(CatServiceLogUtils.KEY_TRACE_MODE, "true");
		headerParams.put(CatServiceLogUtils.KEY_CLIENT_SYSTEM, CatServiceLogUtils.getClientSystem());
	}

	@Override
	public void setStatus(String status) {
		if (transaction != null) {
			transaction.setStatus(status);
		}
	}

	@Override
	public void setStatus(Throwable e) {
		if (transaction != null) {
			transaction.setStatus(e);
		}
	}

	@Override
	public void logEvent(String type, String name) {
		if (transaction != null) {
			Cat.logEvent(type, name);
		}
	}

	@Override
	public void logEvent(String type, String name, String status, String nameValuePairs) {
		if (transaction != null) {
			Cat.logEvent(type, name, status, nameValuePairs);
		}
	}

	@Override
	public void complete() {
		if (transaction != null) {
			transaction.complete();
		}
	}
}