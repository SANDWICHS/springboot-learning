package cn.zwq.service;

import java.util.List;
import java.util.Map;

/**
 * @author zhangwenqia
 * @create 2022-07-18 9:37
 * @description 类描述
 */
public interface SqlService {
	int update(String sql, Object... params);

	List<Map<String, Object>> query(String sql, Object... params);
}
