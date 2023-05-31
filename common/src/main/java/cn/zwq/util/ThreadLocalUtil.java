package cn.zwq.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangwenqia
 * @create 2022-04-24 10:25
 * @description 类描述
 */
public class ThreadLocalUtil {
	private ThreadLocalUtil() {
	}

	/**
	 * 注意右边new的不是原生的ThreadLocal，而是我自定义的MapThreadLocal，它继承自ThreadLocal
	 *
	 * @see MapThreadLocal
	 */
	private final static ThreadLocal<Map<String, Object>> THREAD_CONTEXT = new MapThreadLocal();

	/**
	 * 根据key获取value 比如key为USER_INFO，则返回"{'name':'bravo', 'age':18}" {
	 * ...THREAD_CONTEXT: { ........."USER_INFO":"{'name':'bravo', 'age':18}",
	 * ........."SCORE":"{'Math':99, 'English': 97}" ......} }
	 *
	 * @param key
	 * @return
	 */
	public static Object get(String key) {
		// getContextMap()表示要先获取THREAD_CONTEXT的value，也就是Map<String, Object>。
		// 然后再从Map<String, Object>中根据key获取
		return getContextMap().get(key);
	}

	/**
	 * put操作，原理同上
	 *
	 * @param key
	 * @param value
	 */
	public static void put(String key, Object value) {
		getContextMap().put(key, value);
	}

	/**
	 * 清除map里的某个值 比如把 { ...THREAD_CONTEXT: { ........."USER_INFO":"{'name':'bravo',
	 * 'age':18}", ........."SCORE":"{'Math':99, 'English': 97}" ......} } 变成 {
	 * ...THREAD_CONTEXT: { ........."SCORE":"{'Math':99, 'English': 97}" ......} }
	 *
	 * @param key
	 * @return
	 */
	public static Object remove(String key) {
		return getContextMap().remove(key);
	}

	/**
	 * 清除整个Map<String, Object> 比如把 { ...THREAD_CONTEXT: {
	 * ........."USER_INFO":"{'name':'bravo', 'age':18}",
	 * ........."SCORE":"{'Math':99, 'English': 97}" ......} } 变成 {
	 * ...THREAD_CONTEXT: {} }
	 */
	public static void remove() {
		getContextMap().clear();
	}

	/**
	 * 从ThreadLocalMap中清除当前ThreadLocal存储的内容 比如把 { ...THREAD_CONTEXT: {
	 * ........."USER_INFO":"{'name':'bravo', 'age':18}",
	 * ........."SCORE":"{'Math':99, 'English': 97}" ......} } 变成 { }
	 */
	public static void clear() {
		THREAD_CONTEXT.remove();
	}

	public static void checkClear() {
		if (isEmpty()) {
			clear();
		}
	}

	public static boolean isEmpty() {
		return THREAD_CONTEXT.get().isEmpty();
	}

	/**
	 * 从ThreadLocalMap { ...THREAD_CONTEXT: { ........."USER_INFO":"{'name':'bravo',
	 * 'age':18}", ........."SCORE":"{'Math':99, 'English': 97}" ......} }
	 * 中获取Map<String, Object> { ..."USER_INFO":"{'name':'bravo', 'age':18}",
	 * ..."SCORE":"{'Math':99, 'English': 97}" }
	 *
	 * @return
	 */
	private static Map<String, Object> getContextMap() {
		return THREAD_CONTEXT.get();
	}

	/**
	 * 内部类，继承自ThreadLocal，仍旧指定value为Map<String, Object>
	 * 之所以要自定义MapThreadLocal，是为了重写原生ThreadLocal的initialValue()
	 * 把ThreadLocal中判断null的操作隐藏掉，让代码优雅一些（但对于初学者来说，理解难度也提升了）
	 */
	private static class MapThreadLocal extends ThreadLocal<Map<String, Object>> {

		@Override
		protected Map<String, Object> initialValue() {
			return new HashMap<String, Object>(8) {

				private static final long serialVersionUID = 3637958959138295593L;

				@Override
				public Object put(String key, Object value) {
					return super.put(key, value);
				}
			};
		}
	}

}
