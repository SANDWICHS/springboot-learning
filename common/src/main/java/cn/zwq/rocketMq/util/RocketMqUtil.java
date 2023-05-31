package cn.zwq.rocketMq.util;

import cn.zwq.rocketMq.dto.RocketMqMessage;
import cn.zwq.util.SpringBeanUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * @author zhangwenqia
 * @create 2022-05-18 17:45
 * @description 类描述
 */
public class RocketMqUtil {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(RocketMqUtil.class);

	private RocketMqUtil() {
	}

	public static void sendCommonTaskMessage(String uuid, JSONObject jsonObject, String topic, String producerGroup, String type, String method) {
		RocketMQTemplate rocketMQTemplate = (RocketMQTemplate) SpringBeanUtils.getBean("rocketMQTemplate");

		RocketMqMessage<String> message = new RocketMqMessage<>();
		message.setMsgKey(uuid);
		message.setContent(JSON.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue, SerializerFeature.WriteNullStringAsEmpty,
				SerializerFeature.WriteDateUseDateFormat));
		message.setProducerTopic(topic);
		message.setProducerGroup(producerGroup);
		message.setProducerTag(type);
		message.setMethod(method);

		Message<RocketMqMessage<String>> messageFinal = MessageBuilder.withPayload(message).setHeader("KEYS", uuid).build();
		String destination = String.format("%s:%s", message.getProducerTopic(), message.getProducerTag());

		SendResult sendResult = rocketMQTemplate.syncSend(destination, messageFinal, 30000, 5);// 延时一分钟发送
		logger.info("成功发送消息，消息内容为：{}，返回值为：{}", message, sendResult);
	}
}
