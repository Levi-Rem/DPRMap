// utils/CoordinateParser.java
package ll.luolin.utils;

/**
 * 坐标解析工具类
 * 支持格式：371112N1221341E (度分秒格式)
 */
public class CoordinateParser {
    
    /**
     * 解析度分秒格式的坐标
     * 格式：371112N1221341E
     * 解释：37度11分12秒北纬，122度13分41秒东经
     */
    public static double[] parseDMS(String dmsString) {
        LogUtils.info("CoordinateParser-parseDMS-解析度分秒格式的坐标");

        if (dmsString == null || dmsString.trim().isEmpty()) {
            return new double[]{0.0, 0.0};
        }
        
        try {
            // 移除空格
            dmsString = dmsString.trim();
            
            // 分离纬度和经度部分
            String latPart, lonPart;
            
            // 查找N/S分隔符
            int latEndIndex = Math.max(
                dmsString.indexOf('N'),
                dmsString.indexOf('S')
            );
            
            if (latEndIndex == -1) {
                // 如果没有找到N/S，尝试其他格式
                return parseAlternativeFormat(dmsString);
            }
            
            latPart = dmsString.substring(0, latEndIndex + 1);
            lonPart = dmsString.substring(latEndIndex + 1);
            
            // 解析纬度
            double latitude = parseSingleCoordinate(latPart);
            
            // 解析经度
            double longitude = parseSingleCoordinate(lonPart);
            
            return new double[]{longitude, latitude};
            
        } catch (Exception e) {
            LogUtils.error("解析坐标失败: " + dmsString, e);
            return new double[]{0.0, 0.0};
        }
    }
    
    /**
     * 解析单个坐标（纬度或经度）
     * 格式：371112N 或 1221341E
     */
    private static double parseSingleCoordinate(String coord) {
        LogUtils.info("CoordinateParser-parseSingleCoordinate-解析单个坐标");

        if (coord == null || coord.length() < 2) {
            return 0.0;
        }
        
        // 获取方向字符（最后一个字符）
        char direction = coord.charAt(coord.length() - 1);
        
        // 获取数值部分
        String valuePart = coord.substring(0, coord.length() - 1);
        
        // 根据长度确定格式
        double degrees, minutes, seconds;
        
        if (valuePart.length() == 6) {
            // 格式：DDMMSS (度分秒各2位)
            degrees = Double.parseDouble(valuePart.substring(0, 2));
            minutes = Double.parseDouble(valuePart.substring(2, 4));
            seconds = Double.parseDouble(valuePart.substring(4, 6));
        } else if (valuePart.length() == 7) {
            // 格式：DDDMMSS (经度可能是3位度)
            degrees = Double.parseDouble(valuePart.substring(0, 3));
            minutes = Double.parseDouble(valuePart.substring(3, 5));
            seconds = Double.parseDouble(valuePart.substring(5, 7));
        } else if (valuePart.length() == 8) {
            // 格式：DDMMSS.SS (带小数秒)
            degrees = Double.parseDouble(valuePart.substring(0, 2));
            minutes = Double.parseDouble(valuePart.substring(2, 4));
            seconds = Double.parseDouble(valuePart.substring(4));
        } else {
            // 尝试其他格式
            return parseAlternativeCoordinate(coord);
        }
        
        // 计算十进制度数
        double decimal = degrees + minutes / 60.0 + seconds / 3600.0;
        
        // 根据方向调整符号
        if (direction == 'S' || direction == 'W') {
            decimal = -decimal;
        }
        
        return decimal;
    }
    
    /**
     * 解析替代格式的坐标
     */
    private static double parseAlternativeCoordinate(String coord) {
        LogUtils.info("CoordinateParser-parseAlternativeCoordinate-解析替代格式的坐标");

        try {
            // 尝试直接解析为十进制
            if (coord.contains(".")) {
                return Double.parseDouble(coord.replaceAll("[NSEW]", ""));
            }
            
            // 尝试度分格式
            if (coord.contains("°") || coord.contains("'")) {
                return parseDegreeMinuteFormat(coord);
            }
            
        } catch (Exception e) {
            LogUtils.error("解析替代格式坐标失败: " + coord, e);
        }
        
        return 0.0;
    }
    
    /**
     * 解析度分格式
     */
    private static double parseDegreeMinuteFormat(String coord) {
        LogUtils.info("CoordinateParser-parseDegreeMinuteFormat-解析度分格式");

        // 示例：37°11.2'N 或 122°13.41'E
        coord = coord.trim();
        
        // 提取方向
        char direction = coord.charAt(coord.length() - 1);
        coord = coord.substring(0, coord.length() - 1);
        
        // 分割度和分
        String[] parts = coord.split("[°']");
        if (parts.length >= 2) {
            double degrees = Double.parseDouble(parts[0]);
            double minutes = Double.parseDouble(parts[1]);
            
            double decimal = degrees + minutes / 60.0;
            
            if (direction == 'S' || direction == 'W') {
                decimal = -decimal;
            }
            
            return decimal;
        }
        
        return 0.0;
    }
    
    /**
     * 解析替代格式
     */
    private static double[] parseAlternativeFormat(String dmsString) {
        LogUtils.info("CoordinateParser-parseAlternativeFormat-解析替代格式");

        try {
            // 尝试空格分隔的格式
            if (dmsString.contains(" ")) {
                String[] parts = dmsString.split(" ");
                if (parts.length >= 2) {
                    double lat = parseSingleCoordinate(parts[0]);
                    double lon = parseSingleCoordinate(parts[1]);
                    return new double[]{lon, lat};
                }
            }
            
            // 尝试逗号分隔的格式
            if (dmsString.contains(",")) {
                String[] parts = dmsString.split(",");
                if (parts.length >= 2) {
                    double lat = parseSingleCoordinate(parts[0].trim());
                    double lon = parseSingleCoordinate(parts[1].trim());
                    return new double[]{lon, lat};
                }
            }
            
        } catch (Exception e) {
            LogUtils.error("解析替代格式失败: " + dmsString, e);
        }
        
        return new double[]{0.0, 0.0};
    }
    
    /**
     * 验证坐标字符串格式
     */
    public static boolean isValidDMS(String dmsString) {
        LogUtils.info("CoordinateParser-isValidDMS-验证坐标字符串格式");

        if (dmsString == null || dmsString.trim().isEmpty()) {
            return false;
        }
        
        String str = dmsString.trim().toUpperCase();
        
        // 检查是否包含N/S和E/W
        boolean hasLatDir = str.contains("N") || str.contains("S");
        boolean hasLonDir = str.contains("E") || str.contains("W");
        
        return hasLatDir && hasLonDir;
    }
    
    /**
     * 将十进制坐标转换为度分秒格式
     */
    public static String toDMS(double decimal, boolean isLongitude) {
        LogUtils.info("CoordinateParser-toDMS-将十进制坐标转换为度分秒格式");

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
        
        if (isLongitude) {
            return String.format("%03d%02d%02d%c", degrees, minutes, (int) seconds, direction);
        } else {
            return String.format("%02d%02d%02d%c", degrees, minutes, (int) seconds, direction);
        }
    }
}
