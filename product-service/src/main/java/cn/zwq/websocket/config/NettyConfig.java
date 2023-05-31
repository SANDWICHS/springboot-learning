package cn.zwq.websocket.config;

import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * @author zhangwenqia
 * @create 2022-10-16 15:17
 * @description 类描述
 */
public class NettyConfig {
	private NettyConfig() {
	}

	/*
	 * 定义全局单例channel组 管理所有channel
	 */
	private static ChannelGroup channelGroup = null;

	/*
	 * 存放请求ID与channel的对应关系
	 */
	private static ConcurrentHashMap<String, Channel> channelMap = null;

	public static synchronized ChannelGroup getChannelGroup() {
		if (null == channelGroup) {
			channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		}

		return channelGroup;
	}

	public static synchronized ConcurrentHashMap<String, Channel> getChannelMap() {
		if (null == channelMap) {
			channelMap = new ConcurrentHashMap<>();
		}
		return channelMap;
	}

	public static Channel getChannel(String userId) {
		if (null == channelMap) {
			return getChannelMap().get(userId);
		}
		return channelMap.get(userId);
	}
}
