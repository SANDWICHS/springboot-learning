package cn.zwq.controller;

import cn.zwq.dto.WfTaskCarryDto;
import cn.zwq.dto.WfTaskDto;
import cn.zwq.dto.WfTaskProcessUserDto;
import cn.zwq.entities.CommonResults;
import cn.zwq.service.WfTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zhangwenqia
 * @create 2023-03-22 18:18
 * @description 类描述
 */
@RestController
@RequestMapping("/task")
public class TaskController {
	@Resource
	WfTaskService wfTaskService;

	@GetMapping("/query/type")
	public CommonResults queryType(@RequestParam("type") String type,
			@RequestParam(value = "version") Integer version,
			@RequestParam(value = "currentStatus") Integer currentStatus,
			@RequestParam(value = "userId", required = false) Integer userId,
			@RequestParam(value = "index") Integer index,
			@RequestParam(value = "pageSize") Integer pageSize) {

		Map<String, Object> result = new HashMap<>(2);
		switch (type) {
		case "WfTaskTable":
			final Pair<Integer, List<WfTaskDto>> taskValues = wfTaskService.queryWfTaskDb(version, currentStatus, Pair.of(index, pageSize));
			result.put("total", taskValues.getLeft());
			result.put("data", taskValues.getRight());
			break;
		case "WfTaskCarryTable":
			final Pair<Integer, List<WfTaskCarryDto>> taskCarryValues = wfTaskService.queryWfTaskCarryDb(version, currentStatus,
					Pair.of(index, pageSize));
			result.put("total", taskCarryValues.getLeft());
			result.put("data", taskCarryValues.getRight());
			break;
		case "WfTaskProcessUserTable":
			final Pair<Integer, List<WfTaskProcessUserDto>> taskProcessUserValues = wfTaskService.queryWfTaskProcessUserDb(version, userId,
					currentStatus,
					Pair.of(index, pageSize));
			result.put("total", taskProcessUserValues.getLeft());
			result.put("data", taskProcessUserValues.getRight());
			break;
		default:
			break;
		}
		return new CommonResults(0, "success", result);
	}

	@GetMapping("/create/type")
	public CommonResults createTable(@RequestParam("type") String type, @RequestParam("version") Integer version) {
		switch (type) {
		case "WfTaskTable":
			wfTaskService.createWfTaskTableDb(version);
			break;
		case "WfTaskCarryTable":
			wfTaskService.createWfTaskCarryTableDb(version);
			break;
		case "WfTaskProcessUserTable":
			wfTaskService.createWfTaskProcessUserTableDb(version);
			break;
		default:
			break;
		}
		return new CommonResults(0, "success", "建表成功");
	}

	@GetMapping("/load/type")
	public CommonResults loadResource(@RequestParam("type") String type, @RequestParam("version") Integer version) {
		int totalCount = 0;
		Integer maxId = 0;
		List<Map<String, Object>> mapList = wfTaskService.queryTableInfos(type, version, maxId);
		while (!mapList.isEmpty()) {
			switch (type) {
			case "WfTaskTable":
				List<WfTaskDto> wfTaskDtos = new ArrayList<>(mapList.size());
				mapList.stream().forEach(value -> {
					WfTaskDto wfTaskDto = new WfTaskDto();
					wfTaskDtos.add(wfTaskDto);
					wfTaskDto.setId((int) value.get("id"));
					wfTaskDto.setName((String) value.get("name"));
					wfTaskDto.setDescription((String) value.get("description"));
					wfTaskDto.setCurrentStatus((int) value.get("current_status"));
					wfTaskDto.setCreateUser((String) value.get("create_user"));
					wfTaskDto.setCreateTime((Date) value.get("create_time"));
					wfTaskDto.setLastUser((String) value.get("last_user"));
					wfTaskDto.setLastTime((Date) value.get("last_time"));
					wfTaskDto.setPushUser((String) value.get("push_user"));
					wfTaskDto.setPushTime((Date) value.get("push_time"));
					wfTaskDto.setCurrentTimeLimit((Date) value.get("current_time_limit"));
					wfTaskDto.setCurrentTimeLimit2((Date) value.get("current_time_limit2"));
					wfTaskDto.setTotalTimeLimit((Date) value.get("total_time_limit"));
					wfTaskDto.setTotalTimeLimit2((Date) value.get("total_time_limit2"));
					final HashMap<Integer, String> conditionFieldMap = new HashMap<>(20);
					wfTaskDto.setConditionFieldMap(conditionFieldMap);
					for (int i = 1; i <= 20; i++) {
						conditionFieldMap.put(i, (String) value.get(String.format("condition_field_%d", i)));
					}
					wfTaskDto.setTaskType((Integer) value.get("task_type"));
				});
				totalCount += wfTaskDtos.size();
				maxId = wfTaskDtos.get(wfTaskDtos.size() - 1).getId();
				wfTaskService.insertWfTaskDb(version, wfTaskDtos);
				break;
			case "WfTaskCarryTable":
				List<WfTaskCarryDto> wfTaskCarryDtos = new ArrayList<>(mapList.size());
				mapList.stream().forEach(value -> {
					WfTaskCarryDto wfTaskCarryDto = new WfTaskCarryDto();
					wfTaskCarryDtos.add(wfTaskCarryDto);

					wfTaskCarryDto.setTaskId((int) value.get("task_id"));
					wfTaskCarryDto.setCarry((int) value.get("carry"));
					wfTaskCarryDto.setCurrentStatus((int) value.get("current_status"));
					wfTaskCarryDto.setPushUser((String) value.get("push_user"));
					wfTaskCarryDto.setPushTime((Date) value.get("push_time"));
					wfTaskCarryDto.setCurrentTimeLimit((Date) value.get("current_time_limit"));
					wfTaskCarryDto.setCurrentTimeLimit2((Date) value.get("current_time_limit2"));
					wfTaskCarryDto.setTaskType((Integer) value.get("task_type"));
				});
				totalCount += wfTaskCarryDtos.size();
				maxId = wfTaskCarryDtos.get(wfTaskCarryDtos.size() - 1).getTaskId();
				wfTaskService.insertWfTaskCarryDb(version, wfTaskCarryDtos);
				break;
			case "WfTaskProcessUserTable":
				List<WfTaskProcessUserDto> wfTaskProcessUserDtos = new ArrayList<>(mapList.size());
				mapList.stream().forEach(value -> {
					WfTaskProcessUserDto wfTaskProcessUserDto = new WfTaskProcessUserDto();
					wfTaskProcessUserDtos.add(wfTaskProcessUserDto);

					wfTaskProcessUserDto.setTaskId((int) value.get("task_id"));
					wfTaskProcessUserDto.setCurrentStatus((int) value.get("current_status"));
					wfTaskProcessUserDto.setProcessUser((int) value.get("process_user"));
					wfTaskProcessUserDto.setTimeOuted((Date) value.get("time_outed"));
					wfTaskProcessUserDto.setTimeOuting((Date) value.get("time_outing"));
				});
				totalCount += wfTaskProcessUserDtos.size();
				maxId = wfTaskProcessUserDtos.get(wfTaskProcessUserDtos.size() - 1).getTaskId();
				wfTaskService.insertWfTaskProcessUserDb(version, wfTaskProcessUserDtos);
				break;
			default:
				break;
			}
			mapList = wfTaskService.queryTableInfos(type, version, maxId);
		}

		return new CommonResults(0, "success", String.format("总共入库%d条数据", totalCount));
	}

	@GetMapping("/optimize/type")
	public CommonResults optimizeTable(@RequestParam("type") String type, @RequestParam("version") Integer version) {
		switch (type) {
		case "WfTaskTable":
			wfTaskService.optimizeWfTaskTableDb(version);
			break;
		case "WfTaskCarryTable":
			wfTaskService.optimizeWfTaskCarryTableDb(version);
			break;
		case "WfTaskProcessUserTable":
			wfTaskService.optimizeWfTaskProcessUserTableDb(version);
			break;
		default:
			break;
		}
		return new CommonResults(0, "success", "合并数据成功");
	}

	@GetMapping("/truncate/type")
	public CommonResults truncateTable(@RequestParam("type") String type, @RequestParam("version") Integer version) {
		switch (type) {
		case "WfTaskTable":
			wfTaskService.truncateWfTaskTableDb(version);
			break;
		case "WfTaskCarryTable":
			wfTaskService.truncateWfTaskCarryTableDb(version);
			break;
		case "WfTaskProcessUserTable":
			wfTaskService.truncateWfTaskProcessUserTableDb(version);
			break;
		default:
			break;
		}
		return new CommonResults(0, "success", "清空数据成功");
	}

	@PostMapping("/delete/type")
	public CommonResults deleteRowValue(@RequestBody JSONObject param) {
		final String type = param.getString("type");
		final Integer version = param.getInteger("version");
		final JSONArray rows;
		switch (type) {
		case "WfTaskTable":
			rows = param.getJSONArray("rows");
			List<WfTaskDto> wfTaskDtos = new ArrayList<>(rows.size());
			for (int i = 0; i < rows.size(); i++) {
				final JSONObject value = rows.getJSONObject(i);
				WfTaskDto wfTaskDto = new WfTaskDto();
				wfTaskDtos.add(wfTaskDto);
				wfTaskDto.setId((int) value.get("id"));
				wfTaskDto.setName((String) value.get("name"));
				wfTaskDto.setDescription((String) value.get("description"));
				wfTaskDto.setCurrentStatus((int) value.get("current_status"));
				wfTaskDto.setCreateUser((String) value.get("create_user"));
				wfTaskDto.setCreateTime((Date) value.get("create_time"));
				wfTaskDto.setLastUser((String) value.get("last_user"));
				wfTaskDto.setLastTime((Date) value.get("last_time"));
				wfTaskDto.setPushUser((String) value.get("push_user"));
				wfTaskDto.setPushTime((Date) value.get("push_time"));
				wfTaskDto.setCurrentTimeLimit((Date) value.get("current_time_limit"));
				wfTaskDto.setCurrentTimeLimit2((Date) value.get("current_time_limit2"));
				wfTaskDto.setTotalTimeLimit((Date) value.get("total_time_limit"));
				wfTaskDto.setTotalTimeLimit2((Date) value.get("total_time_limit2"));
				final HashMap<Integer, String> conditionFieldMap = new HashMap<>(20);
				wfTaskDto.setConditionFieldMap(conditionFieldMap);
				for (int j = 1; j <= 20; i++) {
					conditionFieldMap.put(j, (String) value.get(String.format("condition_field_%d", j)));
				}
				wfTaskDto.setTaskType((Integer) value.get("task_type"));
			}
			wfTaskService.deleteWfTaskDb(version, wfTaskDtos);
			break;
		case "WfTaskCarryTable":
			rows = param.getJSONArray("rows");
			List<WfTaskCarryDto> wfTaskCarryDtos = new ArrayList<>(rows.size());
			for (int i = 0; i < rows.size(); i++) {
				final JSONObject value = rows.getJSONObject(i);
				WfTaskCarryDto wfTaskCarryDto = new WfTaskCarryDto();
				wfTaskCarryDtos.add(wfTaskCarryDto);

				wfTaskCarryDto.setTaskId((int) value.get("task_id"));
				wfTaskCarryDto.setCarry((int) value.get("carry"));
				wfTaskCarryDto.setCurrentStatus((int) value.get("current_status"));
				wfTaskCarryDto.setPushUser((String) value.get("push_user"));
				wfTaskCarryDto.setPushTime((Date) value.get("push_time"));
				wfTaskCarryDto.setCurrentTimeLimit((Date) value.get("current_time_limit"));
				wfTaskCarryDto.setCurrentTimeLimit2((Date) value.get("current_time_limit2"));
				wfTaskCarryDto.setTaskType((Integer) value.get("task_type"));
			}
			wfTaskService.deleteWfTaskCarryDb(version, wfTaskCarryDtos);
			break;
		case "WfTaskProcessUserTable":
			rows = param.getJSONArray("rows");
			List<WfTaskProcessUserDto> wfTaskProcessUserDtos = new ArrayList<>(rows.size());
			for (int i = 0; i < rows.size(); i++) {
				final JSONObject value = rows.getJSONObject(i);
				WfTaskProcessUserDto wfTaskProcessUserDto = new WfTaskProcessUserDto();
				wfTaskProcessUserDtos.add(wfTaskProcessUserDto);

				wfTaskProcessUserDto.setTaskId((int) value.get("task_id"));
				wfTaskProcessUserDto.setCurrentStatus((int) value.get("current_status"));
				wfTaskProcessUserDto.setProcessUser((int) value.get("process_user"));
				wfTaskProcessUserDto.setTimeOuted((Date) value.get("time_outed"));
				wfTaskProcessUserDto.setTimeOuting((Date) value.get("time_outing"));
			}
			wfTaskService.deleteWfTaskProcessUserDb(version, wfTaskProcessUserDtos);
			break;
		default:
			break;
		}
		return new CommonResults(0, "success", "合并数据成功");
	}

}
