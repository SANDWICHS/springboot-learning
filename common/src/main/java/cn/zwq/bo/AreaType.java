package cn.zwq.bo;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zhangwenqia
 * @create 2023-04-12 14:13
 * @description 类描述
 */
public enum AreaType {
	CIRCLE(1, "圆形"),
	POLYGON(2, "多边形"),
	CELL(3, "基站");

	protected static final Map<Integer, AreaType> areaTypeMap = Arrays.stream(AreaType.values())
			.collect(Collectors.toMap(AreaType::getCode, areaType -> areaType));

	public static AreaType fromCode(Integer code) {
		return areaTypeMap.get(code);
	}

	private Integer code;
	private String message;

	AreaType(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
