package cn.zwq.service;

import cn.zwq.entities.CommonResults;

import java.util.List;

/**
 * @author zhangwenqia
 * @create 2023-03-10 16:38
 * @description 类描述
 */
public interface ResourceService {
	CommonResults queryResource(String tableName, Integer current, Double longitude, Double latitude);

	CommonResults loadResource(String tableName);

	CommonResults deleteResource(String tableName, List<String> resources);

	CommonResults getResourcePos(String tableName, String resource1, String resource2);

	CommonResults getTwoResourceDistance(String tableName, String resource1, String resource2);

	CommonResults getPointRadius(String tableName, double distance, double longitude, double latitude);

	CommonResults getMemberRadius(String tableName, double distance, String member);

	CommonResults getResourceGeoHash(String tableName, List<String> resources);

	CommonResults property(String city, double longitude, double latitude);
}
