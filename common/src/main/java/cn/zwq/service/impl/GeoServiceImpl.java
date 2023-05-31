package cn.zwq.service.impl;

import cn.zwq.entities.ResourceInfo;
import cn.zwq.service.IGeoService;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metric;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhangwenqia
 * @create 2023-03-16 16:53
 * @description 类描述
 */
@Service
public class GeoServiceImpl implements IGeoService {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(GeoServiceImpl.class);

	private final String GEO_KEY = "Resource-Geo";

	/** redis 客户端 */
	private final StringRedisTemplate redisTemplate;

	@Autowired
	public GeoServiceImpl(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	private String getRedisKey(String redisKey) {
		if (StringUtils.isEmpty(redisKey)) {
			return GEO_KEY;
		}
		return redisKey;
	}

	@Override
	public Long saveResourceInfoToRedis(String redisKey, Collection<ResourceInfo> resourceInfos) {
		logger.info("start to save resource info: {}.", JSON.toJSONString(resourceInfos));

		GeoOperations<String, String> ops = redisTemplate.opsForGeo();

		Set<RedisGeoCommands.GeoLocation<String>> locations = new HashSet<>();
		resourceInfos.forEach(ci -> locations.add(new RedisGeoCommands.GeoLocation<String>(
				ci.getKey(), new Point(ci.getLongitude(), ci.getLatitude()))));

		logger.info("done to save resource info.");

		return ops.add(getRedisKey(redisKey), locations);
	}

	@Override
	public Long deleteResourceInfoFromRedis(String redisKey, Collection<String> resources) {
		logger.info("start to delete resource info: {}.", JSON.toJSONString(resources));

		Long remove = redisTemplate.boundZSetOps(redisKey).remove(resources.toArray());

		logger.info("done to delete resource info.");

		return remove;
	}

	@Override
	public List<Point> getResourcePos(String redisKey, String[] resources) {
		GeoOperations<String, String> ops = redisTemplate.opsForGeo();

		return ops.position(getRedisKey(redisKey), resources);
	}

	@Override
	public Distance getTwoResourceDistance(String redisKey, String resource1, String resource2, Metric metric) {
		GeoOperations<String, String> ops = redisTemplate.opsForGeo();

		return metric == null ? ops.distance(getRedisKey(redisKey), resource1, resource2)
				: ops.distance(getRedisKey(redisKey), resource1, resource2, metric);
	}

	@Override
	public GeoResults<RedisGeoCommands.GeoLocation<String>> getPointRadius(String redisKey, Circle within,
			RedisGeoCommands.GeoRadiusCommandArgs args) {
		GeoOperations<String, String> ops = redisTemplate.opsForGeo();

		return args == null ? ops.radius(getRedisKey(redisKey), within) : ops.radius(getRedisKey(redisKey), within, args);
	}

	@Override
	public GeoResults<RedisGeoCommands.GeoLocation<String>> getMemberRadius(String redisKey, String member, Distance distance,
			RedisGeoCommands.GeoRadiusCommandArgs args) {
		GeoOperations<String, String> ops = redisTemplate.opsForGeo();

		return args == null ? ops.radius(getRedisKey(redisKey), member, distance) : ops.radius(getRedisKey(redisKey), member, distance, args);
	}

	@Override
	public List<String> getResourceGeoHash(String redisKey, String[] resources) {
		GeoOperations<String, String> ops = redisTemplate.opsForGeo();

		return ops.hash(getRedisKey(redisKey), resources);
	}

	@Override
	public Set<String> deleteKeys(String redisKey) {
		Set<String> keys = redisTemplate.keys(redisKey);
		redisTemplate.delete(keys);

		return keys;
	}
}
