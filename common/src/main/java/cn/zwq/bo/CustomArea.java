package cn.zwq.bo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import cn.zwq.util.GisUtil;

/**
 * @author zhangwenqia
 * @create 2023-04-11 18:18
 * @description 类描述
 */
public class CustomArea {
	private String areaShape;

	public CustomArea() {
	}

	public CustomArea(String areaShape) {
		this.areaShape = areaShape;
	}

	public List<Point2D> parseAreaAsPointList(String pointRegex, String regex) {
		List<Point2D> pointList = new ArrayList<>();
		String[] pointStrArray = areaShape.split(pointRegex);
		for (String pointStr : pointStrArray) {
			String[] points = pointStr.split(regex);
			double x = Double.parseDouble(points[0]);
			double y = Double.parseDouble(points[1]);
			pointList.add(new Point2D.Double(x, y));
		}
		return pointList;
	}

	public Polygon parseAreaAsPolygon(String pointRegex, String regex) {
		List<Point2D> points = parseAreaAsPointList(pointRegex, regex);
		Polygon polygon = new Polygon();
		for (Point2D point : points) {
			int x = GisUtil.fromFloatCoordinateToInt(point.getX());
			int y = GisUtil.fromFloatCoordinateToInt(point.getY());
			polygon.addPoint(x, y);
		}
		return polygon;
	}
}
