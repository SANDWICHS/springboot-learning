package cn.zwq.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cn.zwq.cat.util.CatServiceLogUtils;
import cn.zwq.cat.util.CatUtils;
import cn.zwq.exection.CommonException;
import cn.zwq.service.CallInfoService;
import cn.zwq.util.SpringBeanUtils;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dianping.cat.message.Transaction;

/**
 * @author zhangwenqia
 * @create 2022-03-08 15:10
 * @description 消息队列监听类 String messageDelayLevel = "1s 5s 10s 30s 1m 2m 3m 4m 5m
 *              6m 7m 8m 9m 10m 20m 30m 1h 2h"
 */
@Component
@RocketMQMessageListener(consumerGroup = "${product.consumerGroup:commonTask-zwq}", topic = "${product.topic:CommonTask-zwq}")
public class CommonTaskListener implements RocketMQListener<MessageExt> {
	Logger logger = org.slf4j.LoggerFactory.getLogger(CommonTaskListener.class);

	CallInfoService callInfoService;

	@Autowired
	public void setCallInfoService(CallInfoService callInfoService) {
		this.callInfoService = callInfoService;
	}

	@Override
	public void onMessage(MessageExt messageExt) {
		JSONObject params = JSON.parseObject(new String(messageExt.getBody()));
		String msgKey = params.getString("msgKey");
		String producerTag = params.getString("producerTag");
		String methodValue = params.getString("method");
		String content = params.getString("content");
		String name = producerTag + ":" + methodValue;
		Transaction t = CatUtils.newTransaction("MtmqRecvMessage", name);
		String traceId = "";
		try {
			traceId = CatServiceLogUtils.initTraceId();
			logger.info(msgKey);
			Object bean = SpringBeanUtils.getBean(producerTag);
			Method method = bean.getClass().getMethod(methodValue, JSONObject.class);
			method.invoke(bean, JSON.parseObject(content));
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("消息队列异常-Bean名不存在" + e.getMessage(), e);
			CatUtils.setStatus(t, e);
		} catch (NoSuchMethodException e) {
			logger.error("消息队列异常-方法名不存在" + e.getMessage(), e);
			CatUtils.setStatus(t, e);
		} catch (IllegalAccessException e) {
			logger.error("消息队列异常-调用参数不符" + e.getMessage(), e);
			CatUtils.setStatus(t, e);
		} catch (InvocationTargetException e) {
			logger.error("消息队列异常-执行方法出错" + e.getMessage(), e);
			CatUtils.setStatus(t, e);
			int times = messageExt.getReconsumeTimes();
			if (times < 3) {
				throw new CommonException("消息队列异常-执行方法出错", e);
			}
		} finally {
			CatUtils.closeTransaction(t);
			CatServiceLogUtils.clearTraceId(traceId);
		}
	}

}
