package cn.zwq.service;

import cn.zwq.dto.WfTaskCarryDto;
import cn.zwq.dto.WfTaskDto;
import cn.zwq.dto.WfTaskProcessUserDto;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

/**
 * @author zhangwenqia
 * @create 2023-03-20 17:56
 * @description 类描述
 */
public interface WfTaskService {
	void insertWfTaskDb(Integer versionId, List<WfTaskDto> tasks);

	void insertWfTaskCarryDb(Integer versionId, List<WfTaskCarryDto> taskCarries);

	void insertWfTaskProcessUserDb(Integer versionId, List<WfTaskProcessUserDto> taskProcessUsers);

	void deleteWfTaskDb(Integer versionId, List<WfTaskDto> tasks);

	void deleteWfTaskCarryDb(Integer versionId, List<WfTaskCarryDto> taskCarries);

	void deleteWfTaskProcessUserDb(Integer versionId, List<WfTaskProcessUserDto> taskProcessUsers);

	Pair<Integer, List<WfTaskDto>> queryWfTaskDb(Integer versionId, Integer currentStatus, Pair<Integer, Integer> pageIndexRowCount);

	Pair<Integer, List<WfTaskCarryDto>> queryWfTaskCarryDb(Integer versionId, Integer currentStatus,
			Pair<Integer, Integer> pageIndexRowCount);

	Pair<Integer, List<WfTaskProcessUserDto>> queryWfTaskProcessUserDb(Integer versionId, Integer userId, Integer currentStatus,
			Pair<Integer, Integer> pageIndexRowCount);

	void createWfTaskTableDb(Integer versionId);

	void createWfTaskCarryTableDb(Integer versionId);

	void createWfTaskProcessUserTableDb(Integer versionId);

	void optimizeWfTaskTableDb(Integer versionId);

	void optimizeWfTaskCarryTableDb(Integer versionId);

	void optimizeWfTaskProcessUserTableDb(Integer versionId);

	void truncateWfTaskTableDb(Integer versionId);

	void truncateWfTaskCarryTableDb(Integer versionId);

	void truncateWfTaskProcessUserTableDb(Integer versionId);

	List<Map<String, Object>> queryTableInfos(String type, Integer versionId, Integer maxId);
}
