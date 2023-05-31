package cn.zwq.service;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangwenqia
 * @create 2022-07-18 9:37
 * @description 类描述
 */
public interface ProductService {
	boolean checkOrder(Integer orderId) throws InterruptedException;

	JSONObject detail(Integer id);

	void saveLog();
}
