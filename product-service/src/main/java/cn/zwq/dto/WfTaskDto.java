package cn.zwq.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author zhangwenqia
 * @create 2023-03-20 18:13
 * @description 类描述
 */
@Data
public class WfTaskDto {
	private int id;
	private String name;
	private String description;
	private int currentStatus;
	private String createUser;
	private Date createTime;
	private String lastUser;
	private Date lastTime;
	private String pushUser;
	private Date pushTime;
	private Date currentTimeLimit;
	private Date currentTimeLimit2;
	private Date totalTimeLimit;
	private Date totalTimeLimit2;
	private Integer taskType;
	private Map<Integer, String> conditionFieldMap;
}
