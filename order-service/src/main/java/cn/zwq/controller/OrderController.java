package cn.zwq.controller;

import cn.zwq.api.OrderApi;
import cn.zwq.api.ProductApi;
import cn.zwq.entities.CommonResults;
import cn.zwq.service.OrderService;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * @author zhangwenqia
 * @create 2022-04-27 14:58
 * @description 类描述
 */
@RestController
public class OrderController implements OrderApi {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(OrderController.class);
	@Resource
	OrderService orderService;

	@Resource
	RestTemplate clusterRestTemplate;

	@Resource
	RestTemplate singleRestTemplate;

	ProductApi productApi;

	@Resource
	public void setProductApi(ProductApi productApi) {
		this.productApi = productApi;
	}

	@RequestMapping("/order/test")
	public CommonResults test(@RequestParam("id") Integer id) {
		logger.info("info-test-id:{}", id);
		try {

			HttpHeaders headers = new HttpHeaders();
			MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
			headers.setContentType(type);
			headers.add("Accept", MediaType.APPLICATION_JSON.toString());

			JSONObject paramObject = new JSONObject();
			paramObject.put("id", id);
			paramObject.put("token", id);
			HttpEntity<JSONObject> formEntity = new HttpEntity<>(paramObject, headers);
			ResponseEntity<String> stringResponseEntity;
			try {/// product/call
				String url = String.format("http://127.0.0.1:6100/zwq/product/call");
				url = String.format("http://product-service/product/call");
				if (id.intValue() > 100) {
					url = String.format("http://127.0.0.1:6100/zwq/product/info?id=%d&token=33", id);
					logger.info("product-url:{}", url);
					stringResponseEntity = singleRestTemplate.getForEntity(url, String.class);
				} else {
					if (url.contains("product-service")) {
						stringResponseEntity = clusterRestTemplate.postForEntity(url, formEntity,
								String.class);
					} else {
						stringResponseEntity = singleRestTemplate.postForEntity(url, formEntity,
								String.class);
					}
				}
			} catch (Exception e) {
				logger.error("[运维中心推送故障工单及工单流转环节至政企运维系统]失败", e);
				throw e;
			}

			logger.info("code:{},message:{}", stringResponseEntity.getStatusCode().value(), stringResponseEntity.getBody());
			return new CommonResults(200, "success", stringResponseEntity.getBody());
		} catch (Exception e) {
			logger.error("调用异常", e);
		}
		return new CommonResults(-1, "fail", id);
	}

	@GetMapping("/order/get")
	public CommonResults orderGet(@RequestParam("id") Integer id) {
		logger.info("order-get:{}", id);

		int xy = 1 / id;

		return new CommonResults(200, "success", id);
	}

	@GetMapping("/order/get1")
	public void orderGet1(@RequestParam("id") Integer id) {
		logger.info("order-get1:{}", id);

		int xy = 1 / id;
	}

	@GetMapping("/order/flag")
	public CommonResults orderFlag(@RequestParam("flag") Integer flag) {
		logger.info("order-flag:{}", flag);
		try {
			CommonResults product = productApi.product(flag);
			logger.info("code:{},message:{},date:{}", product.getCode(), product.getMessage(), product.getDatas());
			String url = "http://127.0.0.1:6100/zwq/product/3?token=33";
			final ResponseEntity<CommonResults> forEntity = singleRestTemplate.getForEntity(url, CommonResults.class);
			if (forEntity.getStatusCode() == HttpStatus.OK) {
				product = forEntity.getBody();
			} else {
				product = productApi.product(flag);
			}
			logger.info("code:{},message:{},date:{}", product.getCode(), product.getMessage(), product.getDatas());

			return new CommonResults(200, "success", flag);
		} catch (Exception e) {
			logger.error("调用异常", e);
		}
		return new CommonResults(-1, "fail", flag);
	}

	@RequestMapping("/order/info")
	public CommonResults info(@RequestParam("id") Integer id) {
		logger.info("info-order-id:{}", id);
		try {
			CommonResults product;// = productApi.info(id);
			// logger.info("code:{},message:{},date:{}", product.getCode(),
			// product.getMessage(), product.getDatas());
			String url = String.format("http://127.0.0.1:6100/zwq/product/info?id=%d&token=33", id);
			logger.info("product-url:{}", url);
			final ResponseEntity<String> forEntity = singleRestTemplate.getForEntity(url, String.class);
			if (forEntity.getStatusCode() == HttpStatus.OK) {
				String productValue = forEntity.getBody();
				logger.info("info-order-result:{}", productValue);
				return new CommonResults(200, "success", id);
			} else {
				product = productApi.info(id);
			}
			logger.info("code:{},message:{},date:{}", product.getCode(), product.getMessage(), product.getDatas());
			return new CommonResults(200, "success", id);
		} catch (Exception e) {
			logger.error("调用异常", e);
		}
		return new CommonResults(-1, "fail", id);
	}

	@RequestMapping("/order/product")
	public CommonResults product(@RequestParam("id") Integer id) {
		logger.info("order-product-id:{}", id);
		JSONObject params = new JSONObject();
		params.put("id", id);
		params.put("token", "???");
		CommonResults product = productApi.call(params);

		// final ResponseEntity<CommonResults> commonResultsResponseEntity =
		// clusterRestTemplate.postForEntity("", params, CommonResults.class);

		logger.info("code:{},message:{},date:{}", product.getCode(), product.getMessage(), product.getDatas());
		return new CommonResults(0, "success", product.getDatas());
	}

	@Override
	public CommonResults order(Integer id) {
		return orderService.order(id);
	}

	@Override
	public CommonResults orderList(Integer userId, Integer index, Integer eachCount) {
		return orderService.orderList(userId, index, eachCount);
	}

	@Override
	public CommonResults insert(JSONObject params) {
		return orderService.insert(params);
	}

	@Override
	public CommonResults update(JSONObject params) {
		return orderService.update(params);
	}

	@Override
	public CommonResults delete(JSONObject params) {
		return orderService.delete(params);
	}
}
