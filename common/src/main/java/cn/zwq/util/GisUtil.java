package cn.zwq.util;

import java.math.BigDecimal;

/**
 * @author zhangwenqia
 * @create 2023-04-11 18:21
 * @description 类描述
 */
public class GisUtil {
	public static final int IRATIO = 10000000;
	public static final int LOT_PER_METER = 105;
	public static final int LAT_PER_METER = 90;
	public static final int METER = 40;
	private static int lotGridSize = 4000;
	private static int latGridSize = 3600;
	public static final int LOT_LEFT_TYPE = 0;
	public static final int LOT_RIGHT_TYPE = 1;
	public static final int LAT_UP_TYPE = 1;
	public static final int LAT_DOWN_TYPE = 0;

	private GisUtil() {
	}

	public static int fromFloatCoordinateToInt(double coordinate) {
		BigDecimal bigCoordinate = new BigDecimal(String.valueOf(coordinate));
		BigDecimal bigDecimal = new BigDecimal(10000000);
		BigDecimal multiply = bigCoordinate.multiply(bigDecimal);
		return multiply.intValue();
	}

	public static double fromIntCoordinateToFloat(int coordinate) {
		return coordinate / 1.0E7D;
	}

	public static int getX(int longitude) {
		return longitude / lotGridSize;
	}

	public static int getY(int latitude) {
		return latitude / latGridSize;
	}

	public static int getLot(int x) {
		return x * lotGridSize;
	}

	public static int getLat(int y) {
		return y * latGridSize;
	}

	public static int getX(int longitude, int meter) {
		return longitude / (meter * 100);
	}

	public static int getY(int latitude, int meter) {
		return latitude / (meter * 90);
	}

	public static int getLot(int x, int meter) {
		return x * meter * 100;
	}

	public static int getLat(int y, int meter) {
		return y * meter * 90;
	}

	public static int getLTLot(int lot, int meter) {
		int x = getX(lot, meter);
		return getLot(x, meter);
	}

	public static int getLTLat(int lat, int meter) {
		int y = getY(lat, meter);
		return getLat(y + 1, meter);
	}

	public static int getCenterLot(int lot, int meter, int type) {
		int partLot = (int) (0.5D * meter * 100.0D);
		switch (type) {
		case 0:
			return lot + partLot;
		case 1:
			return lot - partLot;
		default:
			return 0;
		}
	}

	public static int getCenterLat(int lat, int meter, int type) {
		int partLat = (int) (0.5D * meter * 90.0D);
		switch (type) {
		case 0:
			return lat + partLat;
		case 1:
			return lat - partLat;
		default:
			return 0;
		}
	}

	public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
		double longitudeDistance = (Math.sin((90.0D - latitude2) * 2.0D * 3.141592653589793D / 360.0D)
				+ Math.sin((90.0D - latitude1) * 2.0D * 3.141592653589793D / 360.0D)) / 2.0D * (longitude2 - longitude1) / 360.0D * 4.007536E7D;
		double latitudeDistance = (latitude2 - latitude1) / 360.0D * 3.994067E7D;
		return Math.sqrt(longitudeDistance * longitudeDistance + latitudeDistance * latitudeDistance);
	}

	public static double getDistance(int longitude1, int latitude1, int longitude2, int latitude2) {
		double lon1 = longitude1 / 1.0E7D;
		double lon2 = longitude2 / 1.0E7D;
		double lat1 = latitude1 / 1.0E7D;
		double lat2 = latitude2 / 1.0E7D;
		return getDistance(lon1, lat1, lon2, lat2);
	}
}
