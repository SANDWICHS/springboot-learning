package cn.zwq.controller;

import java.util.*;

import javax.annotation.Resource;

import cn.zwq.service.SqlService;
import org.springframework.web.bind.annotation.*;

import com.alibaba.fastjson.JSONObject;

import cn.zwq.entities.CommonResults;

/**
 * @author zhangwenqia
 * @create 2023-03-22 18:18
 * @description 类描述
 */
@RestController
@RequestMapping("/sql")
public class SqlController {
	@Resource
	SqlService sqlService;

	@PostMapping("/query")
	public CommonResults query(@RequestBody JSONObject param) {
		final String sql = param.getString("sql");
		Object[] params;
		if (param.containsKey("paramCount")) {
			final Integer paramCount = param.getInteger("paramCount");
			params = new Object[paramCount];
			for (Integer i = 0; i < paramCount; i++) {
				params[i] = param.getString(String.format("param", i + 1));
			}
		} else {
			params = new Object[0];
		}
		return new CommonResults(0, "success", sqlService.query(sql, params));
	}

	@PostMapping("/update")
	public CommonResults update(@RequestBody JSONObject param) {
		final String sql = param.getString("sql");
		Object[] params;
		if (param.containsKey("paramCount")) {
			final Integer paramCount = param.getInteger("paramCount");
			params = new Object[paramCount];
			for (Integer i = 0; i < paramCount; i++) {
				params[i] = param.getString(String.format("param", i + 1));
			}
		} else {
			params = new Object[0];
		}
		return new CommonResults(0, "success", sqlService.update(sql, params));
	}

}
