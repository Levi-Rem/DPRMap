// config/AppConstants.java
package ll.luolin.config;

/**
 * 应用常量
 */
public class AppConstants {
    
    // 应用信息
    public static final String APP_NAME = "地图可视化工具";
    public static final String APP_VERSION = "2.0.0";
    public static final String APP_AUTHOR = "ll.luolin";
    
    // 窗口尺寸
    public static final int WINDOW_WIDTH = 1400;
    public static final int WINDOW_HEIGHT = 900;
    public static final int CANVAS_WIDTH = 1200;
    public static final int CANVAS_HEIGHT = 800;
    
    // 地图参数
    public static final double INITIAL_CENTER_LON = 104.0;
    public static final double INITIAL_CENTER_LAT = 35.0;
    public static final int INITIAL_ZOOM_LEVEL = 5;
    public static final int MIN_ZOOM_LEVEL = 0;
    public static final int MAX_ZOOM_LEVEL = 18;
    
    // 瓦片参数
    public static final int TILE_SIZE = 256;
    public static final int TILE_CACHE_SIZE = 100;
    
    // 文件相关
    public static final String[] SHP_FILE_EXTENSIONS = {
        ".shp", ".shx", ".dbf", ".prj", ".sbn", ".sbx", ".cpg", ".qix"
    };
    
    // 资源路径
    public static final String RESOURCE_DIR = "src/main/resources";
    public static final String SHP_DIR = RESOURCE_DIR + "/shp";
    public static final String CONFIG_DIR = RESOURCE_DIR + "/config";
    public static final String STYLE_DIR = RESOURCE_DIR + "/style";
    
    // 日志配置
    public static final String LOG_FILE = "logs/app.log";
    public static final int LOG_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    public static final int LOG_FILE_COUNT = 5;
    
    private AppConstants() {
        // 防止实例化
    }
}
