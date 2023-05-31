package cn.zwq.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zhangwenqia
 * @create 2022-10-17 10:42
 * @description 类描述
 */
@RestController
@RequestMapping("/websocket")
public class WebsocketController {
	/**
	 *
	 * @param id     用户主键
	 * @param idList 要把消息发送给其他用户的主键
	 */
	@RequestMapping("hello")
	private void hello(Long id, List<Long> idList) {
		// 获取所有连接的客户端,如果是集群环境使用redis的hash数据类型存储即可
		/*
		 * Map<String, Channel> channelMap = WebSocketHandler.getChannelMap(); //
		 * 获取与用户主键绑定的channel,如果是集群环境使用redis的hash数据类型存储即可 Map<String, Long> clientMap =
		 * WebSocketHandler.getClientMap(); // 解决问题六,websocket集群中一个客户端向其他客户端主动发送消息，如何实现？
		 * clientMap.forEach((k, v) -> { if (idList.contains(v) && !Objects.equals(v,
		 * id)) { Channel channel = channelMap.get(k); channel.eventLoop().execute(() ->
		 * channel .writeAndFlush(new
		 * TextWebSocketFrame(Thread.currentThread().getName() + "服务器时间" +
		 * LocalDateTime.now() + "wdy"))); } });
		 */
	}
}
