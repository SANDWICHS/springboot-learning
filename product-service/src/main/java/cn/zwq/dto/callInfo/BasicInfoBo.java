package cn.zwq.dto.callInfo;

import java.util.Date;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangwenqia
 * @create 2022-05-10 15:17
 * @description 类描述
 */
public class BasicInfoBo {
	Long id;
	String mqKey;
	String taskSn;
	String type;
	String method;
	JSONObject content;
	String result;
	Boolean success;
	Date createTime;
	Date updateTime;
	Integer tryNum;

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setMqKey(String mqKey) {
		this.mqKey = mqKey;
	}

	public String getMqKey() {
		return mqKey;
	}

	public void setTaskSn(String taskSn) {
		this.taskSn = taskSn;
	}

	public String getTaskSn() {
		return taskSn;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	public JSONObject getContent() {
		return content;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setTryNum(Integer tryNum) {
		this.tryNum = tryNum;
	}

	public Integer getTryNum() {
		return tryNum;
	}
}
