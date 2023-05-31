package cn.zwq.websocket.init;

import cn.zwq.websocket.handler.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author zhangwenqia
 * @create 2022-10-16 15:30
 * @description 类描述
 */
@Service
public class ProjectInitializer extends ChannelInitializer<SocketChannel> {
	static final String WEBSOCKET_PROTOCOL = "WebSocket";

	@Value("${webSocket.netty.path:/webSocket}")
	String webSocketPath;

	WebSocketHandler webSocketHandler;

	@Autowired
	public void setWebSocketHandler(WebSocketHandler webSocketHandler) {
		this.webSocketHandler = webSocketHandler;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// 设置管道
		ChannelPipeline pipeline = ch.pipeline();
		// 流水线管理通道中的处理程序（handler）,用来处理业务
		// webSocket协议本身是基于Http协议的，所以这边也要使用Http编解码器
		pipeline.addLast(new HttpServerCodec());// 添加一个http的编解码器
		// pipeline.addLast(new ObjectEncoder());
		// 以块的方式来写的处理器
		pipeline.addLast(new ChunkedWriteHandler());// 添加一个大数据流的支持
		pipeline.addLast(new HttpObjectAggregator(8192));// 添加一个聚合器，主要将HTTPMessage聚合成FullhttpRequest/Response
		// pipeline.addLast(new WebSocketServerProtocolHandler(webSocketPath,
		// WEBSOCKET_PROTOCOL, true, 65536 * 10));
		pipeline.addLast(new WebSocketServerProtocolHandler(webSocketPath, WEBSOCKET_PROTOCOL, true, 65536 * 10));
		// 自定义的handler,处理业务逻辑
		pipeline.addLast(webSocketHandler);
	}
}
