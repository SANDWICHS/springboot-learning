package cn.zwq.config;

import ru.yandex.clickhouse.settings.ClickHouseProperties;

/**
 * @author zhangwenqia
 * @create 2023-03-20 17:36
 * @description 类描述
 */
public class ClickHouseDataSourceProperties extends ClickHouseProperties {
	private boolean isBalance = false;
	private String url;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isBalance() {
		return isBalance;
	}

	public void setBalance(boolean balance) {
		isBalance = balance;
	}
}
