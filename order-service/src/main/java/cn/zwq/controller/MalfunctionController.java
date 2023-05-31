package cn.zwq.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

import cn.zwq.api.OrderApi;
import cn.zwq.api.ProductApi;
import cn.zwq.entities.CommonResults;
import cn.zwq.service.OrderService;

/**
 * @author zhangwenqia
 * @create 2022-04-27 14:58
 * @description 类描述
 */
@RestController
public class MalfunctionController {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(MalfunctionController.class);

	@Resource
	RestTemplate clusterRestTemplate;
	@Resource
	RestTemplate singleRestTemplate;

	@RequestMapping("/order/pushToZhengqi")
	public CommonResults pushToZhengqi(@RequestParam("id") Integer id) {
		logger.info("info-pushToZhengqi-id:{}", id);
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
			try {
				String url = String.format("http://127.0.0.1:6100/zwq/product/pushToZhengqi");
//				url = String.format("http://product-service/zwq/product/pushToZhengqi");
				if (url.contains("product-service")) {
					stringResponseEntity = clusterRestTemplate.postForEntity(url, formEntity,
							String.class);
				} else {
					stringResponseEntity = singleRestTemplate.postForEntity(url, formEntity,
							String.class);
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

	@RequestMapping("/order/syncSheetState")
	public CommonResults syncSheetState(@RequestParam("id") Integer id) {
		logger.info("info-syncSheetState-id:{}", id);
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
			try {
				String url = String.format("http://127.0.0.1:6100/zwq/product/syncSheetState");
				url = String.format("http://product-service/product/syncSheetState");

				if (url.contains("product-service")) {
					stringResponseEntity = clusterRestTemplate.postForEntity(url, formEntity,
							String.class);
				} else {
					stringResponseEntity = singleRestTemplate.postForEntity(url, formEntity,
							String.class);
				}
			} catch (Exception e) {
				logger.error("[集中故障系统推送信息]失败", e);
				throw e;
			}

			logger.info("code:{},message:{}", stringResponseEntity.getStatusCode().value(), stringResponseEntity.getBody());
			return new CommonResults(200, "success", stringResponseEntity.getBody());
		} catch (Exception e) {
			logger.error("调用异常", e);
		}
		return new CommonResults(-1, "fail", id);
	}

}
