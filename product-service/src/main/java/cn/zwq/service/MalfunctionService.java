package cn.zwq.service;

import org.unidal.tuple.Pair;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangwenqia
 * @create 2022-05-17 10:03
 * @description 类描述
 */
public interface MalfunctionService {
	Pair<Boolean, String> syncSheetState(JSONObject paramObject);

	Pair<Boolean, String> pushToZhengqi(JSONObject paramObject);
}
