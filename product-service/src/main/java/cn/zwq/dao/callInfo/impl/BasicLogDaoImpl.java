package cn.zwq.dao.callInfo.impl;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import cn.hutool.core.lang.Pair;
import cn.zwq.dto.callInfo.BasicInfoBo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;

/**
 * @author zhangwenqia
 * @create 2022-03-08 19:23
 * @description 类描述
 */
public abstract class BasicLogDaoImpl {
	JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("mysqlJdbcTemplate")
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public long addLog(String table, String mqKey, String taskSn, String type, String method, JSONObject content) {
		String sql = String.format("insert into %s(`mq_key`,`taskSn`,`type`,`method`,`content`) values(?,?,?,?,?)", table);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		int value = jdbcTemplate.update(conn -> {
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setObject(1, mqKey);
			ps.setObject(2, taskSn);
			ps.setObject(3, type);
			ps.setObject(4, method);
			ps.setObject(5, JSON.toJSONString(content, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty,
					SerializerFeature.WriteDateUseDateFormat));
			return ps;
		}, keyHolder);
		if (value > 0 && keyHolder.getKey() != null) {
			return keyHolder.getKey().longValue();
		}
		return -1;
	}

	public void updateLog(String table, Long id, Map<String, Object> params) {
		Object[] values = new Object[params.size() + 1];
		AtomicInteger i = new AtomicInteger(0);
		List<String> fieldItems = new ArrayList<>(params.size() + 1);
		params.entrySet().forEach(value -> {
			fieldItems.add(String.format("%s = ?", value.getKey()));
			values[i.getAndIncrement()] = value.getValue();
		});
		fieldItems.add("`update_time` = now() , `try_num` = `try_num` + 1");
		String sql = String.format(
				"update %s set %s where id = ?",
				table, String.join(",", fieldItems));

		jdbcTemplate.update(sql, values, id);
	}

	public void createTable(String table) {
		String yearMonth = DateUtil.format(DateUtil.date(), "yyyyMM");
		String tableName = String.format("%s_%s", table, yearMonth);
		String sql = String.format("CREATE TABLE IF NOT EXISTS %s LIKE %s;", tableName, table);
		jdbcTemplate.execute(sql);
	}

	public void moveFinishLog(String table) {
		String yearMonth = DateUtil.format(DateUtil.date(), "yyyyMM");
		String tableName = String.format("%s_%s", table, yearMonth);
		String sql = String.format(
				"INSERT INTO %s SELECT * FROM %s WHERE create_time BETWEEN ? AND ? AND `success` = 1;", tableName, table);
		DateTime date = DateUtil.date();
		jdbcTemplate.update(sql, DateUtil.beginOfMonth(date), DateUtil.endOfMonth(date));
	}

	public BasicInfoBo getBasicInfo(String table, Long id) {
		String sql = String.format("SELECT %s FROM %s WHERE `id` = ? LIMIT 1", fieldItems, table);
		List<BasicInfoBo> results = getBasicInfoBos(sql, id);
		return results.isEmpty() ? null : results.get(0);
	}

	public List<BasicInfoBo> queryBasicInfos(String table) {
		return queryBasicInfos(table, null, false);
	}

	/**
	 * 获取最近三天最大或者最小的数据id
	 * 
	 * @param table   表名
	 * @param type    数据类型
	 * @param isMaxId 是否最大值Id
	 * 
	 * @return 返回最大或者最小id值
	 */
	public long getLastThreeDayId(String table, String type, boolean isMaxId) {
		DateTime dateTime = DateUtil.offsetDay(DateUtil.date(), -3);
		Object[] idParams = Stream.of(dateTime, type).toArray();
		String idValue = " MIN(id) ";
		if (isMaxId) {
			idValue = " MAX(id) ";
		}
		String sqlIdQuery = String.format("SELECT %s AS id FROM %s WHERE create_time > ? AND `type` = ? AND success = 0 LIMIT 1", idValue, table);
		if (StringUtils.isEmpty(type)) {
			idParams = Stream.of(dateTime).toArray();
			sqlIdQuery = String.format("SELECT %s AS id FROM %s WHERE create_time > ? AND success = 0 LIMIT 1", idValue, table);
		}
		List<Map<String, Object>> maps = jdbcTemplate.queryForList(sqlIdQuery, idParams);
		long id = 0;
		if (!maps.isEmpty()) {
			BigInteger value = (BigInteger) maps.get(0).get("id");
			id = value.intValue();
		}
		return id;
	}

	public List<BasicInfoBo> queryBasicInfos(String table, String type, boolean orderByMaxId) {
		long id = getLastThreeDayId(table, type, orderByMaxId);

		return queryBasicInfos(table, type, id, orderByMaxId);
	}

	public List<BasicInfoBo> queryBasicInfos(String table, String type, long id, boolean isMaxId) {
		final Pair<String, Object[]> sqlParams = getSqlParams(table, type, id, isMaxId);
		String sql = sqlParams.getKey();
		Object[] params = sqlParams.getValue();
		List<BasicInfoBo> results = getBasicInfoBos(sql, params);
		return results;
	}

	private List<BasicInfoBo> getBasicInfoBos(String sql, Object... params) {
		List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql, params);
		List<BasicInfoBo> results = new ArrayList<>();
		maps.stream().forEach(item -> {
			BasicInfoBo basicInfoBo = new BasicInfoBo();
			setBasicInfoBo(basicInfoBo, item);
			results.add(basicInfoBo);
		});
		return results;
	}

	private Pair<String, Object[]> getSqlParams(String table, String type, long id, boolean isMaxId) {
		DateTime dateTime = DateUtil.offsetDay(DateUtil.date(), -3);
		String querySqlOrder = " ORDER BY id DESC ";
		List<Object> params = new ArrayList<>(3);
		params.add(id);
		params.add(dateTime);
		params.add(type);
		if (!isMaxId) {
			params.remove(1);
			querySqlOrder = " ORDER BY id ASC ";
		}

		String querySqlTable = String.format("SELECT %s FROM %s ", fieldItems, table);
		String querySqlCondition = isMaxId ? " WHERE id <= ?  AND create_time > ? " : " WHERE id >= ? ";
		if (StringUtils.isEmpty(type)) {
			params.remove(params.size() - 1);
		} else {
			querySqlCondition += " AND `type` = ? ";
		}
		String sql = String.format(
				"%s  %s AND `success` = 0 AND `try_num` BETWEEN 0 AND 3 %s LIMIT 500", querySqlTable, querySqlCondition, querySqlOrder);

		return Pair.of(sql, params.toArray());
	}

	protected void setBasicInfoBo(BasicInfoBo basicInfoBo, Map<String, Object> item) {
		BigInteger value = (BigInteger) item.get("id");
		basicInfoBo.setId(value.longValue());
		basicInfoBo.setMqKey((String) item.get("mq_key"));
		basicInfoBo.setTaskSn((String) item.get("taskSn"));
		basicInfoBo.setType((String) item.get("type"));
		basicInfoBo.setMethod((String) item.get("method"));
		Object content = item.get("content");
		if (content != null) {
			JSONObject jsonObject = JSON.parseObject(String.valueOf(content));
			basicInfoBo.setContent(jsonObject);
		}

		basicInfoBo.setResult((String) item.get("result"));
		basicInfoBo.setSuccess((Boolean) item.get("success"));
		basicInfoBo.setCreateTime((Date) item.get("create_time"));
		basicInfoBo.setUpdateTime((Date) item.get("update_time"));
		basicInfoBo.setTryNum((Integer) item.get("try_num"));
	}

	private final static String fieldItems = " `id`,`mq_key`,`taskSn`,`type`,`method`,`content`,`result`,`success`,`create_time`,`update_time`,`try_num` ";
}
