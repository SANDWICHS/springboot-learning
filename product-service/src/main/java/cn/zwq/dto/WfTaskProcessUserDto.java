package cn.zwq.dto;

import lombok.Data;

import java.util.Date;

/**
 * @author zhangwenqia
 * @create 2023-03-20 18:14
 * @description 类描述
 */
@Data
public class WfTaskProcessUserDto {
	private int taskId;
	private int currentStatus;
	private int processUser;
	private Date timeOuted;
	private Date timeOuting;
}
