package cn.zwq.controller;

import cn.zwq.entities.CommonResults;
import cn.zwq.websocket.service.PushMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * @author zhangwenqia
 * @create 2022-10-16 16:37
 * @description 类描述
 */
@RestController
@RequestMapping("/msg")
public class MsgController {
	PushMsgService pushMsgService;

	@Autowired
	public void setPushMsgService(PushMsgService pushMsgService) {
		this.pushMsgService = pushMsgService;
	}

	@PostMapping("/pushOne")
	public CommonResults pushOne(@RequestBody JSONObject param) {
		JSONArray userIds = param.getJSONArray("userIds");
		if (userIds.isEmpty()) {
			return new CommonResults(-1, "调用失败", "userIds不能为空");
		}
		String msg = param.getString("msg");
		userIds.stream().forEach(id -> {
			String userId = id.toString();
			pushMsgService.pushMsgToOne(userId, msg);
		});
		return new CommonResults(200, "发生消息成功", msg);
	}

	@PostMapping("/pushAll")
	public CommonResults pushAll(@RequestBody JSONObject param) {
		String msg = param.getString("msg");

		pushMsgService.pushMsgToAll(msg);

		return new CommonResults(200, "发生消息成功", msg);
	}
}
