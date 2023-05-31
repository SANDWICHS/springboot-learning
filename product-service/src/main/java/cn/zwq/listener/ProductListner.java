package cn.zwq.listener;

import cn.zwq.event.ProductEvent;
import org.slf4j.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * @author zhangwenqia
 * @create 2022-07-18 9:27
 * @description 类描述
 */
@Component
public class ProductListner implements ApplicationListener<ProductEvent> {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(ProductListner.class);

	@Override
	public void onApplicationEvent(ProductEvent event) {
		String orderId = event.getOrderId();
		StopWatch watch = new StopWatch();
		watch.start("productEventWatcher");

		long start = System.currentTimeMillis();
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
			//
		}
		long end = System.currentTimeMillis();
		watch.stop();
		logger.info("{}:校验订单商品价格耗时：({})毫秒，{}", orderId, end - start, watch.prettyPrint());
	}
}
