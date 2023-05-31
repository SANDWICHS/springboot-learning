package cn.zwq.service;

import cn.zwq.entities.ResourceInfo;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author zhangwenqia
 * @create 2023-03-16 16:43
 * @description Geo服务接口定义
 */
public interface IGeoService {
	/**
	 * <h2>把资源信息保存到 Redis 中</h2>
	 *
	 * @param redisKey      存储的Key值
	 * @param resourceInfos {@link ResourceInfo}
	 * @return 成功保存的个数
	 */
	Long saveResourceInfoToRedis(String redisKey, Collection<ResourceInfo> resourceInfos);

	/**
	 * <h2>把资源信息删除</h2>
	 *
	 * @param redisKey  存储的Key值
	 * @param resources {@link Collection<String>}
	 * @return 成功保存的个数
	 */
	Long deleteResourceInfoFromRedis(String redisKey, Collection<String> resources);

	/**
	 * <h2>获取给定资源的坐标</h2>
	 * 
	 * @param redisKey  存储的Key值
	 * @param resources 给定资源 key
	 * @return {@link Point}s
	 */
	List<Point> getResourcePos(String redisKey, String[] resources);

	/**
	 * <h2>获取两个资源之间的距离</h2>
	 * 
	 * @param redisKey  存储的Key值
	 * @param resource1 第一个资源
	 * @param resource2 第二个资源
	 * @param metric    {@link Metric} 单位信息, 可以是 null
	 * @return {@link Distance}
	 */
	Distance getTwoResourceDistance(String redisKey, String resource1, String resource2, Metric metric);

	/**
	 * <h2>根据给定地理位置坐标获取指定范围内的地理位置集合</h2>
	 * 
	 * @param redisKey 存储的Key值
	 * @param within   {@link Circle} 中心点和距离
	 * @param args     {@link RedisGeoCommands.GeoRadiusCommandArgs} 限制返回的个数和排序方式,
	 *                 可以是 null
	 * @return {@link RedisGeoCommands.GeoLocation}
	 */
	GeoResults<RedisGeoCommands.GeoLocation<String>> getPointRadius(String redisKey,
			Circle within, RedisGeoCommands.GeoRadiusCommandArgs args);

	/**
	 * <h2>根据给定地理位置获取指定范围内的地理位置集合</h2>
	 * 
	 * @param redisKey 存储的Key值
	 */
	GeoResults<RedisGeoCommands.GeoLocation<String>> getMemberRadius(String redisKey,
			String member, Distance distance, RedisGeoCommands.GeoRadiusCommandArgs args);

	/**
	 * <h2>获取某个地理位置的 geohash 值</h2>
	 * 
	 * @param redisKey  存储的Key值
	 * @param resources 给定资源 key
	 * @return resource geohashs
	 */
	List<String> getResourceGeoHash(String redisKey, String[] resources);

	Set<String> deleteKeys(String redisKey);
}
