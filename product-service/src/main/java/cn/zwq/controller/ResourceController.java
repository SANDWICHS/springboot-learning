package cn.zwq.controller;

import cn.zwq.entities.CommonResults;
import cn.zwq.service.ResourceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhangwenqia
 * @create 2023-03-10 16:33
 * @description 类描述
 */
@RestController
@RequestMapping("/resource")
public class ResourceController {
	@Resource
	ResourceService resourceService;

	@GetMapping("/query/type")
	public CommonResults queryShopByType(@RequestParam("type") String type,
			@RequestParam(value = "current", defaultValue = "1") Integer current,
			@RequestParam(value = "x", required = false) Double x,
			@RequestParam(value = "y", required = false) Double y) {

		return resourceService.queryResource(type, current, x, y);
	}

	@GetMapping("/load/type")
	public CommonResults loadResource(@RequestParam("type") String type) {
		return resourceService.loadResource(type);
	}

	@GetMapping("/delete/type")
	public CommonResults deleteResource(@RequestParam("type") String type, @RequestParam(value = "resources") List<String> resources) {
		return resourceService.deleteResource(type, resources);
	}

	@GetMapping("/query/resourcePos")
	public CommonResults getResourcePos(@RequestParam("type") String type,
			@RequestParam(value = "resource1") String resource1,
			@RequestParam(value = "resource2") String resource2) {

		return resourceService.getResourcePos(type, resource1, resource2);
	}

	@GetMapping("/query/resourceDistance")
	public CommonResults getTwoResourceDistance(@RequestParam("type") String type,
			@RequestParam(value = "resource1") String resource1,
			@RequestParam(value = "resource2") String resource2) {

		return resourceService.getTwoResourceDistance(type, resource1, resource2);
	}

	@GetMapping("/query/pointRadius")
	public CommonResults getPointRadius(@RequestParam("type") String type,
			@RequestParam(value = "distance") Double distance,
			@RequestParam(value = "longitude") Double longitude,
			@RequestParam(value = "latitude") Double latitude) {

		return resourceService.getPointRadius(type, distance, longitude, latitude);
	}

	@GetMapping("/query/memberRadius")
	public CommonResults getMemberRadius(@RequestParam("type") String type,
			@RequestParam(value = "distance") Double distance,
			@RequestParam(value = "member") String member) {

		return resourceService.getMemberRadius(type, distance, member);
	}

	@GetMapping("/query/resourceGeoHash")
	public CommonResults getResourceGeoHash(@RequestParam("type") String type,
			@RequestParam(value = "resources") List<String> resources) {

		return resourceService.getResourceGeoHash(type, resources);
	}

	@GetMapping("/query/property")
	public CommonResults getProperty(@RequestParam("city") String city,
			@RequestParam(value = "longitude") Double longitude,
			@RequestParam(value = "latitude") Double latitude) {

		return resourceService.property(city, longitude, latitude);
	}
}
