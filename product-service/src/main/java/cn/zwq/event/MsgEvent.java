package cn.zwq.event;

/**
 * @author zhangwenqia
 * @create 2022-07-18 11:21
 * @description 类描述
 */
public class MsgEvent {
	private String orderId;

	public MsgEvent(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderId() {
		return orderId;
	}
}
