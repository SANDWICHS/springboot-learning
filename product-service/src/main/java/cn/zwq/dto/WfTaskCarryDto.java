package cn.zwq.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author zhangwenqia
 * @create 2023-03-20 18:14
 * @description 类描述
 */
@Data
public class WfTaskCarryDto {
	private int taskId;
	private int carry;
	private int currentStatus;
	private String pushUser;
	private Date pushTime;
	private Date currentTimeLimit;
	private Date currentTimeLimit2;
	private Integer taskType;
}
