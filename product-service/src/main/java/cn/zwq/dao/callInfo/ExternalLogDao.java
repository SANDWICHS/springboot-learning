package cn.zwq.dao.callInfo;

import java.util.List;

import cn.zwq.dto.callInfo.BasicInfoBo;
import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangwenqia
 * @create 2022-03-08 17:49
 * @description 投诉流程调用外系统接口日志操作类
 */
public interface ExternalLogDao {
	long addLog(String mqKey, String taskSn, String type, String method, JSONObject content);

	void updateLog(Long id, String url, String result, Boolean success);

	void createTable();

	void moveFinishLog();

	BasicInfoBo getExternalInfo(Long id);

	List<BasicInfoBo> queryExternalInfos(String type);

	List<BasicInfoBo> queryExternalInfos(String type, long maxId);

	List<BasicInfoBo> queryExternalInfos();
}
