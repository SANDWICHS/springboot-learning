package cn.zwq.websocket.server;

import java.net.InetSocketAddress;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import cn.zwq.websocket.init.ProjectInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;

/**
 * @author zhangwenqia
 * @create 2022-10-16 15:13
 * @description 类描述
 */
@Service
public class NettyServer {
	static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	@Value("${webSocket.netty.port:8899}")
	int port;

	EventLoopGroup bossGroup;
	EventLoopGroup workerGroup;

	ProjectInitializer nettyInitializer;

	@Autowired
	public void setNettyInitializer(ProjectInitializer nettyInitializer) {
		this.nettyInitializer = nettyInitializer;
	}

	@PostConstruct
	public void start() {
		new Thread(() -> {
			bossGroup = new NioEventLoopGroup();
			workerGroup = new NioEventLoopGroup();

			try {
				ServerBootstrap bootstrap = new ServerBootstrap();
				// bossGroup辅助客户端的tcp连接请求，workGroup负责与客户端之前的读写操作
				bootstrap.group(bossGroup, workerGroup);
				// 设置NIO类型的channel
				bootstrap.channel(NioServerSocketChannel.class);
				// 设置管道
				bootstrap.childHandler(nettyInitializer);

				// 配置完成，开始绑定server,通过调用sync同步方法阻塞直到绑定成功
				ChannelFuture channelFuture = bootstrap.bind(port).sync();
				logger.info("WebServer started and listen on :{}", channelFuture.channel());
				// 对关闭通道进行监听
				channelFuture.channel().closeFuture().sync();
			} catch (InterruptedException e) {
				logger.error("", e);
			} finally {
				// bossGroup.shutdownGracefully();
				// workerGroup.shutdownGracefully();
			}
		}).start();
	}

	/*
	 * 释放资源
	 */
	@PreDestroy
	public void destroy() throws InterruptedException {
		if (bossGroup != null) {
			bossGroup.shutdownGracefully().sync();
		}
		if (workerGroup != null) {
			workerGroup.shutdownGracefully().sync();
		}
	}

}
