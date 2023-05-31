package cn.zwq.service.impl;

import java.time.LocalDateTime;
import java.util.*;
import cn.zwq.http.WebServiceInvokerUtil;
import cn.zwq.service.MalfunctionService;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.unidal.tuple.Pair;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.core.date.DateUtil;

/**
 * @author zhangwenqia
 * @create 2022-05-17 10:05
 * @description 类描述
 */
@Service(value = "malfunctionService")
public class MalfunctionServiceImpl implements MalfunctionService {
	Logger logger = org.slf4j.LoggerFactory.getLogger(MalfunctionServiceImpl.class);

	@Override
	public Pair<Boolean, String> syncSheetState(JSONObject paramObject) {
		logger.info("开始调用集中故障系统推送工单状态信息:{}", paramObject.toString());
		/**
		 * serSupplier string(16) 服务提供方 命名形如：省市代码_系统名称 serCaller string(16) 服务调用方
		 * 命名形如：省市代码_系统名称 callerPwd string(32) 口令 / callTime string(20) 服务调用时间 /
		 * opDetail String 详细信息 详细信息，参见“详细信息约定”
		 */
		String serSupplier = "集中故障系统";
		String serCaller = "运维系统";
		String callerPwd = "口令";
		String callTime = DateUtil.formatLocalDateTime(LocalDateTime.now());
		String opDetail = paramObject.getString("opDetail");
		String url = "http://10.205.228.32:9000/SheetStateSync?wsdl";
		String location = null;
		if (url.length() - 5 > 0) {
			location = url.substring(0, url.length() - 5);
		}
		Pair<String, String> wsdlUrl = Pair.from(url, location);
		/*
		 * Object[] objects = WebServiceUtil.executeWebService(wsdlUrl,
		 * "syncSheetState", serSupplier, serCaller, callerPwd, callTime, opDetail);
		 * StringBuilder sb = new StringBuilder(); if (objects != null) { for (Object
		 * item : objects) { sb.append(item); } }
		 */
		String[] parameterValue = { serSupplier, serCaller, callerPwd, callTime, opDetail };

		Object resultValue = WebServiceInvokerUtil.invoke(wsdlUrl, 10000, "syncSheetState",
				parameterValue);
		logger.info("调用返回结果：{}", resultValue);
		return Pair.from(true, Optional.ofNullable(resultValue).orElse("").toString());
	}

	@Override
	public Pair<Boolean, String> pushToZhengqi(JSONObject paramObject) {
		logger.info("开始调用[运维中心推送故障工单及工单流转环节至政企运维系统], 状态信息:{}", paramObject.toString());
		String sheettype = paramObject.getString("sheettype");
		String opDetail = paramObject.getString("opDetail");
		String url = "http://10.205.194.53:33069/services/faultWorkService/faultWorkSyn?wsdl";
		String location = null;
		if (url.length() > 5) {
			location = url.substring(0, url.length() - 5);
		}
		Pair<String, String> wsdlUrl = Pair.from(url, location);
		// Object resultValue = WebServiceInvokerUtil.invoke(wsdlUrl, 10000,
		// "faultWorkSyn", sheettype, opDetail);
		Object resultValue = null;
		try {
			Thread.sleep(RandomUtils.nextInt(11, 1000) * 2);
		} catch (Exception e) {
			logger.error("", e);
		}
		try {
			Thread.sleep(RandomUtils.nextInt(22, 1000) * 2);
		} catch (Exception e) {
			logger.error("", e);
		}
		logger.info("调用返回结果：{}", resultValue);
		return Pair.from(true, Optional.ofNullable(resultValue).orElse("").toString());
	}

}
