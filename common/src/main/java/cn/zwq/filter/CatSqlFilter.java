package cn.zwq.filter;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.zwq.cat.CatProperties;
import cn.zwq.cat.CatSqlTransaction;
import cn.zwq.cat.context.CatSingleConfig;
import cn.zwq.cat.util.CatUtils;
import cn.zwq.util.SpringBeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.alibaba.druid.DbType;
import com.alibaba.druid.filter.AutoLoad;
import com.alibaba.druid.filter.FilterAdapter;
import com.alibaba.druid.filter.FilterChain;
import com.alibaba.druid.proxy.jdbc.DataSourceProxy;
import com.alibaba.druid.proxy.jdbc.PreparedStatementProxy;
import com.alibaba.druid.proxy.jdbc.ResultSetProxy;
import com.alibaba.druid.proxy.jdbc.StatementProxy;
import com.alibaba.druid.sql.visitor.ParameterizedOutputVisitorUtils;

/**
 * cat监控drui连接池
 *
 * @author flowkr90@gmail.com
 */
@AutoLoad
@ConditionalOnProperty(value = "cat.config.enabled", havingValue = "true")
public class CatSqlFilter extends FilterAdapter {
	private static Logger logger = LoggerFactory.getLogger(CatSqlFilter.class);

	private String dbType;

	private String url;

	private String successStatus = "0";

	private CatProperties catProperties;

	@Autowired
	public void setCatProperties(CatProperties catProperties) {
		this.catProperties = catProperties;
	}

	@Override
	public void init(DataSourceProxy dataSource) {
		dbType = dataSource.getDbType();
		url = dataSource.getUrl();
		if (StringUtils.isEmpty(dbType)) {
			if (String.format("%s", url).contains("mysql")) {
				dbType = "mysql";
			} else {
				dbType = "sqlserver";
			}
		}
		if (catProperties == null) {
			catProperties = SpringBeanUtils.getBean(CatProperties.class);
		}
	}

	private void initCatTransaction(CatSqlTransaction transaction, String url) {
		if (!CatSingleConfig.isRunning()) {
			return;
		}
		transaction.init(CatUtils.TYPE_SQL, url);
	}

	private void logCatEvent(CatSqlTransaction transaction, String type, String name) {
		if (!CatSingleConfig.isRunning()) {
			return;
		}
		if (transaction != null) {
			transaction.logEvent(type, name);
		}
	}

	private void logCatEvent(CatSqlTransaction transaction, String type, String name, String status, String nameValuePairs) {
		if (!CatSingleConfig.isRunning()) {
			return;
		}
		if (transaction != null) {
			transaction.logEvent(type, name, status, nameValuePairs);
		}
	}

	private void complete(CatSqlTransaction transaction) {
		if (!CatSingleConfig.isRunning()) {
			return;
		}
		if (transaction != null) {
			transaction.complete();
		}
	}

	private CatSqlTransaction preExecute(String sql, StatementProxy statement) {
		if (!CatSingleConfig.isRunning()) {
			logger.info("sql:{}", sql);
			return null;
		}
		if (!SpringBeanUtils.hasBean(CatSqlTransaction.class)) {
			logger.info("sql:{}", sql);
			return null;
		}
		CatSqlTransaction transaction = SpringBeanUtils.getBean(CatSqlTransaction.class);
		String paraSql = parameterizSql(sql, dbType);
		initCatTransaction(transaction, paraSql);
		logCatEvent(transaction, "SQL.DbType", dbType);
		logCatEvent(transaction, "SQL.Database", url);
		// logCatEvent(transaction, "SQL.sql", paraSql, successStatus, sql);
		StringBuilder params = new StringBuilder(sql);
		if (catProperties.isSqlParams() && statement != null && statement.getParametersSize() > 0) {
			params.append(" 参数：");
			for (int i = 0; i < statement.getParametersSize(); i++) {
				params.append(statement.getParameter(i).getValue());
				params.append(",");
			}
		}

		logger.info("{}", params);
		return transaction;
	}

	private void catchExecute(CatSqlTransaction transaction, SQLException e) {
		if (!CatSingleConfig.isRunning()) {
			return;
		}
		if (transaction != null) {
			transaction.logError(e);
			transaction.setStatus(e);
		}
	}

	@Override
	public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		boolean result;
		try {
			result = super.statement_execute(chain, statement, sql);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}

		return result;
	}

	@Override
	public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		boolean result;
		try {
			result = super.statement_execute(chain, statement, sql, autoGeneratedKeys);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}

		return result;
	}

	@Override
	public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, int[] columnIndexes)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		boolean result;
		try {
			result = super.statement_execute(chain, statement, sql, columnIndexes);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}

		return result;
	}

	@Override
	public boolean statement_execute(FilterChain chain, StatementProxy statement, String sql, String[] columnNames)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		boolean result;
		try {
			result = super.statement_execute(chain, statement, sql, columnNames);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}

		return result;
	}

	@Override
	public int[] statement_executeBatch(FilterChain chain, StatementProxy statement) throws SQLException {
		CatSqlTransaction transaction = preExecute(statement.getBatchSql(), statement);
		int[] result;
		try {
			result = super.statement_executeBatch(chain, statement);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}

		return result;
	}

	@Override
	public ResultSetProxy statement_executeQuery(FilterChain chain, StatementProxy statement, String sql)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		ResultSetProxy result;
		try {
			result = super.statement_executeQuery(chain, statement, sql);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}

		return result;
	}

	@Override
	public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql) throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		int result;
		try {
			result = super.statement_executeUpdate(chain, statement, sql);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}
		return result;
	}

	@Override
	public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int autoGeneratedKeys)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		int result;
		try {
			result = super.statement_executeUpdate(chain, statement, sql, autoGeneratedKeys);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}
		return result;
	}

	@Override
	public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, int[] columnIndexes)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		int result;
		try {
			result = super.statement_executeUpdate(chain, statement, sql, columnIndexes);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}
		return result;
	}

	@Override
	public int statement_executeUpdate(FilterChain chain, StatementProxy statement, String sql, String[] columnNames)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(sql, statement);
		int result;
		try {
			result = super.statement_executeUpdate(chain, statement, sql, columnNames);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}
		return result;
	}

	@Override
	public boolean preparedStatement_execute(FilterChain chain, PreparedStatementProxy statement) throws SQLException {
		CatSqlTransaction transaction = preExecute(statement.getSql(), statement);
		boolean result;
		try {
			result = super.preparedStatement_execute(chain, statement);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}
		return result;
	}

	@Override
	public ResultSetProxy preparedStatement_executeQuery(FilterChain chain, PreparedStatementProxy statement)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(statement.getSql(), statement);
		ResultSetProxy result;
		try {
			result = super.preparedStatement_executeQuery(chain, statement);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}
		return result;
	}

	@Override
	public int preparedStatement_executeUpdate(FilterChain chain, PreparedStatementProxy statement)
			throws SQLException {
		CatSqlTransaction transaction = preExecute(statement.getSql(), statement);
		int result;
		try {
			result = super.preparedStatement_executeUpdate(chain, statement);
		} catch (SQLException e) {
			catchExecute(transaction, e);
			throw e;
		} finally {
			complete(transaction);
		}
		return result;
	}

	private String parameterizSql(String sql, String dbType) {
		try {
			sql = ParameterizedOutputVisitorUtils.parameterize(sql, DbType.valueOf(dbType));
		} catch (Exception e) {
		}
		sql = mergeQuestionMarks(sql);
		sql = removeOrReplaceAs(sql);
		sql = truncateSql(sql);
		return chinese2xo(sql);
	}

	private static String mergeQuestionMarks(String sql) { // 归并 in (?, ?, ?) 成 in (?)
		Pattern pattern1 = Pattern.compile(" (in *[(]([?],.?)+[?][)])");
		Matcher matcher1 = pattern1.matcher(sql);
		while (matcher1.find()) {
			sql = sql.replace(matcher1.group(0), " in (?)");
			matcher1 = pattern1.matcher(sql);
		}

		Pattern pattern2 = Pattern.compile(" (IN *[(]([?],.?)+[?][)])");
		Matcher matcher2 = pattern2.matcher(sql);
		while (matcher1.find()) {
			sql = sql.replace(matcher2.group(0), " IN (?)");
			matcher2 = pattern2.matcher(sql);
		}
		return sql;
	}

	private static String truncateSql(String sql) {
		if (sql == null) {
			return "";
		}

		if (sql.length() <= 155) {
			return sql;
		}
		return sql.substring(0, 100) + "..." + sql.substring(sql.length() - 50);
	}

	private static char[] XO = { 'x', 'X', 'o', 'O' };

	private static String chinese2xo(String sql) {
		boolean changed = false;
		char[] chars = sql.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (c >= 0x4E00 && c <= 0x9FA5) { // 常用汉字
				chars[i] = XO[c % XO.length];
				changed = true;
			} else if (c < 32 || c > 126) { // 特殊字符
				chars[i] = '.';
				changed = true;
			}
		}
		return changed ? new String(chars) : sql;
	}

	/**
	 * @Description: 替换AS后面的字段别名为XXX
	 * @Param
	 * @return string
	 **/
	private static String removeOrReplaceAs(String sql) {
		sql = sql.replaceAll(" +", " ");
		sql = sql.replace("\r\n", " ");
		sql = sql.replace("\n", " ");
		sql = sql.replace("\t", " ");
		String regex1 = " AS (.*?)(,| FROM )";
		Pattern pattern1 = Pattern.compile(regex1);
		Matcher m1 = pattern1.matcher(sql);
		while (m1.find()) {
			String source1 = m1.group(0);
			if (source1.contains(",")) {
				sql = sql.replace(source1, " AS XXX,");
			}
			if (source1.contains(" FROM ")) {
				sql = sql.replace(source1, " AS XXX FROM ");
			}
		}

		String regex2 = " as (.*?)(,| from )";
		Pattern pattern2 = Pattern.compile(regex2);
		Matcher m2 = pattern2.matcher(sql);
		while (m2.find()) {
			String source2 = m2.group(0);
			if (source2.contains(",")) {
				sql = sql.replace(source2, " as XXX,");
			}
			if (source2.contains(" from ")) {
				sql = sql.replace(source2, " as XXX from ");
			}
		}
		return sql;
	}
}