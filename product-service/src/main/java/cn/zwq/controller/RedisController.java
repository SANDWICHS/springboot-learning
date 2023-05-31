package cn.zwq.controller;

import cn.zwq.service.IGeoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.zwq.entities.CommonResults;
import cn.zwq.websocket.service.PushMsgService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author zhangwenqia
 * @create 2022-10-16 16:37
 * @description 类描述
 */
@RestController
@RequestMapping("/redis")
public class RedisController {

	@Resource
	IGeoService geoService;

	@PostMapping("/delete")
	public CommonResults delete(@RequestBody JSONObject param) {
		String key = param.getString("key") + "";
		if (!key.endsWith("*")) {
			key += "*";
		}
		final Set<String> keys = geoService.deleteKeys(key);

		return new CommonResults(200, "发生消息成功", keys);
	}

}
