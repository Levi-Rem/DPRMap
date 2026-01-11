// config/AppConfig.java
package ll.luolin.config;

import org.geotools.util.factory.GeoTools;
import org.geotools.util.factory.Hints;
import java.util.prefs.Preferences;

public class AppConfig {
    private static final Preferences PREFS = Preferences.userNodeForPackage(AppConfig.class);
    
    public static void initialize() {
        // 设置系统属性
        System.setProperty("org.geotools.referencing.forceXY", "true");
        System.setProperty("java.awt.headless", "false");
        
        // 设置GeoTools提示
        Hints.putSystemDefault(Hints.FORCE_LONGITUDE_FIRST_AXIS_ORDER, Boolean.TRUE);
        Hints.putSystemDefault(Hints.LENIENT_DATUM_SHIFT, Boolean.TRUE);
        
        // 初始化GeoTools
        GeoTools.init();
        
        // 加载用户配置
        loadUserPreferences();
    }
    
    private static void loadUserPreferences() {
        // 加载用户保存的配置
    }
    
    public static String get(String key, String defaultValue) {
        return PREFS.get(key, defaultValue);
    }
    
    public static void set(String key, String value) {
        PREFS.put(key, value);
    }
}
