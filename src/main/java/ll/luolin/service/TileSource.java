// view/TileSource.java - 确保完整
package ll.luolin.service;

import ll.luolin.utils.LogUtils;

public enum TileSource {
    OPENSTREETMAP("OpenStreetMap",
            "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png"),
    
    GOOGLE_MAPS("Google地图",
            "https://mt1.google.com/vt/lyrs=m&x={x}&y={y}&z={z}"),
    
    ARCGIS_SATELLITE("ArcGIS卫星图",
            "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"),
    
    LOCAL_TILES("本地瓦片地图",
            "file:///{path}/tiles/{z}/{x}/{y}.png"),
    
    NONE("无底图", "");
    
    private final String name;
    private final String urlTemplate;
    
    TileSource(String name, String urlTemplate) {
        LogUtils.info("TileSource-TileSource-初始化TileSource");

        this.name = name;
        this.urlTemplate = urlTemplate;
    }
    
    public String getName() {
        return name;
    }
    
    public String getUrlTemplate() {
        return urlTemplate;
    }
    
    public String getTileUrl(int x, int y, int zoom) {
        LogUtils.info("TileSource-getTileUrl-获取瓦片路径");

        if (this == NONE) {
            return "";
        }
        
        if (this == LOCAL_TILES) {
            String localPath = System.getProperty("user.home") + "/map_tiles";
            return urlTemplate
                    .replace("{path}", localPath.replace("\\", "/"))
                    .replace("{x}", String.valueOf(x))
                    .replace("{y}", String.valueOf(y))
                    .replace("{z}", String.valueOf(zoom));
        }
        
        return urlTemplate
                .replace("{x}", String.valueOf(x))
                .replace("{y}", String.valueOf(y))
                .replace("{z}", String.valueOf(zoom));
    }
    
    public boolean requiresInternet() {
        return this != LOCAL_TILES && this != NONE;
    }
    
    public boolean supportsOffline() {
        return this == LOCAL_TILES || this == NONE;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
