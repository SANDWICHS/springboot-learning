package cn.zwq.service.impl;

import cn.zwq.api.ProductApi;
import cn.zwq.entities.CommonResults;
import cn.zwq.service.OrderService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangwenqia
 * @create 2023-05-17 15:01
 * @description 类描述
 */
@Service
public class OrderServiceImpl implements OrderService {
	Logger logger = org.slf4j.LoggerFactory.getLogger(OrderService.class);

	private final RedisTemplate<String, String> redisTemplate;

	@Autowired
	public OrderServiceImpl(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Resource
	RestTemplate clusterRestTemplate;

	@Resource
	RestTemplate singleRestTemplate;

	@Resource
	ProductApi productApi;

	@Override
	public CommonResults order(Integer id) {
		String key = String.format("OrderInfo:Id:%d", id);
		final String value = redisTemplate.opsForValue().get(key);
		logger.info("获取数据,key:{},value:{}", key, value);
		JSONObject orderJson = JSON.parseObject(value);
		final Integer productId = orderJson.getInteger("productId");
		final CommonResults product = productApi.product(productId);
		String datas = (String) product.getDatas();
		logger.info("product:{}", datas);
		JSONObject detail = JSON.parseObject(datas);
		orderJson.put("ProductDetail", detail);
		int xx = 200 / id;
		logger.info("运算结果：{}", xx);
		return new CommonResults(200, "success", JSON.toJSONString(orderJson));
	}

	@Override
	public CommonResults orderList(Integer userId, Integer index, Integer eachCount) {
		String key = String.format("OrderInfo:UserId:%d", userId);
		int start = eachCount * (index - 1);
		int end = start + eachCount;
		final List<String> range = redisTemplate.opsForList().range(key, start, end);
		List<Integer> ids = new ArrayList<>(range.size());
		JSONArray orderJsons = new JSONArray();
		range.stream().forEach(value -> {
			JSONObject orderJson = JSON.parseObject(value);
			orderJsons.add(orderJson);
			ids.add(orderJson.getIntValue("productId"));
		});

		final CommonResults productList = productApi.productList(ids.stream().distinct().collect(Collectors.toList()));
		String datas = (String) productList.getDatas();
		JSONArray productJsons = JSON.parseArray(datas);
		Map<Integer, JSONObject> idProducts = new HashMap<>(productJsons.size());
		productJsons.forEach(value -> {
			JSONObject json = (JSONObject) value;
			idProducts.put(json.getInteger("id"), json);
		});

		orderJsons.forEach(value -> {
			JSONObject json = (JSONObject) value;
			final Integer productId = json.getInteger("productId");
			final JSONObject product = idProducts.get(productId);
			json.put("ProductDetail", product);
		});
		logger.info("获取数据,key:{},value:{}", key, range);
		return new CommonResults(200, "success", JSON.toJSONString(orderJsons));
	}

	@Override
	public CommonResults insert(JSONObject params) {
		final Integer id = params.getInteger("id");
		final Integer userId = params.getInteger("userId");
		String key = String.format("OrderInfo:Id:%d", id);
		String value = JSON.toJSONString(params);
		redisTemplate.opsForValue().set(key, value);
		logger.info("存储数据,key:{},value:{}", key, value);
		key = String.format("OrderInfo:UserId:%d", userId);
		redisTemplate.opsForList().rightPush(key, value);
		logger.info("存储数据,key:{},value:{}", key, value);
		return new CommonResults(200, "success", "入库成功");
	}

	@Override
	public CommonResults update(JSONObject params) {
		final Integer id = params.getInteger("id");
		final Integer userId = params.getInteger("userId");
		String key = String.format("OrderInfo:Id:%d", id);
		String value = redisTemplate.opsForValue().get(key);
		String currentValue = JSON.toJSONString(params);
		redisTemplate.opsForValue().set(key, currentValue);
		logger.info("存储数据,key:{},value:{}", key, currentValue);
		key = String.format("OrderInfo:UserId:%d", userId);
		if (StringUtils.isNotEmpty(value)) {
			redisTemplate.opsForList().remove(key, 1, value);
			logger.info("删除数据,key:{},value:{}", key, value);
		}
		redisTemplate.opsForList().rightPush(key, currentValue);
		logger.info("存储数据,key:{},value:{}", key, currentValue);
		return new CommonResults(200, "success", "更新成功");
	}

	@Override
	public CommonResults delete(JSONObject params) {
		final Integer id = params.getInteger("id");
		final Integer userId = params.getInteger("userId");
		String key = String.format("OrderInfo:Id:%d", id);
		String value = redisTemplate.opsForValue().get(key);
		redisTemplate.delete(key);
		key = String.format("OrderInfo:UserId:%d", userId);
		if (StringUtils.isNotEmpty(value)) {
			redisTemplate.opsForList().remove(key, 1, value);
		}
		return new CommonResults(200, "success", "删除成功");
	}
}
