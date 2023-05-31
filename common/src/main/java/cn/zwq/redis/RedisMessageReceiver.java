package cn.zwq.redis;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import cn.zwq.config.MachineInfo;

@Component("redisMessageReceiver")
public class RedisMessageReceiver {
	private static Logger log = org.slf4j.LoggerFactory.getLogger(RedisMessageReceiver.class);

	@Resource
	private MachineInfo machineInfo;

	private boolean self(String msg) {
		if (msg == null)
			return false;
		return msg.equals(machineInfo.getInfo());
	}

	/**
	 * 收到版本修改的消息
	 *
	 * @param message
	 */
	public void workflowChanged(String message) {
		if (message == null) {
			return;
		}
		String[] items = message.split("-");
		String machineInfo = items[0];
		String operatorType = items[1];
		String resourceType = items[2];
		int resourceId = Integer.parseInt(items[3]);

		if (self(machineInfo)) {
			return;
		}

		switch (resourceType) {
		case "version":
			log.info("{},{}", resourceType, operatorType);
			break;
		case "workFlow":
			log.info("{},{}", resourceType, operatorType);
			break;
		}
	}
}
