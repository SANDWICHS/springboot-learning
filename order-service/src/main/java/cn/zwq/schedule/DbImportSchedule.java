package cn.zwq.schedule;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.ssh.JschUtil;
import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import cn.zwq.conf.CallProperties;
import cn.zwq.conf.FtpProperties;
import cn.zwq.util.SftpClientUtil;

import com.dianping.cat.message.Transaction;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.unidal.tuple.Pair;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * @author zhangwenqia
 * @create 2022-08-08 16:33
 * @description 故障工单短信通知调度
 */
@Service
public class DbImportSchedule {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	JdbcTemplate jdbcTemplate;
	RedissonClient redissonClient;
	CallProperties callProperties;

	static final Integer BATCH_COUNT = 200;

	@Autowired
	public void setCallProperties(CallProperties callProperties) {
		this.callProperties = callProperties;
	}

	@Autowired
	public void setRedissonClient(RedissonClient redissonClient) {
		this.redissonClient = redissonClient;
	}

	@Autowired
	@Qualifier("mysqlJdbcTemplate")
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	private RLock getLock(String key) {
		return redissonClient.getLock(key);
	}

	private boolean tryLock(RLock clientLock, Integer time, TimeUnit timeUnit) throws InterruptedException {
		time = Optional.ofNullable(time).orElse(30);
		timeUnit = Optional.ofNullable(timeUnit).orElse(TimeUnit.SECONDS);
		return clientLock.tryLock(time, timeUnit);
	}

	private void unLock(RLock lock, boolean isLock) {
		if (Boolean.TRUE.equals(isLock)) {
			lock.unlock();
		}
	}

	ChannelSftp channelSftp = null;

	@Scheduled(cron = "0/20 * * * * ?")
	@Async
	public void oilEngineImport() {
		Transaction transaction = null;

		String lockName = "TaskSchedule:OilEngineImport";
		boolean isLock = false;
		RLock lock = getLock(lockName);
		Session session = null;
		SftpClientUtil sftpClientUtil = null;
		String traceId = "";
		try {
			isLock = tryLock(lock, 30, TimeUnit.SECONDS);
			if (!isLock) {
				return;
			}
			FtpProperties sftpOilEngineImport = callProperties.getFtpProperties("sftpOilEngineImport");
			if (sftpOilEngineImport == null) {
				logger.error("sftpOilEngineImport配置为空");
				return;
			}
			transaction = CatUtils.newTransaction(CatUtils.TYPE_TASK_SERVICE, "oilEngineImport");
			traceId = CatServiceLogUtils.initTraceId();
			String host = sftpOilEngineImport.getIp();
			int sftpPort = Optional.ofNullable(sftpOilEngineImport.getPort()).orElse(22);
			int timeout = Optional.ofNullable(sftpOilEngineImport.getTimeout()).orElse(20000);
			String username = sftpOilEngineImport.getUserName();
			String password = sftpOilEngineImport.getMiMa();
			String path = sftpOilEngineImport.getRemotePath();

			// 利用JschUtil获取会话连接
			session = JschUtil.openSession(host, sftpPort, username, password);
			// 获取sftp通道
			channelSftp = JschUtil.openSftp(session, timeout);

			initTypeFieldNames();

			// 上传
			List<String> fileNames = getPathFileNames(channelSftp, path);
			logger.info("服务器上油调附件信息：{}", String.join(",", fileNames));

			Map<String, List<String>> typeNameFiles = new HashMap<>();
			typeTableNames.keySet().forEach(type -> {
				typeNameFiles.put(type, new ArrayList<>());
				fileNames.stream().forEach(value -> {
					if (value.startsWith(type)) {
						typeNameFiles.get(type).add(value);
					}
				});
			});

			List<String> existFileNames = new CopyOnWriteArrayList<>();
			StringBuffer importInfos = new StringBuffer();
			importInfos.append(System.lineSeparator());

			typeNameFiles.entrySet().parallelStream().forEach(value -> {
				value.getValue().sort(String::compareTo);
				value.getValue().forEach(fileName -> readDatFileImport(channelSftp, path, existFileNames, importInfos, fileName));
			});

			logger.info("已经入库的油调附件信息：{}", String.join(",", existFileNames));

			logger.info("这批次入库成功的信息:{}", importInfos.toString());
		} catch (Exception e) {
			String error = "油调数据入库异常";
			logger.error(error, e);
			CatUtils.setStatus(transaction, e);
		} finally {
			JschUtil.close(channelSftp);
			JschUtil.close(session);
			Optional.ofNullable(sftpClientUtil).ifPresent(value -> value.disconnect());
			CatUtils.closeTransaction(transaction);
			CatServiceLogUtils.clearTraceId(traceId);
			unLock(lock, isLock);
		}
	}

	private void readDatFileImport(ChannelSftp channelSftp, String path, List<String> existFileNames, StringBuffer importInfos, String fileName) {
		Integer importRow = readDatFileImport(path, channelSftp, fileName, existFileNames);
		if (importRow != null && importRow.intValue() != -1) {
			importInfos.append(String.format("入库文件file:%s,文件内容行数:%d", fileName, importRow));
			importInfos.append(System.lineSeparator());
		}
	}

	private List<String> getPathFileNames(ChannelSftp channelSftp, String path) throws SftpException {
		List<ChannelSftp.LsEntry> ls = channelSftp.ls(path);
		Iterator<ChannelSftp.LsEntry> iterator = ls.iterator();
		List<String> fileNames = new ArrayList<>();
		while (iterator.hasNext()) {
			ChannelSftp.LsEntry lsEntry = iterator.next();
			String filename = lsEntry.getFilename();
			if (!(".".equals(filename) || "..".equals(filename))) {
				fileNames.add(filename);
			}
		}
		return fileNames;
	}

	private int checkFile(String file) {
		val today = LocalDateTime.now();
		for (int i = 0; i <= 2; i++) {
			val checkDateTime = today.minusDays(i);
			String dateTime = DateUtil.format(checkDateTime, "yyyyMMdd");
			if (file.contains(dateTime)) {
				return file.indexOf(dateTime);
			}
		}
		return -1;
	}

	private Integer readDatFileImport(String path, ChannelSftp channelSftp, String file, List<String> existFileNames) {
		Transaction transaction = null;
		boolean isLock = false;
		RLock lock = null;
		File saveFile = null;
		try {
			int index = checkFile(file);
			if (index == -1) {
				return -1;
			}
			String typeName = file.substring(0, index);
			String tableName = typeTableNames.get(typeName);
			if (StringUtils.isEmpty(tableName)) {
				logger.warn("非法文件类型暂时不支持：{}", file);
				return -1;
			}

			String lockName = String.format("TaskSchedule:ReadDatFileImport:%s", file);

			lock = getLock(lockName);
			isLock = tryLock(lock, 30, TimeUnit.SECONDS);
			if (!isLock) {
				return -1;
			}
			transaction = CatUtils.newTransaction(CatUtils.TYPE_TASK_SERVICE, tableName);

			Pair<File, Integer> fileIntegerPair = fileDataImport(path, channelSftp, file, typeName, tableName, existFileNames);

			rmFile(channelSftp, file);

			if (fileIntegerPair != null) {
				saveFile = fileIntegerPair.getKey();

				return fileIntegerPair.getValue();
			}
		} catch (Exception e) {
			logger.error("数据入库异常", e);
			CatUtils.setStatus(transaction, e);
		} finally {
			if (saveFile != null) {
				logger.info("删除本地文件：{}", saveFile.toString());
				org.apache.commons.io.FileUtils.deleteQuietly(saveFile);
			}
			unLock(lock, isLock);
			CatUtils.closeTransaction(transaction);
		}

		return -1;
	}

	private synchronized void rmFile(ChannelSftp channelSftp, String file) {
		val today = LocalDateTime.now();
		for (int i = 2; i <= 10; i++) {
			val checkDateTime = today.minusDays(i);
			String dateTime = DateUtil.format(checkDateTime, "yyyyMMdd");
			if (file.contains(dateTime)) {
				try {
					channelSftp.rm(file);
					logger.info("删除服务器文件：{}", file);
				} catch (Exception e) {
					logger.error("删除服务器文件：" + file, e);
				}
				return;
			}
		}
	}

	private Pair<File, Integer> fileDataImport(String path, ChannelSftp channelSftp, String file, String typeName, String tableName,
			List<String> existFileNames)
			throws SftpException, IOException {
		List<Map<String, Object>> list = jdbcTemplate
				.queryForList("select id from mtnoh_aaa_task.tb_electric_file_import_info where fileName = ? limit 1", file);
		if (!list.isEmpty()) {
			existFileNames.add(file);
			return null;
		}

		String currentTableName = String.format("%s_temp_import", tableName);
		String sql = String.format("DROP TABLE IF EXISTS %s;CREATE TABLE %s LIKE %s;", currentTableName, currentTableName, tableName);
		jdbcTemplate.execute(sql);

		List<String> timeNames = typeTimeNames.get(typeName);
		List<String> fieldNameList = typeFieldNames.get(typeName);
		Set<String> ids = new HashSet<>(BATCH_COUNT);
		Set<String> repeatIds = new HashSet<>(BATCH_COUNT);
		List<Object[]> batchArgs = new ArrayList<>(BATCH_COUNT);
		Map<String, Integer> idIndex = new HashMap<>(BATCH_COUNT);
		String[] item = new String[fieldNameList.size()];
		Arrays.fill(item, "?");
		String deleteSql = String.format("delete from %s where id in (id_values) ", currentTableName);
		sql = String.format("insert into %s(%s) values(%s) as i ", currentTableName, String.join(",", fieldNameList),
				String.join(",", item));
		String property = System.getProperty("java.io.tmpdir");
		String saveFileValue = String.format("%s%s%s", property, File.separator, file);
		File saveFile = getFile(path, channelSftp, file, saveFileValue);
		boolean isIdTable = idTables.contains(typeName);
		int totalRow = 0;
		try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
			String lineTxt;
			while ((lineTxt = reader.readLine()) != null) {
				String[] values = lineTxt.split("\\|", -1);
				if (values.length == fieldNameList.size()) {
					final Pair<Object, Object[]> resetValuesPair = resetValues(fieldNameList, timeNames, values);
					batchArgs.add(resetValuesPair.getValue());
					if (isIdTable) {
						String id = (String) resetValuesPair.getKey();
						boolean insertIndex = true;
						if (StringUtils.isNotEmpty(id)) {
							if (ids.contains(id)) {
								final Integer currentIndex = idIndex.get(id);// 这批次数据中有重复的id
								if (currentIndex != null) {
									batchArgs.remove(batchArgs.size() - 1);// 删除刚加入的数据
									batchArgs.remove(currentIndex.intValue());// 移除之前的数据
									batchArgs.add(currentIndex.intValue(), resetValuesPair.getValue());// 加新数据加入到之前的位置
									insertIndex = false;
								}
								repeatIds.add(id);
							} else {
								ids.add(id);
							}
						}
						if (insertIndex) {
							idIndex.put(id + "", batchArgs.size() - 1);
						}
					}

					importValues(batchArgs, deleteSql, sql, repeatIds, true);

					if (batchArgs.isEmpty()) {
						totalRow += BATCH_COUNT;
					}
				}
			}
		}

		totalRow += batchArgs.size();
		importValues(batchArgs, deleteSql, sql, repeatIds, false);

		if (typeTableHisNames.containsKey(typeName)) {
			String hisTableName = String.format("%s_his", tableName);
			sql = String.format("INSERT INTO %s SELECT * FROM %s;", hisTableName, tableName);
			jdbcTemplate.execute(sql);
		}

		if (typeRenames.contains(typeName)) {
			String oldTableName = String.format("%s_old", tableName);
			sql = String.format("DROP TABLE IF EXISTS %s;", oldTableName);
			jdbcTemplate.execute(sql);
			sql = String.format("ALTER TABLE %s RENAME TO %s;", tableName, oldTableName);
			jdbcTemplate.execute(sql);
			sql = String.format("ALTER TABLE %s RENAME TO %s;", currentTableName, tableName);
			jdbcTemplate.execute(sql);
		}

		if (typeDeleteImports.contains(typeName)) {
			if (updateImportTimeTables.contains(typeName)) {
				sql = String.format("UPDATE %s t1 JOIN %s t2 ON t1.id = t2.id SET t1.tbDataImportDateTime = t2.tbDataImportDateTime;",
						currentTableName,
						tableName);
				jdbcTemplate.execute(sql);
			}

			sql = String.format("DELETE t2 FROM %s t1 JOIN %s t2 ON t1.id = t2.id;", currentTableName,
					tableName);
			jdbcTemplate.execute(sql);
			sql = String.format("INSERT INTO %s SELECT * FROM %s;", tableName, currentTableName);
			jdbcTemplate.execute(sql);
		}

		// 删除临时表
		sql = String.format("DROP TABLE IF EXISTS %s;", currentTableName);
		jdbcTemplate.execute(sql);

		jdbcTemplate.update("insert into mtnoh_aaa_task.tb_electric_file_import_info(fileName,importDateTime) values(?,?)", file, new Date());
		return new Pair<>(saveFile, totalRow);
	}

	private synchronized File getFile(String path, ChannelSftp channelSftp, String file, String saveFileValue) throws SftpException, IOException {
		channelSftp.cd(path);
		File saveFile = new File(saveFileValue);
		logger.info("下载file:{}", file);
		try (FileOutputStream outputStream = new FileOutputStream(saveFile)) {
			channelSftp.get(file, outputStream);
		}
		return saveFile;
	}

	private void importValues(List<Object[]> batchArgs, String deleteSql, String sql, Set<String> repeatIds, boolean checkCount) {
		if ((checkCount && Objects.equals(BATCH_COUNT, batchArgs.size()))
				|| (Boolean.FALSE.equals(checkCount) && Boolean.FALSE.equals(batchArgs.isEmpty()))) {
			if (!repeatIds.isEmpty()) {
				try {
					String[] item = new String[repeatIds.size()];
					Arrays.fill(item, "?");
					deleteSql = deleteSql.replace("id_values", String.join(",", item));
					jdbcTemplate.update(deleteSql, repeatIds.toArray());
				} catch (Exception e) {
					logger.error("油调文件数据-删除id重复的数据异常", e);
				}
				repeatIds.clear();
			}
			try {
				jdbcTemplate.batchUpdate(sql, batchArgs);
			} catch (Exception e) {
				logger.error("油调文件数据-批量入库异常", e);
				logger.info("油调文件数据-批量入库异常,尝试一个一个入库开始");
				if (sql.contains("ywgl_res.tb_oil_module")) {
					sql += " ON DUPLICATE KEY UPDATE id = i.id ";
				}
				for (Object[] batchArg : batchArgs) {
					try {
						jdbcTemplate.update(sql, batchArg);
					} catch (Exception ex) {
						logger.info("{}:{}", sql, Arrays.toString(batchArg));
						logger.error("油调文件数据-批量入库异常,尝试一个一个入库异常", e);
					}
				}
				logger.info("油调文件数据-批量入库异常,尝试一个一个入库结束");
			} finally {
				batchArgs.clear();
			}
		}
	}

	private Pair<Object, Object[]> resetValues(List<String> fieldNameList, List<String> timeNames, String[] values) {
		Object[] resultValues = new Object[values.length];
		Object id = null;

		for (int i = 0; i < values.length; i++) {
			String value = values[i];
			if (StringUtils.isEmpty(value)) {
				resultValues[i] = null;
			} else {
				String fieldName = fieldNameList.get(i);
				if (timeNames.contains(fieldName)) {
					resultValues[i] = parseLong(values[i]);
				} else {
					resultValues[i] = values[i];
				}
				if ("id".equals(fieldName)) {
					id = resultValues[i];
				}
			}
		}
		return new Pair<>(id, resultValues);
	}

	private Date parseLong(String value) {
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		try {
			if (value.length() == 8) {
				return DateUtil.parse(value, DatePattern.PURE_DATE_PATTERN);
			}
		} catch (Exception e) {
			//
		}

		try {
			Long longValue = Long.valueOf(value);
			return new Date(longValue);
		} catch (Exception e) {
			//
		}

		try {
			if (value.length() == 8) {
				return DateUtil.parse(value, DatePattern.PURE_DATE_PATTERN);
			}
			if (value.length() == 10) {
				return DateUtil.parse(value, DatePattern.NORM_DATE_PATTERN);
			}
			return DateUtil.parse(value, DatePattern.NORM_DATETIME_PATTERN);
		} catch (Exception e) {
			return null;
		}
	}

	private Map<String, List<String>> typeFieldNames = new HashMap<>();
	private Map<String, List<String>> typeTimeNames = new HashMap<>();
	private Map<String, String> typeTableNames = new HashMap<>();
	private Map<String, String> typeTableHisNames = new HashMap<>();
	// 增量：i_deviceBattery_、i_oilModule_
	// 全量：a_oilEngine_、a_siteEndurance_
	private List<String> typeRenames = Arrays.asList("a_oilEngine_", "a_siteEndurance_");
	private List<String> typeDeleteImports = Arrays.asList("i_oilModule_", "i_deviceBattery_");
	private List<String> idTables = Arrays.asList("i_oilModule_", "i_deviceBattery_");
	private List<String> updateImportTimeTables = Arrays.asList("i_oilModule_");

	private void initTypeFieldNames() {
		List<String> siteEnduranceList = Arrays.asList("city", "zone", "roomName", "siteType", "endurance");
		List<String> deviceBatteryList = Arrays.asList("id",
				"createTime",
				"dataTime",
				"res_code",
				"province_id",
				"city_id",
				"country_id",
				"related_site",
				"related_room",
				"device_type",
				"device_subclass",
				"zh_lable",
				"device_code",
				"product_name",
				"vendor_id",
				"related_power_device",
				"reted_capacity",
				"cell_voltage_level",
				"total_monomers_number",
				"start_time",
				"estimated_retirement_time",
				"lifecycle_status",
				"maintainor",
				"qualitor",
				"qr_code_no",
				"backup_time",
				"native_ems_id",
				"native_ems_name");
		List<String> oilEngineList = Arrays.asList("cityName",
				"areaName",
				"stationCode",
				"stationName",
				"code",
				"collectorCode",
				"sim",
				"moduleManufact",
				"ratedPower",
				"longitude",
				"latitude",
				"oilType",
				"tankCapacity",
				"diffType",
				"oilProperty",
				"oilState",
				"officeName",
				"status",
				"isStart",
				"signalflag",
				"cardFormat",
				"positionState",
				"elecType",
				"moduleStatus",
				"moduleStateUpdate",
				"oilElecKind",
				"createUserName",
				"createExportDate",
				"updateExportDate",
				"residueOil",
				"latestUsageTime",
				"maintenanceUnitOffice",
				"identifyStatus",
				"identifyPower");
		List<String> oilModuleList = Arrays.asList("id",
				"cityName",
				"areaName",
				"collectorcode",
				"localLac",
				"localCell",
				"serial",
				"signalflag",
				"elecType",
				"starttime",
				"endtime",
				"duration",
				"latitude",
				"longitude",
				"stationCode",
				"stationName",
				"blankLast",
				"uncityLast",
				"meterUsage",
				"oilUsage",
				"producer",
				"volA",
				"volB",
				"volC",
				"curtA",
				"curtB",
				"curtC",
				"signalStre",
				"elecFrequency",
				"usePower",
				"nousePower",
				"apparPower",
				"powerFactor",
				"number",
				"tempFiled1",
				"tempFiled2",
				"createDate");

		typeFieldNames.put("a_oilEngine_", oilEngineList);
		typeFieldNames.put("a_siteEndurance_", siteEnduranceList);
		typeFieldNames.put("i_deviceBattery_", deviceBatteryList);
		typeFieldNames.put("i_oilModule_", oilModuleList);

		typeTableNames.put("a_oilEngine_", "ywgl_res.tb_oil_engine");
		typeTableNames.put("a_siteEndurance_", "ywgl_res.tb_oil_site_endurance");
		typeTableNames.put("i_deviceBattery_", "ywgl_res.tb_oil_device_battery");
		typeTableNames.put("i_oilModule_", "ywgl_res.tb_oil_module");

		typeTableHisNames.put("a_oilEngine_", "ywgl_res.tb_oil_engine_his");

		typeTimeNames.put("a_oilEngine_", Arrays.asList("moduleStateUpdate", "createExportDate", "updateExportDate", "latestUsageTime"));
		typeTimeNames.put("i_oilModule_", Arrays.asList("starttime", "endtime", "createDate"));
		typeTimeNames.put("i_deviceBattery_", Arrays.asList("createTime", "dataTime", "start_time", "estimated_retirement_time", "backup_time"));
		typeTimeNames.put("a_siteEndurance_", new ArrayList<>());

	}
}
