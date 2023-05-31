package cn.zwq.service.impl;

import cn.zwq.bo.AreaType;
import cn.zwq.bo.CustomArea;
import cn.zwq.bo.Region;
import cn.zwq.constant.RedisConstants;
import cn.zwq.constant.SystemConstants;
import cn.zwq.entities.CommonResults;
import cn.zwq.entities.ResourceInfo;
import cn.zwq.service.IGeoService;
import cn.zwq.service.ResourceService;
import cn.zwq.util.AreaShapeUtil;
import cn.zwq.util.GisUtil;
import org.slf4j.Logger;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author zhangwenqia
 * @create 2023-03-10 16:39
 * @description 类描述
 */
@Service
public class ResourceServiceImpl implements ResourceService {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(ResourceServiceImpl.class);

	@Resource
	JdbcTemplate jdbcTemplate;

	@Resource
	IGeoService geoService;

	private String getSearchSql(String tableName) {
		String searchSql = String.format("SELECT * FROM ywgl_res.%s ", tableName);
		return searchSql;
	}

	@Override
	public CommonResults queryResource(String tableName, Integer current, Double x, Double y) {
		// 2. 计算分页参数
		int form = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
		int end = current * SystemConstants.DEFAULT_PAGE_SIZE;

		String searchSql = getSearchSql(tableName);
		// 1. 判断是否需要坐标查询
		if (x == null || y == null) {
			// 不需要坐标查询，按数据库查询
			String sql = searchSql + " limit ?,? ";
			List<Map<String, Object>> resources = jdbcTemplate.queryForList(sql, form, end);
			// 返回数据
			return new CommonResults(0, "success", resources);
		}
		// 3. 查询redis，按照距离排序、分页 结果：id、distance
		String key = RedisConstants.RESOURCE_GEO_KEY + tableName;
		Point center = new Point(x, y);
		Distance radius = new Distance(50, Metrics.KILOMETERS);
		Circle within = new Circle(center, radius);
		// order by 距离 limit 2, 同时返回距离中心点的距离
		RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(end)
				.sortAscending();
		GeoResults<RedisGeoCommands.GeoLocation<String>> pointRadius = geoService.getPointRadius(key, within, args);
		// 4. 解析id
		if (pointRadius == null) {
			return new CommonResults(0, "success", Collections.emptyList());
		}
		List<GeoResult<RedisGeoCommands.GeoLocation<String>>> content = pointRadius.getContent();
		// 4.1 截取from => end
		List<Long> ids = new ArrayList<>(content.size());
		Map<String, Distance> distanceMap = new HashMap<>(content.size());
		if (content.size() <= form) {
			return new CommonResults(0, "success", Collections.emptyList());
		}
		content.stream().skip(form).forEach(result -> {
			// 4.2 获取资源id
			String id = result.getContent().getName();
			ids.add(Long.valueOf(id));
			// 4.2 获取距离
			Distance distance = result.getDistance();
			distanceMap.put(id, distance);
		});
		// 5. 根据id查询资源
		String sql = searchSql + String.format(" where id in (%s) ", String.join(",", Collections.nCopies(ids.size(), "?")));
		List<Map<String, Object>> resources = jdbcTemplate.queryForList(sql, ids.toArray());
		// 循环将 资源距离放入对象距离属性中
		resources.stream().forEach(value -> value.put("distance", distanceMap.get(value.get("id").toString()).getValue()));

		// 6. 返回结果
		return new CommonResults(0, "success", resources);
	}

	@Override
	public CommonResults loadResource(String tableName) {
		String searchSql = getSearchSql(tableName);
		String sql = searchSql + " limit 1 ";
		List<Map<String, Object>> resources = jdbcTemplate.queryForList(sql);
		if (resources.isEmpty()) {
			return new CommonResults(0, "success", "资源数据为空");
		}
		if (!resources.get(0).containsKey("id") || !resources.get(0).containsKey("longitude") || !resources.get(0).containsKey("latitude")) {
			return new CommonResults(0, "success", "数据不合法，缺失字段：id、longitude、latitude");
		}

		String key = RedisConstants.RESOURCE_GEO_KEY + tableName;
		sql = searchSql + " where id > ? order by id limit 100 ";
		int id = 0;
		int total = 0;
		int validResourceNum = 0;
		resources = jdbcTemplate.queryForList(sql, id);
		while (!resources.isEmpty()) {
			List<ResourceInfo> resourceInfos = new ArrayList<>(resources.size());
			for (Map<String, Object> item : resources) {
				id = (int) item.get("id");
				try {
					/*
					 * String longitudeValue = item.get("longitude").toString(); String
					 * latitudeValue = item.get("latitude").toString(); String[] longitudeSplit =
					 * longitudeValue.split("\\."); String[] latitudeValueSplit =
					 * latitudeValue.split("\\."); if (longitudeSplit.length < 2 ||
					 * latitudeValueSplit.length < 2) { continue; } if (longitudeSplit[1].length() <
					 * 6 || latitudeValueSplit[1].length() < 6) { continue; }
					 */
					double longitude = Double.valueOf(item.get("longitude").toString());
					double latitude = Double.valueOf(item.get("latitude").toString());
					if (longitude > 180 || longitude < -180 || latitude > 90 || latitude < -90) {
						continue;
					}
					ResourceInfo resourceInfo = new ResourceInfo(String.valueOf(id), longitude, latitude);
					resourceInfos.add(resourceInfo);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			if (!resourceInfos.isEmpty()) {
				geoService.saveResourceInfoToRedis(key, resourceInfos);
			}
			total += resources.size();
			validResourceNum += resourceInfos.size();
			resources = jdbcTemplate.queryForList(sql, id);
		}
		return new CommonResults(0, "success", String.format("总资源数据:%d,合法数据为:%d", total, validResourceNum));
	}

	@Override
	public CommonResults deleteResource(String tableName, List<String> resources) {
		String key = RedisConstants.RESOURCE_GEO_KEY + tableName;
		Long aLong = geoService.deleteResourceInfoFromRedis(key, resources);

		return new CommonResults(0, "success", aLong);
	}

	@Override
	public CommonResults getResourcePos(String tableName, String resource1, String resource2) {
		String key = RedisConstants.RESOURCE_GEO_KEY + tableName;
		List<Point> resourcePos = geoService.getResourcePos(key,
				Arrays.asList(resource1, resource2).toArray(new String[2]));
		return new CommonResults(0, "success", resourcePos);
	}

	@Override
	public CommonResults getTwoResourceDistance(String tableName, String resource1, String resource2) {
		String key = RedisConstants.RESOURCE_GEO_KEY + tableName;
		Distance twoResourceDistance = geoService.getTwoResourceDistance(key,
				resource1, resource2, Metrics.KILOMETERS);
		return new CommonResults(0, "success", twoResourceDistance);
	}

	@Override
	public CommonResults getPointRadius(String tableName, double distance, double longitude, double latitude) {
		String key = RedisConstants.RESOURCE_GEO_KEY + tableName;
		Point center = new Point(longitude, latitude);
		Distance radius = new Distance(distance, Metrics.KILOMETERS);
		Circle within = new Circle(center, radius);

		// order by 距离 limit 20, 同时返回距离中心点的距离
		RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(20)
				.sortAscending();
		GeoResults<RedisGeoCommands.GeoLocation<String>> pointRadius = geoService.getPointRadius(key, within, args);

		return new CommonResults(0, "success", pointRadius);
	}

	@Override
	public CommonResults getMemberRadius(String tableName, double distance, String member) {
		String key = RedisConstants.RESOURCE_GEO_KEY + tableName;
		Distance radius = new Distance(distance, Metrics.KILOMETERS);

		// order by 距离 limit 20, 同时返回距离中心点的距离
		RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(20)
				.sortAscending();

		GeoResults<RedisGeoCommands.GeoLocation<String>> pointRadius = geoService.getMemberRadius(key, member, radius, args);

		return new CommonResults(0, "success", pointRadius);
	}

	@Override
	public CommonResults getResourceGeoHash(String tableName, List<String> resources) {
		String key = RedisConstants.RESOURCE_GEO_KEY + tableName;
		List<String> resourceGeoHash = geoService.getResourceGeoHash(key,
				resources.toArray(new String[resources.size()]));

		return new CommonResults(0, "success", resourceGeoHash);
	}

	@Override
	public CommonResults property(String city, double longitude, double latitude) {
		String key = "RESOURCE-GEO:物业点_" + city;
		String sql = "SELECT MAX(max_dist) distance FROM `ywgl_property_point`.`tb_new_wuyedian` WHERE city = ? LIMIT 1;";
		List<Map<String, Object>> distanceItems = jdbcTemplate.queryForList(sql, city);
		double distance;
		if (distanceItems.isEmpty()) {
			distance = 50;
		} else {
			distance = (double) distanceItems.get(0).get("distance");
			distance /= 1000;
		}

		Point center = new Point(longitude, latitude);
		Distance radius = new Distance(distance, Metrics.KILOMETERS);
		Circle within = new Circle(center, radius);

		// order by 距离 limit 20, 同时返回距离中心点的距离
		RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeDistance().limit(20)
				.sortAscending();
		GeoResults<RedisGeoCommands.GeoLocation<String>> pointRadius = geoService.getPointRadius(key, within, args);

		List<String> ids = new ArrayList<>(pointRadius.getContent().size());
		pointRadius.getContent().forEach(value -> ids.add(value.getContent().getName()));
		logger.info(pointRadius.toString());
		List<String> matchPoints = new ArrayList<>();
		if (!ids.isEmpty()) {
			String idItems = String.join(",", Collections.nCopies(ids.size(), "?"));
			String sqlProperty = String.format(
					"SELECT `OBJECTID`,`NAME`,`points` FROM `ywgl_property_point`.`tb_new_wuyedian` WHERE OBJECTID IN (%s) LIMIT %d;", idItems,
					ids.size());
			List<Map<String, Object>> propertyItems = jdbcTemplate.queryForList(sqlProperty, ids.toArray());
			propertyItems.forEach(value -> {
				final String points = (String) value.get("points");
				Region region = new Region();
				region.setPointRegex("\\|");
				region.setRegex(",");
				region.setShapeType(AreaType.POLYGON.getCode());
				region.setRegionCoors(points);
				int x = GisUtil.fromFloatCoordinateToInt(longitude);
				int y = GisUtil.fromFloatCoordinateToInt(latitude);
				if (AreaShapeUtil.getAreaShape(region).contains(x, y)) {
					final Object id = value.get("OBJECTID");
					final Object name = value.get("NAME");
					matchPoints.add(String.format("%s-%s", id, name));
				}

				StopWatch stopWatch = new StopWatch();
				stopWatch.start("AreaShapeUtil");

				stopWatch.stop();

				stopWatch.start("GisUtils");

				stopWatch.stop();

				System.out.println(stopWatch.prettyPrint());

			});

		}
		return new CommonResults(0, "success", matchPoints);
	}

	private void test1(String points, double longitude, double latitude) {
		Region region = new Region();
		region.setPointRegex("\\|");
		region.setRegex(",");
		region.setShapeType(AreaType.POLYGON.getCode());
		region.setRegionCoors(points);
		int x = GisUtil.fromFloatCoordinateToInt(longitude);
		int y = GisUtil.fromFloatCoordinateToInt(latitude);
		if (AreaShapeUtil.getAreaShape(region).contains(x, y)) {

		}
	}

}
