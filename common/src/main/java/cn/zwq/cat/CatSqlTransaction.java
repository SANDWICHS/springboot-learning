package cn.zwq.cat;

import java.sql.SQLException;

public interface CatSqlTransaction {
	void init(String type, String url);

	void logEvent(String s, String dbType);

	void logEvent(String type, String name, String status, String nameValuePairs);

	void setStatus(Throwable e);

	void logError(SQLException e);

	void complete();
}
