// config/StyleConfig.java - 添加坐标显示配置
package ll.luolin.config;

import java.awt.Color;
import java.util.prefs.Preferences;

/**
 * 样式配置
 */
public class StyleConfig {

    private static final Preferences PREFS = Preferences.userNodeForPackage(StyleConfig.class);

    // 坐标显示配置
    public static class CoordinateDisplay {
        public static final int DECIMAL_PLACES = 4;
        public static final boolean SHOW_DMS = true; // 是否显示度分秒
        public static final boolean SHOW_IN_STATUS_BAR = true;
        public static final Color TEXT_COLOR = Color.BLACK;
        public static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 180);
    }

    // ... 其他配置 ...

    /**
     * 获取坐标显示的小数位数
     */
    public static int getCoordinateDecimalPlaces() {
        return PREFS.getInt("coordinate.decimalPlaces", CoordinateDisplay.DECIMAL_PLACES);
    }

    /**
     * 设置坐标显示的小数位数
     */
    public static void setCoordinateDecimalPlaces(int places) {
        PREFS.putInt("coordinate.decimalPlaces", places);
    }

    /**
     * 是否显示度分秒格式
     */
    public static boolean isShowDMS() {
        return PREFS.getBoolean("coordinate.showDMS", CoordinateDisplay.SHOW_DMS);
    }

    /**
     * 设置是否显示度分秒格式
     */
    public static void setShowDMS(boolean showDMS) {
        PREFS.putBoolean("coordinate.showDMS", showDMS);
    }
}
