package cn.zwq.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author zhangwenqia
 * @create 2022-07-18 9:24
 * @description 类描述
 */
public class ProductEvent extends ApplicationEvent {
	private String orderId;

	/**
	 * Create a new {@code ApplicationEvent}.
	 *
	 * @param source the object on which the event initially occurred or with which
	 *               the event is associated (never {@code null})
	 */
	public ProductEvent(Object source, String orderId) {
		super(source);
		this.orderId = orderId;
	}

	public String getOrderId() {
		return orderId;
	}
}
