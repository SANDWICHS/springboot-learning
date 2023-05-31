package cn.zwq.cat;

import java.net.URL;
import java.util.Map;

/**
 * @author zhangwenqia
 * @create 2022-06-21 17:28
 * @description 类描述
 */
public interface CatNetTransaction {
	void init(URL url, Map<String, String> headerParams);

	void setStatus(String status);

	void setStatus(Throwable e);

	void logEvent(String type, String name);

	void logEvent(String type, String name, String status, String nameValuePairs);

	void complete();
}
