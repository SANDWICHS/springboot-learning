package cn.zwq.dao.callInfo.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.zwq.dao.callInfo.ExternalLogDao;
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
public class ExternalLogDaoImpl extends BasicLogDaoImpl implements ExternalLogDao {
	JdbcTemplate jdbcTemplate;

	@Autowired
	@Qualifier("mysqlJdbcTemplate")
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	final static String table = "mtnoh_aaa_task.tb_task_external_info";

	@Override
	public long addLog(String mqKey, String taskSn, String type, String method, JSONObject content) {
		return addLog(table, mqKey, taskSn, type, method, content);
	}

	@Override
	public void updateLog(Long id, String url, String result, Boolean success) {
		Map<String, Object> params = new HashMap<>(3);
		params.put("url", url);
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
	public BasicInfoBo getExternalInfo(Long id) {
		return getBasicInfo(table, id);
	}

	@Override
	public List<BasicInfoBo> queryExternalInfos() {
		return queryExternalInfos(null);
	}

	@Override
	public List<BasicInfoBo> queryExternalInfos(String type) {
		return queryBasicInfos(table, type, false);
	}

	@Override
	public List<BasicInfoBo> queryExternalInfos(String type, long maxId) {
		return queryBasicInfos(table, type, maxId, true);
	}

}
