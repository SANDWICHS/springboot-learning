package cn.zwq.util;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhangwenqia
 * @create 2023-03-21 10:16
 * @description 类描述
 */
public class ClickHouseUtil {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(ClickHouseUtil.class);

	/**
	 * 查询数据
	 *
	 * @param clickHouseDataSource
	 * @param sql                  select * from user_info
	 * @param params               sql参数
	 */
	public static List<JSONObject> querySql(DataSource clickHouseDataSource, String sql, Object... params) {
		try (Connection connection = clickHouseDataSource.getConnection()) {
			return querySql(connection, sql, params);
		} catch (SQLException e) {
			logger.error("数据查询异常", e);
		}
		return null;
	}

	public static List<JSONObject> querySql(Connection connection, String sql, Object... params) {
		logger.info("clickHouse 执行sql：" + sql);
		try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
			for (int i = 1; i <= params.length; i++) {
				pstmt.setObject(i, params[i - 1]);
			}
			try (ResultSet results = pstmt.executeQuery()) {
				ResultSetMetaData rsmd = results.getMetaData();
				List<JSONObject> list = new ArrayList();
				while (results.next()) {
					JSONObject row = new JSONObject();
					for (int i = 1; i <= rsmd.getColumnCount(); i++) {
						row.put(rsmd.getColumnName(i), results.getString(rsmd.getColumnName(i)));
					}
					list.add(row);
				}
				return list;
			}
		} catch (SQLException e) {
			logger.error("数据查询异常", e);
		}
		return null;
	}

	/**
	 * 添加数据
	 *
	 * @param clickHouseDataSource
	 * @param sql                  insert into t_order01 values('103', 'sku_004',
	 *                             '2500.00','2021-06-01 12:00:00'); alter table
	 *                             t_order01 delete where sku_id = * 'sku_002';alter
	 *                             table t_order01 update total_amount = 1 * where
	 *                             sku_id = 'sku_002';
	 * @param params               sql参数
	 */
	public static int[] executeBatch(DataSource clickHouseDataSource, String sql, List<Object[]> params) {
		logger.info("clickHouse 执行sql：" + sql);
		try (Connection connection = clickHouseDataSource.getConnection();
				PreparedStatement pstmt = connection.prepareStatement(sql)) {
			params.forEach(value -> {
				try {
					for (int i = 1; i <= value.length; i++) {
						pstmt.setObject(i, value[i - 1]);
					}
					pstmt.addBatch();
				} catch (SQLException e) {
					logger.error("设置参数值异常", e);
				}
			});
			return pstmt.executeBatch();
		} catch (SQLException e) {
			logger.error("执行脚本异常", e);
		}
		return new int[0];
	}

	/**
	 * 删除数据
	 *
	 * @param clickHouseDataSource
	 * @param sql                  alter table t_order01 delete where sku_id =
	 *                             'sku_002';
	 * @param params               sql参数
	 */
	public static int[] deleteSql(DataSource clickHouseDataSource, String sql, List<Object[]> params) {
		logger.info("clickHouse 执行sql：" + sql);
		try (Connection connection = clickHouseDataSource.getConnection();
				PreparedStatement pstmt = connection.prepareStatement(sql)) {
			params.forEach(value -> {
				try {
					for (int i = 1; i <= value.length; i++) {
						pstmt.setObject(i, value[i - 1]);
					}
					pstmt.addBatch();
				} catch (SQLException e) {
					logger.error("数据删除设置值异常", e);
				}
			});
			return pstmt.executeBatch();
		} catch (SQLException e) {
			logger.error("数据删除异常", e);
		}
		return new int[0];
	}

	/**
	 * 更新数据
	 *
	 * @param clickHouseDataSource
	 * @param sql                  alter table t_order01 update total_amount = 1
	 *                             where sku_id = 'sku_002';
	 * @param params               sql参数
	 */
	public static boolean updateSql(DataSource clickHouseDataSource, String sql, Object... params) {
		logger.info("clickHouse 执行sql：" + sql);
		try (Connection connection = clickHouseDataSource.getConnection();
				PreparedStatement pstmt = connection.prepareStatement(sql)) {
			for (int i = 1; i <= params.length; i++) {
				pstmt.setObject(i, params[i - 1]);
			}
			return pstmt.execute();
		} catch (SQLException e) {
			logger.error("数据更新异常", e);
		}
		return false;
	}

	/**
	 * 建表
	 *
	 * @param clickHouseDataSource
	 * @param sql                  create table t_order02( id UInt32, sku_id String,
	 *                             total_amount Decimal(16,2), create_time Datetime
	 *                             ) engine =MergeTree partition by
	 *                             toYYYYMMDD(create_time) primary key (id) order by
	 *                             (id,sku_id);
	 */
	public static boolean createTable(DataSource clickHouseDataSource, String sql) {
		logger.info("clickHouse 执行sql：" + sql);
		try (Connection connection = clickHouseDataSource.getConnection();
				Statement pstmt = connection.createStatement()) {
			return pstmt.execute(sql);
		} catch (SQLException e) {
			logger.error("建表异常", e);
		}
		return false;
	}

	/**
	 * 删除表
	 *
	 * @param clickHouseDataSource
	 * @param sql                  drop table t_order01;
	 */
	public static boolean dropTable(DataSource clickHouseDataSource, String sql) {
		logger.info("clickHouse 执行sql：" + sql);
		try (Connection connection = clickHouseDataSource.getConnection();
				Statement pstmt = connection.createStatement()) {
			return pstmt.execute(sql);
		} catch (SQLException e) {
			logger.error("删除表异常", e);
		}
		return false;
	}
}
