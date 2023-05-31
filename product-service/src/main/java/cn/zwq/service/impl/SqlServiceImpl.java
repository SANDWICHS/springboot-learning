package cn.zwq.service.impl;

import cn.zwq.service.SqlService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author zhangwenqia
 * @create 2023-04-04 10:02
 * @description 类描述
 */
@Service(value = "sqlService")
public class SqlServiceImpl implements SqlService {
	@Resource(name = "mysqlJdbcTemplate")
	JdbcTemplate mysqlJdbcTemplate;

	@Override
	public int update(String sql, Object... params) {
		return mysqlJdbcTemplate.update(sql, params);
	}

	@Override
	public List<Map<String, Object>> query(String sql, Object... params) {
		return mysqlJdbcTemplate.queryForList(sql, params);
	}
}
