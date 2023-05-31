package cn.zwq.api;

import cn.zwq.entities.CommonResults;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhangwenqia
 * @create 2022-01-19 18:15
 */
public interface UserApi {
	@ResponseBody
	@GetMapping("/sentinel1")
	CommonResults getUser(@RequestParam("id") Long id);

	@ResponseBody
	@GetMapping("/sentinel2")
	CommonResults getUser2(@RequestParam("id") Long id);
}
