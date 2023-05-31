package cn.zwq.bo;

import lombok.Data;

/**
 * @author zhangwenqia
 * @create 2023-04-11 18:31
 * @description 类描述
 */
@Data
public class Region {

	/**
	 * 坐标类型
	 */
	private String coorsType = "WGS84";

	/**
	 * 区域类型
	 */
	private Integer shapeType;

	/**
	 * 区域类型名称
	 */
	private String shapeTypeName;

	/**
	 * 圆行轮廓
	 */
	private Circle circle = new Circle();

	/**
	 * 转换后圆形轮廓
	 */
	private Circle newCircle = new Circle();

	/**
	 * 多边形区域轮廓
	 */
	private String regionCoors;

	/**
	 * 道路区域化
	 */
	private String roadAreaCoors;

	/**
	 * 道路外扩大小
	 */
	private Integer expand;

	/**
	 * 道路外扩大小
	 */
	private String pointRegex = "\\|";

	/**
	 * 道路外扩大小
	 */
	private String regex = ",";
}
