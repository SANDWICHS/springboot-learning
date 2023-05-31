package cn.zwq.entities;

/**
 * @author zhangwenqia
 * @create 2023-03-16 16:41
 * @description 类描述
 */
public class ResourceInfo {
	/**
	 * 唯一标识
	 */
	private String key;
	/**
	 * 经度
	 */
	private Double longitude;
	/**
	 * 纬度
	 */
	private Double latitude;

	public ResourceInfo(String key, Double longitude, Double latitude) {
		this.key = key;
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
}
