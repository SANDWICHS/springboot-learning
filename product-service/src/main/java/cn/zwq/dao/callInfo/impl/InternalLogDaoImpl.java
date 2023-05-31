package cn.zwq.dao.callInfo.impl;

import java.util.*;

import cn.zwq.dao.callInfo.InternalLogDao;
import cn.zwq.dto.callInfo.BasicInfoBo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangwenqia
 * @create 2022-03-08 19:23
 * @description 类描述
 */
@Service
public class InternalLogDaoImpl extends BasicLogDaoImpl implements InternalLogDao {
	JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("mysqlJdbcTemplate")
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	final static String table = "mtnoh_aaa_task.tb_task_internal_info";

	@Override
	public long addLog(String mqKey, String taskSn, String type, String method, JSONObject content) {
		return addLog(table, mqKey, taskSn, type, method, content);
	}

	@Override
	public void updateLog(Long id, String className, String result, Boolean success) {
		Map<String, Object> params = new HashMap<>(3);
		params.put("className", className);
		params.put("result", result);
		params.put("success", success);
		updateLog(table, id, params);
	}

	@Override
	public void createTable() {
		createTable(table);
	}

	@Override
	public void moveFinishLog() {
		moveFinishLog(table);
	}

	@Override
	public BasicInfoBo getInternalInfo(Long id) {
		return getBasicInfo(table, id);
	}

	@Override
	public List<BasicInfoBo> queryInternalInfos() {
		return queryInternalInfos(null);
	}

	@Override
	public List<BasicInfoBo> queryInternalInfos(String type, long maxId) {
		return queryBasicInfos(table, type, maxId, true);
	}

	@Override
	public List<BasicInfoBo> queryInternalInfos(String type) {
		return queryBasicInfos(table, type, false);
	}

}
