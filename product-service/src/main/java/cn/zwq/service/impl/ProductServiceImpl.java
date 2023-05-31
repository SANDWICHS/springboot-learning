package cn.zwq.service.impl;

import cn.zwq.event.MsgEvent;
import cn.zwq.event.ProductEvent;
import cn.zwq.service.ProductService;
import cn.zwq.service.SqlService;
import cn.zwq.util.SpringBeanUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Random;

/**
 * @author zhangwenqia
 * @create 2022-07-18 9:39
 * @description 类描述
 */
@Service
public class ProductServiceImpl implements ProductService {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(ProductServiceImpl.class);

	@Resource
	SqlService sqlService;

	@Override
	public boolean checkOrder(Integer orderId) throws InterruptedException {
		logger.info("开始检验订单-id:{}", orderId);
		StopWatch watch = new StopWatch();
		watch.start("checkOrderWatcher");

		ProductEvent productEvent = new ProductEvent(this, String.valueOf(orderId));
		// 同步处理
		SpringBeanUtils.publishEvent(productEvent);
		MsgEvent msgEvent = new MsgEvent(String.valueOf(orderId));
		// 异步处理
		SpringBeanUtils.publishEvent(msgEvent);
		watch.stop();

		watch.start("checkOrderWatcher");
		Thread.sleep(1000L);
		orderId = 1 / orderId;
		watch.stop();

		logger.info("结束校验订单-id:{}，耗时:{}", orderId, watch.prettyPrint());
		return true;
	}

	@Override
	public JSONObject detail(Integer id) {
		JSONObject productJson = new JSONObject();
		productJson.put("id", id);
		productJson.put("name", "商品");
		productJson.put("description", "商品描述");
		productJson.put("price", new Random().nextInt(id));
		return productJson;
	}

	@Override
	public void saveLog() {
		try {
			sqlService.update("insert xxyy(id,name,description)", 1, "name", "desc");
		} catch (Exception e) {
			logger.error("日志入库失败", e);
		}
	}
}
