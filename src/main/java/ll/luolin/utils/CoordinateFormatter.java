// utils/CoordinateFormatter.java
package ll.luolin.utils;

/**
 * 坐标格式化工具类
 */
public class CoordinateFormatter {
    
    /**
     * 格式化经纬度为度分秒格式
     */
    public static String formatToDMS(double lon, double lat) {
        LogUtils.info("CoordinateFormatter-formatToDMS-格式化经纬度为度分秒格式");


        String lonDMS = decimalToDMS(lon, true);
        String latDMS = decimalToDMS(lat, false);
        return String.format("%s, %s", lonDMS, latDMS);
    }
    
    /**
     * 十进制转度分秒
     */
    private static String decimalToDMS(double decimal, boolean isLongitude) {
        LogUtils.info("CoordinateFormatter-decimalToDMS-十进制转度分秒");

        char direction;
        if (isLongitude) {
            direction = decimal >= 0 ? 'E' : 'W';
        } else {
            direction = decimal >= 0 ? 'N' : 'S';
        }
        
        decimal = Math.abs(decimal);
        int degrees = (int) decimal;
        double minutesDecimal = (decimal - degrees) * 60;
        int minutes = (int) minutesDecimal;
        double seconds = (minutesDecimal - minutes) * 60;
        
        return String.format("%d°%d'%.2f\"%c", degrees, minutes, seconds, direction);
    }
    
    /**
     * 格式化坐标为简洁格式
     */
    public static String formatSimple(double lon, double lat, int decimalPlaces) {
        LogUtils.info("CoordinateFormatter-formatSimple-格式化坐标为简洁格式");

        String format = "%." + decimalPlaces + "f";
        return String.format(format + "°E, " + format + "°N", lon, lat);
    }
    
    /**
     * 格式化坐标为带符号的格式
     */
    public static String formatWithSign(double lon, double lat) {
        LogUtils.info("CoordinateFormatter-formatWithSign-格式化坐标为带符号的格式");

        String lonSign = lon >= 0 ? "E" : "W";
        String latSign = lat >= 0 ? "N" : "S";
        return String.format("%.4f°%s, %.4f°%s", 
            Math.abs(lon), lonSign, Math.abs(lat), latSign);
    }
}
