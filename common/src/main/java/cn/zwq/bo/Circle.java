package cn.zwq.bo;

import cn.zwq.util.GisUtil;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.awt.geom.Ellipse2D;

/**
 * @author zhangwenqia
 * @create 2023-04-11 18:32
 * @description 类描述
 */
@Data
public class Circle {
	private static final long serialVersionUID = 1905122041950251210L;

	@Min(value = -180, message = "centerX必须大于等于-180")
	@Max(value = 180, message = "centerX必须小于等于180")
	private Double centerLongitude;

	@Min(value = -90, message = "centerY必须大于等于-90")
	@Max(value = 90, message = "centerY必须小于等于90")
	private Double centerLatitude;

	@Min(value = 0, message = "radius必须大于0")
	private Double radius;

	public Circle() {
	}

	public Circle(double centerLongitude, double centerLatitude, double radius) {
		this.centerLongitude = centerLongitude;
		this.centerLatitude = centerLatitude;
		this.radius = radius;
	}

	public Ellipse2D toEllipse() {
		double x = GisUtil.fromFloatCoordinateToInt(centerLongitude) - GisUtil.LOT_PER_METER * radius;
		double y = GisUtil.fromFloatCoordinateToInt(centerLatitude) - GisUtil.LAT_PER_METER * radius;
		return new Ellipse2D.Double(x, y, GisUtil.LOT_PER_METER * radius * 2, GisUtil.LAT_PER_METER * radius * 2);
	}

	@Override
	public String toString() {
		return "Circle{" +
				"centerX=" + centerLongitude +
				", centerY=" + centerLatitude +
				", radius=" + radius +
				'}';
	}

	public static Circle parseCircle(String text) {
		if (text == null) {
			return null;
		}

		if (!text.startsWith("Circle{") || !text.endsWith("}")) {
			return null;
		}

		int i = text.indexOf('=');
		int i1 = text.indexOf(',');
		double centerX = Double.parseDouble(text.substring(i + 1, i1));
		i = text.indexOf('=', i + 1);
		i1 = text.indexOf(',', i1 + 1);
		double centerY = Double.parseDouble(text.substring(i + 1, i1));
		i = text.indexOf('=', i + 1);
		i1 = text.indexOf('}');
		double radius = Double.parseDouble(text.substring(i + 1, i1));

		return new Circle(centerX, centerY, radius);
	}
}
