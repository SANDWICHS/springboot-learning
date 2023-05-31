package cn.zwq.websocket.handler;

import cn.zwq.websocket.config.NettyConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;

/**
 * @author zhangwenqia
 * @create 2022-10-16 16:01
 * @description 类描述
 */
@Service
@ChannelHandler.Sharable
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

	static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

	/*
	 * 一旦连接，第一个被执行
	 */
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		logger.info("有新的WebSocket客户端连接:[{}]", ctx.channel().id().asLongText());
		NettyConfig.getChannelGroup().add(ctx.channel());
		// super.handlerAdded(ctx);
	}

	/*
	 * 读取数据
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
		logger.info("WebSocket服务器收到消息:{}", msg.text());

		// 获取用户ID，关联channel
		JSONObject jsonObject = JSONUtil.parseObj(msg.text());
		String uid = jsonObject.getStr("uid");
		NettyConfig.getChannelMap().put(uid, ctx.channel());

		// 将用户ID作为自定义属性加入到channel中,方便随时channel中获取用户ID
		AttributeKey<String> key = AttributeKey.valueOf("userId");
		ctx.channel().attr(key).setIfAbsent(uid);

		// 回复消息
		ctx.channel().writeAndFlush(new TextWebSocketFrame("WebSocket服务器收到消息啦"));
	}

	/*
	 * 用户下线
	 */
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// super.handlerRemoved(ctx);
		logger.info("WebSocket用户下线了:{}", ctx.channel().id().asLongText());
		// 删除通道
		NettyConfig.getChannelGroup().remove(ctx.channel());
		removeUserId(ctx);
	}

	/*
	 * 异常处理
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// super.exceptionCaught(ctx, cause);
		logger.error("异常", cause);
		// 删除通道
		NettyConfig.getChannelGroup().remove(ctx.channel());
		removeUserId(ctx);
		ctx.close();
	}

	/*
	 * 删除用户与channel的对应关系
	 */
	private void removeUserId(ChannelHandlerContext ctx) {
		AttributeKey<String> key = AttributeKey.valueOf("userId");

		String userId = ctx.channel().attr(key).get();
		if (StringUtils.isNotEmpty(userId)) {
			NettyConfig.getChannelMap().remove(userId);
		}
	}
}
