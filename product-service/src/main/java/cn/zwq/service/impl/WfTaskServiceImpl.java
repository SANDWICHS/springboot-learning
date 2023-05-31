package cn.zwq.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.zwq.dto.WfTaskCarryDto;
import cn.zwq.dto.WfTaskDto;
import cn.zwq.dto.WfTaskProcessUserDto;
import cn.zwq.service.WfTaskService;
import cn.zwq.util.ClickHouseUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * @author zhangwenqia
 * @create 2023-03-21 9:15
 * @description 类描述
 */
@Service
public class WfTaskServiceImpl implements WfTaskService {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(WfTaskServiceImpl.class);

	@Resource(name = "mysqlJdbcTemplate")
	JdbcTemplate mysqlJdbcTemplate;

	@Resource(name = "clickHouseDataSource")
	DataSource clickHouseDataSource;

	@Override
	public void insertWfTaskDb(Integer versionId, List<WfTaskDto> tasks) {
		updateWfTasks(versionId, tasks, false);
	}

	@Override
	public void insertWfTaskCarryDb(Integer versionId, List<WfTaskCarryDto> taskCarries) {
		updateWfTaskCarries(versionId, taskCarries, false);
	}

	@Override
	public void insertWfTaskProcessUserDb(Integer versionId, List<WfTaskProcessUserDto> taskProcessUsers) {
		updateWfTaskProcessUsers(versionId, taskProcessUsers, false);
	}

	@Override
	public void deleteWfTaskDb(Integer versionId, List<WfTaskDto> tasks) {
		updateWfTasks(versionId, tasks, true);
	}

	@Override
	public void deleteWfTaskCarryDb(Integer versionId, List<WfTaskCarryDto> taskCarries) {
		updateWfTaskCarries(versionId, taskCarries, true);
	}

	@Override
	public void deleteWfTaskProcessUserDb(Integer versionId, List<WfTaskProcessUserDto> taskProcessUsers) {
		updateWfTaskProcessUsers(versionId, taskProcessUsers, true);
	}

	private void updateWfTasks(Integer versionId, List<WfTaskDto> tasks, boolean deleted) {
		if (tasks.isEmpty()) {
			return;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("insert into mtnoh_aaa_platform.wf_task_%d ");
		sql.append("(id, name, description, current_status, ");
		sql.append("create_user, create_time, last_user, last_time, push_user, push_time, ");
		sql.append("current_time_limit, current_time_limit2, total_time_limit, total_time_limit2, ");
		for (int i = 1; i <= 20; i++) {
			sql.append(String.format("condition_field_%d, ", i));
		}
		sql.append("task_type, version, deleted)");
		sql.append("values(?,?,?,?");
		sql.append(",?,toDateTime(?),?,toDateTime(?),?,toDateTime(?)");
		sql.append(",toDateTime(?),toDateTime(?),toDateTime(?),toDateTime(?)");
		sql.append(",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?");
		sql.append(String.format(",?,toDateTime(?),%b)", deleted));

		List<Object[]> params = new ArrayList<>(tasks.size());
		tasks.stream().forEach(value -> {
			Object[] item = new Object[36];
			int i = 0;
			item[i++] = value.getId();
			item[i++] = getValue(value.getName());
			item[i++] = getValue(value.getDescription());
			item[i++] = value.getCurrentStatus();

			item[i++] = value.getCreateUser();
			item[i++] = value.getCreateTime();
			item[i++] = value.getLastUser();
			item[i++] = value.getLastTime();
			item[i++] = value.getPushUser();
			item[i++] = value.getPushTime();

			item[i++] = value.getCurrentTimeLimit();
			item[i++] = value.getCurrentTimeLimit2();
			item[i++] = value.getTotalTimeLimit();
			item[i++] = value.getTotalTimeLimit2();

			for (int j = 1; j <= 20; j++) {
				item[i++] = getValue(value.getConditionFieldMap().get(j));
			}

			item[i++] = value.getTaskType();
			item[i++] = DateTime.now().toString(DatePattern.NORM_DATETIME_PATTERN);

			params.add(item);
		});

		ClickHouseUtil.executeBatch(clickHouseDataSource, String.format(sql.toString(), versionId), params);
	}

	private Object getValue(Object value) {
		if (value == null) {
			return "";
		}
		return value;
	}

	private void updateWfTaskCarries(Integer versionId, List<WfTaskCarryDto> taskCarries, boolean deleted) {
		if (taskCarries.isEmpty()) {
			return;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("insert into mtnoh_aaa_platform.wf_task_carry_%d ");
		sql.append("(task_id, carry, current_status, push_user, push_time, current_time_limit, current_time_limit2, task_type, version, deleted)");
		sql.append(String.format("values(?,?,?,?,toDateTime(now()),toDateTime(?),toDateTime(?),?,toDateTime(now()),%b)", deleted));

		List<Object[]> params = new ArrayList<>(taskCarries.size());
		taskCarries.stream().forEach(value -> {
			Object[] item = new Object[7];
			int i = 0;
			item[i++] = value.getTaskId();
			item[i++] = value.getCarry();
			item[i++] = value.getCurrentStatus();
			item[i++] = value.getPushUser();
			item[i++] = value.getCurrentTimeLimit();
			item[i++] = value.getCurrentTimeLimit2();
			item[i++] = value.getTaskType();
			params.add(item);
		});

		ClickHouseUtil.executeBatch(clickHouseDataSource, String.format(sql.toString(), versionId), params);
	}

	private void updateWfTaskProcessUsers(Integer versionId, List<WfTaskProcessUserDto> taskProcessUsers, boolean deleted) {
		if (taskProcessUsers.isEmpty()) {
			return;
		}

		StringBuilder sql = new StringBuilder();
		sql.append("insert into mtnoh_aaa_platform.wf_task_process_user_%d ");
		sql.append("(task_id, current_status, process_user, time_outed, time_outing,version,deleted)");
		sql.append(String.format("values(?,?,?,toDateTime(?),toDateTime(?),toDateTime(now()),%b)", deleted));

		List<Object[]> params = new ArrayList<>(taskProcessUsers.size());
		taskProcessUsers.stream().forEach(value -> {
			Object[] item = new Object[5];
			int i = 0;
			item[i++] = value.getTaskId();
			item[i++] = value.getCurrentStatus();
			item[i++] = value.getProcessUser();
			item[i++] = value.getTimeOuted();
			item[i++] = value.getTimeOuting();
			params.add(item);
		});

		ClickHouseUtil.executeBatch(clickHouseDataSource, String.format(sql.toString(), versionId), params);
	}

	@Override
	public Pair<Integer, List<WfTaskDto>> queryWfTaskDb(Integer versionId, Integer currentStatus,
			Pair<Integer, Integer> pageIndexRowCount) {
		String sqlCount = String.format(
				"select count(id) num from mtnoh_aaa_platform.wf_task_%d final where deleted = false and current_status = ?;",
				versionId);
		StringBuilder conditions = new StringBuilder();
		for (int i = 1; i <= 20; i++) {
			conditions.append(String.format(",condition_field_%d", i));
		}
		String sqlValue = String
				.format("select id,name,description,create_user,create_time,last_user,last_time,push_user,push_time,current_time_limit,current_time_limit2,total_time_limit,total_time_limit2,%s,task_type from mtnoh_aaa_platform.wf_task_%d final "
						+
						"where deleted = false and current_status = ?" +
						"order by id limit ?,?;", conditions.substring(1), versionId);
		try (Connection connection = clickHouseDataSource.getConnection()) {
			final List<JSONObject> countValue = ClickHouseUtil.querySql(connection, sqlCount, currentStatus);
			if (countValue != null && !countValue.isEmpty()) {
				final Integer num = countValue.get(0).getInteger("num");
				final int pageIndex = pageIndexRowCount.getKey() - 1;
				final int pageRowCount = pageIndexRowCount.getValue();
				final List<JSONObject> valueItems = ClickHouseUtil.querySql(connection, sqlValue, currentStatus, pageIndex, pageRowCount);
				List<WfTaskDto> wfTaskList = new ArrayList<>(valueItems.size());
				valueItems.stream().forEach(value -> {
					WfTaskDto wfTaskDto = new WfTaskDto();
					wfTaskDto.setId(value.getInteger("task_id"));
					wfTaskDto.setName(value.getString("name"));
					wfTaskDto.setDescription(value.getString("description"));
					wfTaskDto.setCurrentStatus(currentStatus);
					wfTaskDto.setCreateUser(value.getString("create_user"));
					wfTaskDto.setCreateTime(value.getDate("create_time"));
					wfTaskDto.setLastUser(value.getString("push_user"));
					wfTaskDto.setLastTime(value.getDate("push_time"));
					wfTaskDto.setPushUser(value.getString("push_user"));
					wfTaskDto.setPushTime(value.getDate("push_time"));
					wfTaskDto.setCurrentTimeLimit(value.getDate("current_time_limit"));
					wfTaskDto.setCurrentTimeLimit2(value.getDate("current_time_limit2"));
					wfTaskDto.setTotalTimeLimit(value.getDate("total_time_limit"));
					wfTaskDto.setTotalTimeLimit2(value.getDate("total_time_limit2"));
					wfTaskDto.setTaskType(value.getInteger("task_type"));
					Map<Integer, String> conditionFieldMap = new HashMap<>(20);
					for (int i = 1; i <= 20; i++) {
						conditionFieldMap.put(i, value.getString(String.format("condition_field_%d", i)));
					}
					wfTaskDto.setConditionFieldMap(conditionFieldMap);
					wfTaskList.add(wfTaskDto);
				});
				return Pair.of(num, wfTaskList);
			}
		} catch (SQLException e) {
			logger.error("数据查询异常", e);
		}

		return Pair.of(0, new ArrayList<>(0));
	}

	@Override
	public Pair<Integer, List<WfTaskCarryDto>> queryWfTaskCarryDb(Integer versionId, Integer currentStatus,
			Pair<Integer, Integer> pageIndexRowCount) {
		String sqlCount = String.format(
				"select count(task_id) num from mtnoh_aaa_platform.wf_task_carry_%d final where deleted = false and current_status = ?;",
				versionId);
		String sqlValue = String
				.format("select task_id,carry,push_user,push_time,current_time_limit,current_time_limit2,task_type from mtnoh_aaa_platform.wf_task_carry_%d final "
						+
						"where deleted = false and current_status = ?" +
						"order by task_id limit ?,?;", versionId);
		try (Connection connection = clickHouseDataSource.getConnection()) {
			final List<JSONObject> countValue = ClickHouseUtil.querySql(connection, sqlCount, currentStatus);
			if (countValue != null && !countValue.isEmpty()) {
				final Integer num = countValue.get(0).getInteger("num");
				final int pageIndex = pageIndexRowCount.getKey() - 1;
				final int pageRowCount = pageIndexRowCount.getValue();
				final List<JSONObject> valueItems = ClickHouseUtil.querySql(connection, sqlValue, currentStatus, pageIndex, pageRowCount);
				List<WfTaskCarryDto> wfTaskCarryList = new ArrayList<>(valueItems.size());
				valueItems.stream().forEach(value -> {
					WfTaskCarryDto wfTaskCarryDto = new WfTaskCarryDto();
					wfTaskCarryDto.setTaskId(value.getInteger("task_id"));
					wfTaskCarryDto.setCarry(value.getInteger("carry"));
					wfTaskCarryDto.setCurrentStatus(currentStatus);
					wfTaskCarryDto.setPushUser(value.getString("push_user"));
					wfTaskCarryDto.setPushTime(value.getDate("push_time"));
					wfTaskCarryDto.setCurrentTimeLimit(value.getDate("current_time_limit"));
					wfTaskCarryDto.setCurrentTimeLimit2(value.getDate("current_time_limit2"));
					wfTaskCarryDto.setTaskType(value.getInteger("task_type"));
					wfTaskCarryList.add(wfTaskCarryDto);
				});
				return Pair.of(num, wfTaskCarryList);
			}
		} catch (SQLException e) {
			logger.error("数据查询异常", e);
		}

		return Pair.of(0, new ArrayList<>(0));
	}

	@Override
	public Pair<Integer, List<WfTaskProcessUserDto>> queryWfTaskProcessUserDb(Integer versionId, Integer userId, Integer currentStatus,
			Pair<Integer, Integer> pageIndexRowCount) {
		String sqlCount = String.format(
				"select count(task_id) num from mtnoh_aaa_platform.wf_task_process_user_%d final where deleted = false and process_user = ? and current_status = ?;",
				versionId);
		String sqlValue = String.format("select task_id,time_outed,time_outing from mtnoh_aaa_platform.wf_task_process_user_%d final " +
				"where deleted = false and process_user = ? and current_status = ?" +
				"order by task_id limit ?,?;", versionId);
		try (Connection connection = clickHouseDataSource.getConnection()) {
			final List<JSONObject> countValue = ClickHouseUtil.querySql(connection, sqlCount, userId, currentStatus);
			if (countValue != null && !countValue.isEmpty()) {
				final Integer num = countValue.get(0).getInteger("num");
				final int pageIndex = pageIndexRowCount.getKey() - 1;
				final int pageRowCount = pageIndexRowCount.getValue();
				final List<JSONObject> valueItems = ClickHouseUtil.querySql(connection, sqlValue, userId, currentStatus, pageIndex, pageRowCount);
				List<WfTaskProcessUserDto> wfTaskProcessUserList = new ArrayList<>(valueItems.size());
				valueItems.stream().forEach(value -> {
					WfTaskProcessUserDto wfTaskProcessUserDto = new WfTaskProcessUserDto();
					wfTaskProcessUserDto.setTaskId(value.getInteger("task_id"));
					wfTaskProcessUserDto.setCurrentStatus(currentStatus);
					wfTaskProcessUserDto.setProcessUser(userId);
					wfTaskProcessUserDto.setTimeOuted(value.getDate("time_outed"));
					wfTaskProcessUserDto.setTimeOuting(value.getDate("time_outing"));
					wfTaskProcessUserList.add(wfTaskProcessUserDto);
				});
				return Pair.of(num, wfTaskProcessUserList);
			}
		} catch (SQLException e) {
			logger.error("数据查询异常", e);
		}

		return Pair.of(0, new ArrayList<>(0));
	}

	@Override
	public void createWfTaskTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE if not exists mtnoh_aaa_platform.wf_task_%d_local on cluster clickhouse_cluster");
		sql.append("(");
		sql.append("`id` Int64,");
		sql.append("`name`  Nullable(String),");
		sql.append("`description`  Nullable(String),");
		sql.append("`current_status` Int32,");
		sql.append("`create_user` String,");
		sql.append("`create_time` DateTime,");
		sql.append("`last_user` String,");
		sql.append("`last_time` DateTime,");
		sql.append("`push_user` String,");
		sql.append("`push_time` DateTime,");
		sql.append("`current_time_limit`  Nullable(DateTime),");
		sql.append("`current_time_limit2`  Nullable(DateTime),");
		sql.append("`total_time_limit`  Nullable(DateTime),");
		sql.append("`total_time_limit2`  Nullable(DateTime),");
		for (int i = 1; i <= 20; i++) {
			sql.append(String.format("`condition_field_%d`  Nullable(String),", i));
		}
		sql.append("`task_type`  Nullable(Int32),");
		sql.append("`version`  DateTime,");
		sql.append("`deleted` Bool");
		sql.append(") Engine=ReplacingMergeTree(version) ");
		sql.append("PARTITION BY toYYYYMMDD(create_time) ");
		sql.append("ORDER BY (id);");
		ClickHouseUtil.createTable(clickHouseDataSource, String.format(sql.toString(), versionId));

		sql.setLength(0);
		sql.append("CREATE TABLE mtnoh_aaa_platform.wf_task_%d ON CLUSTER clickhouse_cluster AS mtnoh_aaa_platform.wf_task_%d_local ");
		sql.append("ENGINE = Distributed(clickhouse_cluster, mtnoh_aaa_platform, wf_task_%d_local, intHash64(id));");
		ClickHouseUtil.createTable(clickHouseDataSource, String.format(sql.toString(), versionId, versionId, versionId));
	}

	@Override
	public void createWfTaskCarryTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE if not exists mtnoh_aaa_platform.wf_task_carry_%d_local on cluster clickhouse_cluster");
		sql.append("(");
		sql.append("`task_id` Int64,");
		sql.append("`carry` Int32,");
		sql.append("`current_status` Int32,");
		sql.append("`push_user` String,");
		sql.append("`push_time` DateTime,");
		sql.append("`current_time_limit`  Nullable(DateTime),");
		sql.append("`current_time_limit2`  Nullable(DateTime),");
		sql.append("`task_type`  Nullable(Int32),");
		sql.append("`version`  DateTime,");
		sql.append("`deleted` Bool");
		sql.append(") Engine=ReplacingMergeTree(version) ");
		sql.append("PARTITION BY (task_id,carry) ");
		sql.append("ORDER BY (task_id,carry);");
		ClickHouseUtil.createTable(clickHouseDataSource, String.format(sql.toString(), versionId));

		sql.setLength(0);
		sql.append("CREATE TABLE mtnoh_aaa_platform.wf_task_carry_%d ON CLUSTER clickhouse_cluster AS mtnoh_aaa_platform.wf_task_carry_%d_local ");
		sql.append("ENGINE = Distributed(clickhouse_cluster, mtnoh_aaa_platform, wf_task_carry_%d_local, sipHash64(task_id,carry));");
		ClickHouseUtil.createTable(clickHouseDataSource, String.format(sql.toString(), versionId, versionId, versionId));
	}

	@Override
	public void createWfTaskProcessUserTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE if not exists mtnoh_aaa_platform.wf_task_process_user_%d_local on cluster clickhouse_cluster");
		sql.append("(");
		sql.append("`task_id` Int64,");
		sql.append("`current_status` Int32,");
		sql.append("`process_user` Int32,");
		sql.append("`time_outed` Nullable(DateTime),");
		sql.append("`time_outing`  Nullable(DateTime),");
		sql.append("`version`  DateTime,");
		sql.append("`deleted` Bool");
		sql.append(") Engine=ReplacingMergeTree(version) ");
		sql.append("PARTITION BY (process_user,current_status,task_id) ");
		sql.append("ORDER BY (process_user,current_status,task_id);");
		ClickHouseUtil.createTable(clickHouseDataSource, String.format(sql.toString(), versionId));

		sql.setLength(0);
		sql.append(
				"CREATE TABLE mtnoh_aaa_platform.wf_task_process_user_%d ON CLUSTER clickhouse_cluster AS mtnoh_aaa_platform.wf_task_process_user_%d_local ");
		sql.append(
				"ENGINE = Distributed(clickhouse_cluster, mtnoh_aaa_platform, wf_task_process_user_%d_local, sipHash64(process_user,current_status,task_id));");
		ClickHouseUtil.createTable(clickHouseDataSource, String.format(sql.toString(), versionId, versionId, versionId));
	}

	@Override
	public void optimizeWfTaskTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("OPTIMIZE table mtnoh_aaa_platform.wf_task_%d_local on cluster clickhouse_cluster;");

		ClickHouseUtil.updateSql(clickHouseDataSource, String.format(sql.toString(), versionId));
	}

	@Override
	public void optimizeWfTaskCarryTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("OPTIMIZE table mtnoh_aaa_platform.wf_task_carry_%d_local on cluster clickhouse_cluster;");

		ClickHouseUtil.updateSql(clickHouseDataSource, String.format(sql.toString(), versionId));
	}

	@Override
	public void optimizeWfTaskProcessUserTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("OPTIMIZE table mtnoh_aaa_platform.wf_task_process_user_%d_local on cluster clickhouse_cluster;");

		ClickHouseUtil.updateSql(clickHouseDataSource, String.format(sql.toString(), versionId));
	}

	@Override
	public void truncateWfTaskTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("truncate table if exists mtnoh_aaa_platform.wf_task_%d_local on cluster clickhouse_cluster;");

		ClickHouseUtil.updateSql(clickHouseDataSource, String.format(sql.toString(), versionId));
	}

	@Override
	public void truncateWfTaskCarryTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("truncate table if exists mtnoh_aaa_platform.wf_task_carry_%d_local on cluster clickhouse_cluster;");

		ClickHouseUtil.updateSql(clickHouseDataSource, String.format(sql.toString(), versionId));
	}

	@Override
	public void truncateWfTaskProcessUserTableDb(Integer versionId) {
		StringBuilder sql = new StringBuilder();
		sql.append("truncate table if exists mtnoh_aaa_platform.wf_task_process_user_%d_local on cluster clickhouse_cluster;");

		ClickHouseUtil.updateSql(clickHouseDataSource, String.format(sql.toString(), versionId));
	}

	@Override
	public List<Map<String, Object>> queryTableInfos(String type, Integer versionId, Integer maxId) {
		String sql;
		switch (type) {
		case "WfTaskTable":
			sql = String.format("SELECT id,name,description,current_status,create_user," +
					"create_time,last_user,last_time,push_user,push_time,current_time_limit,total_time_limit,current_time_limit2,total_time_limit2," +
					"condition_field_1,condition_field_2,condition_field_3,condition_field_4,condition_field_5,condition_field_6," +
					"condition_field_7,condition_field_8,condition_field_9," +
					"condition_field_10,condition_field_11,condition_field_12," +
					"condition_field_13,condition_field_14,condition_field_15,condition_field_16," +
					"condition_field_17,condition_field_18,condition_field_19,condition_field_20,task_type " +
					"FROM mtnoh_aaa_platform.wf_task_%d WHERE id > ? ORDER BY id LIMIT 1000", versionId);
			break;
		case "WfTaskCarryTable":
			sql = String.format("SELECT task_id,carry,current_status,push_user,push_time,current_time_limit,current_time_limit2,task_type " +
					"FROM mtnoh_aaa_platform.wf_task_carry_%d WHERE task_id > ? ORDER BY task_id LIMIT 1000", versionId);
			break;
		case "WfTaskProcessUserTable":
			sql = String.format("SELECT task_id,current_status,process_user,time_outed,time_outing " +
					"FROM mtnoh_aaa_platform.wf_task_process_user_%d WHERE task_id > ? ORDER BY task_id LIMIT 1000", versionId);
			break;
		default:
			sql = null;
			break;
		}

		if (StringUtils.isEmpty(sql)) {
			return new ArrayList<>(0);
		}
		return mysqlJdbcTemplate.queryForList(sql, maxId);
	}
}
