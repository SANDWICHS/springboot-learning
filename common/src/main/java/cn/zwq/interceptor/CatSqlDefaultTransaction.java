package cn.zwq.interceptor;

import java.sql.SQLException;

import cn.zwq.cat.CatSqlTransaction;
import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import cn.zwq.util.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;

/**
 * cat监控drui连接池
 *
 * @author flowkr90@gmail.com
 */
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component("catSqlDefaultTransaction")
@Primary
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class CatSqlDefaultTransaction implements CatSqlTransaction {

	private Transaction transaction;
	private static final String KEY = "catSqlDefaultTransactionTraceId";

	@Override
	public void init(String type, String url) {
		transaction = CatUtils.newTransaction(type, url);
		String traceId = CatServiceLogUtils.initTraceId();
		if (StringUtils.isNotEmpty(traceId)) {
			ThreadLocalUtil.put(KEY, traceId);
		}
	}

	@Override
	public void logEvent(String type, String name) {
		Cat.logEvent(type, name);
	}

	@Override
	public void logEvent(String type, String name, String status, String nameValuePairs) {
		Cat.logEvent(type, name, status, nameValuePairs);
	}

	@Override
	public void setStatus(Throwable e) {
		CatUtils.setStatus(transaction, e);
	}

	@Override
	public void logError(SQLException e) {
		Cat.logError(e);
	}

	@Override
	public void complete() {
		CatUtils.closeTransaction(transaction);
		String traceId = (String) ThreadLocalUtil.get(KEY);
		if (StringUtils.isNotEmpty(traceId)) {
			ThreadLocalUtil.remove(KEY);
			ThreadLocalUtil.checkClear();
			CatServiceLogUtils.clearTraceId(traceId);
		}
	}
}