package cn.zwq.controller;

import cn.zwq.service.MalfunctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unidal.tuple.Pair;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangwenqia
 * @create 2022-04-01 17:55
 * @description 类描述
 */
@RestController
@RequestMapping("/product")
public class MalfunctionController {
	Logger logger = LoggerFactory.getLogger(MalfunctionController.class);

	MalfunctionService malfunctionService;

	@Autowired
	public void setMalfunctionService(MalfunctionService malfunctionService) {
		this.malfunctionService = malfunctionService;
	}

	@PostMapping("/syncSheetState")
	public ResponseEntity<String> syncSheetState(@RequestBody JSONObject paramObject) {
		try {
			Pair<Boolean, String> result = malfunctionService.syncSheetState(paramObject);
			if (Boolean.FALSE.equals(result.getKey())) {
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(result.getValue());
			}
			return ResponseEntity.status(HttpStatus.OK).body(result.getValue());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("调用[集中故障工单状态同步接口]异常");
		}
	}

	@PostMapping("/pushToZhengqi")
	public ResponseEntity<String> pushToZhengqi(@RequestBody JSONObject paramObject) {
		try {
			Pair<Boolean, String> result = malfunctionService.pushToZhengqi(paramObject);
			if (Boolean.FALSE.equals(result.getKey())) {
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(result.getValue());
			}
			return ResponseEntity.status(HttpStatus.OK).body(result.getValue());
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body("调用[运维中心推送故障工单及工单流转环节至政企运维系统]异常");
		}
	}

}
