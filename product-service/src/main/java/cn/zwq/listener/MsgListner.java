package cn.zwq.listener;

import cn.zwq.event.MsgEvent;
import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import cn.zwq.event.ProductEvent;

/**
 * @author zhangwenqia
 * @create 2022-07-18 9:27
 * @description 类描述
 */
@Component
public class MsgListner {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(MsgListner.class);

	@EventListener(MsgEvent.class)
	public void sendMsg(MsgEvent event) {
		String orderId = event.getOrderId();
		StopWatch watch = new StopWatch();
		watch.start("productEventWatcher");

		long start = System.currentTimeMillis();
		try {
			Thread.sleep(4000);
		} catch (Exception e) {
			//
		}
		long end = System.currentTimeMillis();
		watch.stop();
		logger.info("{}:发生消息耗时：({})毫秒，{}", orderId, end - start, watch.prettyPrint());
	}
}
