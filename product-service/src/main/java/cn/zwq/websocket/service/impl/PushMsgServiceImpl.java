package cn.zwq.websocket.service.impl;

import java.util.Objects;

import cn.zwq.websocket.config.NettyConfig;
import cn.zwq.websocket.service.PushMsgService;
import org.springframework.stereotype.Service;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author zhangwenqia
 * @create 2022-10-16 16:22
 * @description 类描述
 */
@Service
public class PushMsgServiceImpl implements PushMsgService {

	@Override
	public void pushMsgToOne(String userId, String msg) {
		Channel channel = NettyConfig.getChannel(userId);
		if (Objects.isNull(channel)) {
			throw new RuntimeException("未连接WebSocket服务器");
		}

		channel.writeAndFlush(new TextWebSocketFrame(msg));
	}

	@Override
	public void pushMsgToAll(String msg) {
		NettyConfig.getChannelGroup().writeAndFlush(new TextWebSocketFrame(msg));
	}
}
