package cn.zwq.websocket.service;

/**
 * @author zhangwenqia
 * @create 2022-10-16 16:20
 * @description 类描述
 */
public interface PushMsgService {

	/*
	 * 推送给指定用户
	 */
	void pushMsgToOne(String userId, String msg);

	/*
	 * 推送给所有用户
	 */
	void pushMsgToAll(String msg);
}
