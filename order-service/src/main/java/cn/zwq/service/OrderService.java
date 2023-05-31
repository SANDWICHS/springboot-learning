package cn.zwq.service;

import cn.zwq.entities.CommonResults;
import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangwenqia
 * @create 2023-05-17 14:58
 * @description 类描述
 */
public interface OrderService {
	CommonResults order(Integer id);

	CommonResults orderList(Integer userId, Integer index, Integer eachCount);

	CommonResults insert(JSONObject params);

	CommonResults update(JSONObject params);

	CommonResults delete(JSONObject params);

}
