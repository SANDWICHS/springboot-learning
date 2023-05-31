package cn.zwq.api;

import cn.zwq.entities.CommonResults;
import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhangwenqia
 * @create 2022-07-06 15:12
 * @description 类描述
 */
@FeignClient("product-service")
public interface ProductApi {
	@ResponseBody
	@GetMapping("/product/{id}")
	CommonResults product(@PathVariable("id") Integer id);

	@ResponseBody
	@GetMapping("/product/list")
	CommonResults productList(@RequestParam(value = "ids") List<Integer> ids);

	@ResponseBody
	@GetMapping("/product/info")
	CommonResults info(@RequestParam("id") Integer id);

	@PostMapping("/product/call")
	CommonResults call(@RequestBody JSONObject params);
}
