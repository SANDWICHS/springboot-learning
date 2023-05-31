package cn.zwq.redis;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;

import com.alibaba.fastjson.JSON;

import cn.zwq.redis.bo.RedisExpireData;

public class RedisUtils {
	/**
	 *
	 * 提供通过传入redis命令的方式执行操作
	 *
	 * @param command redis命令
	 * @param args    命令需要的参数
	 * @return 返回的结果
	 */
	public static Object execute(RedisTemplate<String, Object> redisTemplate, String command, byte[]... args) {
		try {
			RedisConnection conn = redisTemplate.getConnectionFactory().getConnection();
			Object result = conn.execute(command, args);
			if (result instanceof byte[]) {
				return new String((byte[]) result);
			} else {
				return result;
			}
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Value类型数据， 写入redis
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean set(RedisTemplate<String, Object> redisTemplate, String key, Object value) {
		return set(redisTemplate, key, value, null);
	}

	/**
	 * 操作Value类型数据， 写入redis并设置保存时间，单位为秒
	 *
	 * @param key
	 * @param value
	 * @param saveTime 需要保存的时间长度，单位为秒（S）
	 * @return
	 */
	public static boolean set(RedisTemplate<String, Object> redisTemplate, String key, Object value, Long saveTime) {
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			operations.set(key, value);
			if (saveTime != null) {
				redisTemplate.expire(key, saveTime, TimeUnit.SECONDS);
			}
			return true;
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Value类型数据， 如果key在redis中不存在，则写入redis，并且可以设置保存的时间长度和对应的单位
	 *
	 * @param key
	 * @param value
	 * @param saveTime 保存时间的长度
	 * @param unit     保存时间的单位
	 * @return
	 */
	public static boolean setIfAbsent(RedisTemplate<String, Object> redisTemplate, String key, Object value, long saveTime, TimeUnit unit) {
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			return operations.setIfAbsent(key, value, saveTime, unit);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Value类型数据，删除redis中的key-value。
	 *
	 * @param key
	 */
	public static void remove(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			redisTemplate.delete(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Value类型数据，批量删除redis中的key-value。
	 *
	 * @param keys
	 */
	public static void remove(RedisTemplate<String, Object> redisTemplate, String... keys) {
		for (String key : keys) {
			remove(redisTemplate, key);
		}
	}

	/**
	 * 操作Value类型数据，根据正则表达式获取需要删除的key并批量删除
	 *
	 * @param pattern
	 */
	public static void removePattern(RedisTemplate<String, Object> redisTemplate, String pattern) {
		try {
			Set<String> keys = redisTemplate.keys(pattern);
			if (!keys.isEmpty()) {
				redisTemplate.delete(keys);
			}
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Value类型数据，设置key的过期时间，单位为毫秒
	 *
	 * @param key
	 * @param saveTime
	 */
	public static void expries(RedisTemplate<String, Object> redisTemplate, String key, long saveTime) {
		try {
			redisTemplate.expire(key, saveTime, TimeUnit.MILLISECONDS);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Value类型数据，判断redis中是否存在该key
	 *
	 * @param key
	 * @return
	 */
	public static boolean exists(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			return redisTemplate.hasKey(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Value类型数据，从redis中获取该key对应value值
	 *
	 * @param key
	 * @return
	 */
	public static Object get(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			return operations.get(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Value类型数据，获取指定key的过期时间，单位为秒
	 *
	 * @param key
	 * @return
	 */
	public static Long getExpire(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			return redisTemplate.getExpire(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Hash类型数据，添加整个hash表导hash结构
	 *
	 * @param key
	 * @param value
	 */
	public static void hmPutAll(RedisTemplate<String, Object> redisTemplate, String key, Map<String, Object> value) {
		try {
			HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
			hash.putAll(key, value);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Hash类型数据，添加字段到hash结构
	 *
	 * @param key
	 * @param field
	 * @param value
	 */
	public static void hmSet(RedisTemplate<String, Object> redisTemplate, String key, Object field, Object value) {
		try {
			HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
			hash.put(key, field, value);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Hash类型数据，删除指定key的hash中的属性和值
	 *
	 * @param key
	 * @param fields
	 */
	public static void hmRemove(RedisTemplate<String, Object> redisTemplate, String key, Object... fields) {
		try {
			HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
			hash.delete(key, fields);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Hash类型数据，判断指定key的hash中是否存在属性field
	 *
	 * @param key
	 * @param field
	 * @return
	 */
	public static boolean exsistHashFile(RedisTemplate<String, Object> redisTemplate, String key, Object field) {
		try {
			HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
			return hash.hasKey(key, field);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Hash类型数据，从hash结构获取指定属性的值
	 *
	 * @param key
	 * @param field
	 * @return
	 */
	public static Object hmGet(RedisTemplate<String, Object> redisTemplate, String key, Object field) {
		try {
			HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
			return hash.get(key, field);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Hash类型数据，获取指定key的hash所有属性和值
	 *
	 * @param key
	 * @return
	 */
	public static Map<Object, Object> hmGetAll(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
			return hash.entries(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Hash类型数据，通过正在表达式查询指定key的hash中符合条件的属性和值
	 *
	 * @param key
	 * @param fieldPattern
	 * @return
	 * @throws IOException
	 */
	public static Map<Object, Object> hmGetPattern(RedisTemplate<String, Object> redisTemplate, String key, String fieldPattern) throws IOException {
		try {
			Map<Object, Object> result = new HashMap<>();
			HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
			ScanOptions.ScanOptionsBuilder sacanBuider = ScanOptions.scanOptions();
			sacanBuider.match(fieldPattern);
			ScanOptions sb = sacanBuider.build();
			try (Cursor<Map.Entry<Object, Object>> theCur = hash.scan(key, sb)) {
				while (theCur.hasNext()) {
					Map.Entry<Object, Object> e = theCur.next();
					result.put(e.getKey(), e.getValue());
				}
			}
			return result;
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Hash类型数据，通过正则表达式获取匹配的所有hash列表
	 *
	 * @param keyPattern
	 * @return
	 */
	public static Map<Object, Object> hashsGetPattern(RedisTemplate<String, Object> redisTemplate, String keyPattern) {
		try {
			Map<Object, Object> result = new HashMap<>();
			Set<String> keys = redisTemplate.keys(keyPattern);
			for (String key : keys) {
				try {
					HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
					result.putAll(hash.entries(key));
				} catch (Exception e) {
					//
				}
			}
			return result;
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * @Description 获取redis hash 带过期时间的数据
	 * @Param [redisTemplate, redisKey, hashKey]
	 * @Return java.lang.String
	 */
	public static Object getRedisHash(RedisTemplate<String, String> redisTemplate, String redisKey, String hashKey) {
		String hashValue = redisTemplate.<String, String>opsForHash().get(redisKey, hashKey);
		if (hashValue == null) {
			return null;
		}

		RedisExpireData redisData = JSON.parseObject(hashValue, RedisExpireData.class);
		if (redisData == null) {
			return null;
		} else {
			Object obj = redisData.getStoreData();
			if (obj == null) {
				redisTemplate.<String, String>opsForHash().delete(redisKey, hashKey);
			}
			return obj;
		}
	}

	/**
	 * @Description 设置带过期时间的redis hash
	 * @Param [redisTemplate, redisKey, hashKey, hashValue, expire, timeUnit]
	 * @Return void
	 */
	public static void setRedisHash(RedisTemplate<String, String> redisTemplate, String redisKey, String hashKey, Object hashValue, Long expire,
			TimeUnit timeUnit) {
		RedisExpireData redisData = new RedisExpireData(hashValue, timeUnit.toMillis(expire));
		redisTemplate.<String, String>opsForHash().put(redisKey, hashKey, JSON.toJSONString(redisData));
	}

	/**
	 * 操作List类型数据，从左侧往List插入元素
	 *
	 * @param key
	 * @param value
	 */
	public static void leftPush(RedisTemplate<String, Object> redisTemplate, String key, Object value) {
		try {
			ListOperations<String, Object> list = redisTemplate.opsForList();
			list.leftPush(key, value);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作List类型数据，从右侧往List插入元素
	 *
	 * @param key
	 * @param value
	 */
	public static void rightPush(RedisTemplate<String, Object> redisTemplate, String key, Object value) {
		try {
			ListOperations<String, Object> list = redisTemplate.opsForList();
			list.rightPush(key, value);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作List类型数据，从左侧从List获取元素
	 *
	 * @param key
	 * @return
	 */
	public static Object leftPop(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			ListOperations<String, Object> list = redisTemplate.opsForList();
			return list.leftPop(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作List类型数据，从右侧从List获取元素
	 *
	 * @param key
	 * @return
	 */
	public static Object rightPop(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			ListOperations<String, Object> list = redisTemplate.opsForList();
			return list.rightPop(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作List类型数据，获取[start,end]范围的数据，从左侧开始索引 获取范围值，闭区间，start和end这两个下标的值都会返回;
	 * end为-1时，表示获取的是最后一个； 如果希望返回最后两个元素，可以传入 -2, -1
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<Object> lRange(RedisTemplate<String, Object> redisTemplate, String key, long start, long end) {
		try {
			ListOperations<String, Object> list = redisTemplate.opsForList();
			return list.range(key, start, end);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	public static Object lTrim(RedisTemplate<String, Object> redisTemplate, String key, long start, long end) {
		try {
			return redisTemplate.execute((RedisCallback<Object>) connection -> {
				connection.lTrim(key.getBytes(), end - start, -1);
				return true;
			});
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	public static List<Object> popQueueList(RedisTemplate<String, Object> redisTemplate, String key, long start, long end) {
		try {
			List<Object> list = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
				connection.openPipeline();
				connection.lRange(key.getBytes(), start, end);
				connection.lTrim(key.getBytes(), end - start, -1);
				return null;
			});
			return list.isEmpty() ? new ArrayList<>() : (List<Object>) list.get(0);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作List类型数据，获取指定key的list中的元素个数
	 *
	 * @param key
	 * @return
	 */
	public static long listSize(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			ListOperations<String, Object> list = redisTemplate.opsForList();
			return list.size(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Set类型数据，往set中添加元素
	 *
	 * @param key
	 * @param value
	 */
	public static void setAdd(RedisTemplate<String, Object> redisTemplate, String key, Object value) {
		try {
			SetOperations<String, Object> set = redisTemplate.opsForSet();
			set.add(key, value);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Set类型数据，获取指定set的所有元素
	 *
	 * @param key
	 * @return
	 */
	public static Set<Object> setMembers(RedisTemplate<String, Object> redisTemplate, String key) {
		try {
			SetOperations<String, Object> set = redisTemplate.opsForSet();
			return set.members(key);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Order-Set类型数据，往Order-Set类型插入元素
	 *
	 * @param key
	 * @param value
	 * @param scoure
	 */
	public static void zSetAdd(RedisTemplate<String, Object> redisTemplate, String key, Object value, double scoure) {
		try {
			ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
			zset.add(key, value, scoure);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * 操作Order-Set类型数据，获取指定分值间的集合元素
	 *
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	public static Set<Object> rangeZSet(RedisTemplate<String, Object> redisTemplate, String key, double min, double max) {
		try {
			ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
			return zset.rangeByScore(key, min, max);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * SETBIT key offset value 操作bitmap类型数据， 写入redis
	 *
	 * @param key
	 * @param offset 控制偏移量
	 * @param b
	 * @return
	 */
	public static boolean setBit(RedisTemplate<String, Object> redisTemplate, String key, int offset, boolean b) {
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			return operations.setBit(key, offset, b);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/**
	 * GETBIT key offset 操作bitmap类型数据， 从redis中获取该key对应value值
	 *
	 * @param key
	 * @param offset 控制偏移量
	 * @return
	 */
	public static boolean getBit(RedisTemplate<String, Object> redisTemplate, String key, int offset) {
		try {
			ValueOperations<String, Object> operations = redisTemplate.opsForValue();
			return operations.getBit(key, offset);
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	/*
	 * redis命令：BITCOUNT key [start end]
	 * 
	 * 获取bitmap中1的个数。
	 */
	public static Long bitCount(RedisTemplate<String, Object> redisTemplate, String key) {
		return bitCount(redisTemplate, key, null, null);
	}

	public static Long bitCount(RedisTemplate<String, Object> redisTemplate, String key, Long start, Long end) {
		try {
			if (start != null && end != null) {
				return redisTemplate.execute((RedisCallback<Long>) con -> con.bitCount(key.getBytes(), start, end));
			} else {
				return redisTemplate.execute((RedisCallback<Long>) con -> con.bitCount(key.getBytes()));
			}
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}

	public static List<Long> bitField(RedisTemplate<String, Object> redisTemplate, String key, int limit, long offset) {
		try {
			return redisTemplate.execute((RedisCallback<List<Long>>) con -> con.bitField(key.getBytes(),
					BitFieldSubCommands.create().get(BitFieldSubCommands.BitFieldType.unsigned(limit)).valueAt(offset)));
		} finally {
			RedisConnectionUtils.unbindConnection(redisTemplate.getConnectionFactory());
		}
	}
}
