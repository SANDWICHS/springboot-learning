package cn.zwq.schedule;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import cn.zwq.cat.util.CatUtils;
import cn.zwq.dto.WfTaskCarryDto;
import cn.zwq.dto.WfTaskDto;
import cn.zwq.dto.WfTaskProcessUserDto;
import cn.zwq.service.WfTaskService;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import cn.hutool.core.date.DateUtil;

import javax.annotation.Resource;

/**
 * @author zhangwenqia
 * @create 2022-07-15 11:26
 * @description 类描述
 */
@Service
public class JdbcTest {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(JdbcTest.class);

	@Resource(name = "mysqlJdbcTemplate")
	JdbcTemplate mysqlJdbcTemplate;

	@Resource
	WfTaskService wfTaskService;

	/** redis 客户端 */
	private final StringRedisTemplate redisTemplate;

	@Autowired
	public JdbcTest(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Scheduled(cron = "*/1 1 * * * ?")
	@Async
	// @Transactional
	public void execute() {
		final Transaction transaction = CatUtils.newTransaction(CatUtils.TYPE_SERVICE, "JdbcTest.execute");
		try {
			logger.info("JdbcTest检查是否能执行调度：{}", DateUtil.date());
			AtomicInteger id = new AtomicInteger(0);
			String sql = "SELECT id, mq_key, taskSn, `type`, `method`, url, content, `result`, success, create_time, update_time, try_num\n" +
					"FROM mtnoh_aaa_task.tb_task_external_info_202303 where id > ? order by id limit 500";
			List<Map<String, Object>> mapList = mysqlJdbcTemplate.queryForList(sql, id.get());
			int count = 1;
			while (!mapList.isEmpty()) {
				mapList.parallelStream().forEach(value -> {
					BigInteger bigInteger = (BigInteger) value.get("id");
					id.set(bigInteger.intValue());
					testExternal(value);
				});
				count++;
				logger.info("JdbcTest检查是否能执行调度：{} {}", count, DateUtil.date());
				Thread.sleep(1000);
				mapList = mysqlJdbcTemplate.queryForList(sql, id.get());
			}
			logger.info("JdbcTest检查是否能执行调度：{}", DateUtil.date());
		} catch (Exception e) {
			logger.error("JdbcTest检查是否能执行调度", e);
			CatUtils.setStatus(transaction, e);
		} finally {
			CatUtils.closeTransaction(transaction);
		}
	}

	private void testExternal(Map<String, Object> value) {
		final Transaction transaction = CatUtils.newTransaction(CatUtils.TYPE_SERVICE, "JdbcTest.testExternal");
		transaction.setStatus(Message.SUCCESS);
		String redisKey = "testExternal:";
		Boolean redisResult = false;
		try {
			BigInteger bigInteger = (BigInteger) value.get("id");
			final int id = bigInteger.intValue();
			redisKey += id;
			logger.info("testExternal:{}", id);
			redisResult = redisTemplate.opsForValue().setIfAbsent(redisKey, "");
			if (redisResult) {
				String sql = "SELECT id, mq_key, taskSn, `type`, `method`, url, content, `result`, success, create_time, update_time, try_num\n" +
						"FROM mtnoh_aaa_task.tb_task_external_info_202303 where id = ? limit 1";
				List<Map<String, Object>> mapList = mysqlJdbcTemplate.queryForList(sql, id);
				if (mapList.isEmpty()) {
					logger.info("数据不存在");
				} else {
					Thread.sleep(19000);
				}
				final int update = mysqlJdbcTemplate.update(
						"update mtnoh_aaa_task.tb_task_external_info_202303 set result = ?,update_time = now(),try_num = 1 where id = ?",
						"成功", id);
				logger.info("更新结果：{}", update);
			}
		} catch (Exception e) {
			logger.error("信息", e);
			CatUtils.setStatus(transaction, e);
		} finally {
			if (redisResult) {
				redisTemplate.delete(redisKey);
			}
			CatUtils.closeTransaction(transaction);
		}
	}

	private void test1() {
		final Transaction transaction = Cat.newTransaction("JdbcTest", "test");
		try {
			logger.info("test1");
			int totalCount = 0;
			Integer maxId = 0;
			int version = 1;
			String type = "WfTaskTable";
			List<Map<String, Object>> subMapList = wfTaskService.queryTableInfos(type, version, maxId);
			while (!subMapList.isEmpty()) {
				List<WfTaskDto> wfTaskDtos = new ArrayList<>(subMapList.size());
				subMapList.stream().forEach(subValue -> {
					WfTaskDto wfTaskDto = new WfTaskDto();
					wfTaskDtos.add(wfTaskDto);
					wfTaskDto.setId((int) subValue.get("id"));
					wfTaskDto.setName((String) subValue.get("name"));
					wfTaskDto.setDescription((String) subValue.get("description"));
					wfTaskDto.setCurrentStatus((int) subValue.get("current_status"));
					wfTaskDto.setCreateUser((String) subValue.get("create_user"));
					wfTaskDto.setCreateTime((Date) subValue.get("create_time"));
					wfTaskDto.setLastUser((String) subValue.get("last_user"));
					wfTaskDto.setLastTime((Date) subValue.get("last_time"));
					wfTaskDto.setPushUser((String) subValue.get("push_user"));
					wfTaskDto.setPushTime((Date) subValue.get("push_time"));
					wfTaskDto.setCurrentTimeLimit((Date) subValue.get("current_time_limit"));
					wfTaskDto.setCurrentTimeLimit2((Date) subValue.get("current_time_limit2"));
					wfTaskDto.setTotalTimeLimit((Date) subValue.get("total_time_limit"));
					wfTaskDto.setTotalTimeLimit2((Date) subValue.get("total_time_limit2"));
					final HashMap<Integer, String> conditionFieldMap = new HashMap<>(20);
					wfTaskDto.setConditionFieldMap(conditionFieldMap);
					for (int i = 1; i <= 20; i++) {
						conditionFieldMap.put(i, (String) subValue.get(String.format("condition_field_%d", i)));
					}
					wfTaskDto.setTaskType((Integer) subValue.get("task_type"));
				});
				maxId = wfTaskDtos.get(wfTaskDtos.size() - 1).getId();
				// wfTaskService.insertWfTaskDb(version, wfTaskDtos);
				totalCount += wfTaskDtos.size();
				if (totalCount > 2000) {
					logger.info("退出");
					break;
				}
				subMapList = wfTaskService.queryTableInfos(type, version, maxId);
			}
		} catch (Exception e) {
			logger.error("信息", e);
			transaction.setStatus(e);
		} finally {
			transaction.complete();
		}
	}

}
