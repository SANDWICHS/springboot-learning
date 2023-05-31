package cn.zwq.controller;

import cn.zwq.api.ProductApi;
import cn.zwq.client.OrderClient;
import cn.zwq.entities.CommonResults;
import cn.zwq.service.ProductService;
import cn.zwq.websocket.service.PushMsgService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author zhangwenqia
 * @create 2022-04-27 14:58
 * @description 类描述
 */
@RestController
public class ProductController implements ProductApi {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(ProductApi.class);

	private ProductService productService;
	private PushMsgService pushMsgService;

	@Resource
	OrderClient orderClient;

	@Resource
	RestTemplate clusterRestTemplate;

	@Resource
	RestTemplate singleRestTemplate;

	@Autowired
	public void setPushMsgService(PushMsgService pushMsgService) {
		this.pushMsgService = pushMsgService;
	}

	@Autowired
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	@Override
	public CommonResults product(Integer id) {
		logger.info("收到调用请求：{}", id);

		makeError(id);

		final JSONObject detail = productService.detail(id);

		logger.info("结束调用请求：{}", id);
		return new CommonResults(200, "success", JSON.toJSONString(detail));
	}

	@Override
	public CommonResults productList(List<Integer> ids) {
		logger.info("收到调用请求：{}", ids);

		JSONArray details = new JSONArray();
		ids.stream().forEach(id -> details.add(productService.detail(id)));

		logger.info("结束调用请求：{}", ids);
		return new CommonResults(200, "success", JSON.toJSONString(details));
	}

	@Override
	public CommonResults info(Integer id) {
		logger.info("收到调用请求：{}", id);
		// pushMsgService.pushMsgToAll("msg");
		// pushMsgService.pushMsgToOne(id.toString(), "msg");

		// productService.checkOrder(id);

		makeError(id);

		logger.info("结束调用请求：{}", id);
		return new CommonResults(200, "success", id);
	}

	private void makeError(Integer id) {
		productService.saveLog();

		try {
			Thread.sleep(RandomUtils.nextInt(id, 100));
		} catch (Exception e) {
			logger.error("", e);
		}

		productService.saveLog();

		try {
			Thread.sleep(RandomUtils.nextInt(id, 100));
		} catch (Exception e) {
			logger.error("", e);
		}

		productService.saveLog();

		try {
			Thread.sleep(RandomUtils.nextInt(id, 100));
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	@Override
	public CommonResults call(JSONObject params) {
		logger.info("收到调用请求：{}", JSON.toJSONString(params));
		final Integer id = params.getInteger("id");
		final CommonResults orderResult;
		if (id == 1) {
			orderResult = orderClient.order(id);
		} else if (id == 2) {
			String url = String.format("http://127.0.0.1:6100/zwq/order/%d?token=abcdd", id);
			final ResponseEntity<CommonResults> forEntity = singleRestTemplate.getForEntity(url, CommonResults.class);
			orderResult = forEntity.getBody();
		} else if (id == 3) {
			String url = String.format("http://order-service/order/%d?token=abcdd", id);
			final ResponseEntity<CommonResults> forEntity = clusterRestTemplate.getForEntity(url, CommonResults.class);
			orderResult = forEntity.getBody();
		} else if (id < 0) {
			String url = "http://127.0.0.1:6100/zwq/order/get?id=0&token=abcdd";
			if (id < -10) {
				url = "http://127.0.0.1:6100/zwq/order/get1?id=0&token=abcdd";
				final ResponseEntity<Void> forEntity = singleRestTemplate.getForEntity(url, Void.class);
				orderResult = null;
			} else {
				final ResponseEntity<CommonResults> forEntity = singleRestTemplate.getForEntity(url, CommonResults.class);
				orderResult = forEntity.getBody();
			}
		} else {
			String url = String.format("http://127.0.0.1:6100/zwq/order/%d?token=abcdd", id);
			final ResponseEntity<CommonResults> forEntity = singleRestTemplate.getForEntity(url, CommonResults.class);
			orderResult = forEntity.getBody();
		}

		String datas = orderResult == null ? "" : (String) orderResult.getDatas();
		JSONObject detail;
		try {
			detail = JSON.parseObject(datas);
		} catch (Exception e) {
			logger.error("解析异常", e);
			detail = new JSONObject();
			detail.put("error", datas);
		}

		logger.info("结束调用请求：{}", JSON.toJSONString(params));
		return new CommonResults(200, "success", JSON.toJSONString(detail));
	}
}
