// model/FileType.java
package ll.luolin.model;

import ll.luolin.utils.LogUtils;

/**
 * 支持的文件类型枚举
 */
public enum FileType {
    SHP("Shapefile", ".shp", "ESRI Shapefile格式"),
    ASF("ASCII网格文件", ".asf", "ArcGIS ASCII网格格式"),
    GEOJSON("GeoJSON", ".geojson", "GeoJSON格式"),
    KML("KML", ".kml", "Keyhole Markup Language"),
    UNKNOWN("未知格式", "", "不支持的文件格式");
    
    private final String displayName;
    private final String extension;
    private final String description;
    
    FileType(String displayName, String extension, String description) {
        this.displayName = displayName;
        this.extension = extension;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getExtension() {
        return extension;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据文件扩展名获取文件类型
     */
    public static FileType fromExtension(String filename) {
        LogUtils.info("FileType-fromExtension-根据文件扩展名获取文件类型");

        if (filename == null) {
            return UNKNOWN;
        }
        
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".shp")) {
            return SHP;
        } else if (lowerName.endsWith(".asf")) {
            return ASF;
        } else if (lowerName.endsWith(".geojson") || lowerName.endsWith(".json")) {
            return GEOJSON;
        } else if (lowerName.endsWith(".kml")) {
            return KML;
        } else {
            return UNKNOWN;
        }
    }
    
    /**
     * 检查是否支持该文件类型
     */
    public static boolean isSupported(String filename) {
        FileType type = fromExtension(filename);
        return type != UNKNOWN;
    }
}
