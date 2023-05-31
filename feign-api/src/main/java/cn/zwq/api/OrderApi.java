package cn.zwq.api;

import cn.zwq.entities.CommonResults;
import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhangwenqia
 * @create 2023-05-17 14:19
 * @description 类描述
 */
public interface OrderApi {
	@ResponseBody
	@GetMapping("/order/{id}")
	CommonResults order(@PathVariable("id") Integer id);

	@ResponseBody
	@GetMapping("/order/list")
	CommonResults orderList(@RequestParam("userId") Integer userId, @RequestParam("index") Integer index,
			@RequestParam("eachCount") Integer eachCount)
			throws InterruptedException;

	@PostMapping("/order/insert")
	CommonResults insert(@RequestBody JSONObject params) throws InterruptedException;

	@PostMapping("/order/update")
	CommonResults update(@RequestBody JSONObject params) throws InterruptedException;

	@PostMapping("/order/delete")
	CommonResults delete(@RequestBody JSONObject params) throws InterruptedException;
}
