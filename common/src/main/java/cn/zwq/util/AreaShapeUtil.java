package cn.zwq.util;

import cn.zwq.bo.AreaType;
import cn.zwq.bo.Circle;
import cn.zwq.bo.CustomArea;
import cn.zwq.bo.Region;

import java.awt.*;

/**
 * @author zhangwenqia
 * @create 2023-04-11 18:23
 * @description 类描述
 */
public class AreaShapeUtil {
	/**
	 * 转化区域shape对象
	 * 
	 * @param param
	 * @return
	 */
	public static Shape getAreaShape(Region param) {
		if (AreaType.CIRCLE.getCode().equals(param.getShapeType())) {// 圆形
			Double centerX = param.getCircle().getCenterLongitude();
			Double centerY = param.getCircle().getCenterLatitude();
			Double radius = param.getCircle().getRadius();
			Circle circle = new Circle(centerX, centerY, radius);
			return circle.toEllipse();
		} else if (AreaType.POLYGON.getCode().equals(param.getShapeType())) {// 多边形
			return new CustomArea(param.getRegionCoors()).parseAreaAsPolygon(param.getPointRegex(), param.getRegex());
		} else {// 道路
			return new CustomArea(param.getRoadAreaCoors()).parseAreaAsPolygon(param.getPointRegex(), param.getRegex());
		}
	}

}
