// service/TileService.java
package ll.luolin.service;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import ll.luolin.model.MapModel;
import ll.luolin.utils.LogUtils;
import java.util.concurrent.*;

/**
 * 瓦片服务
 * 负责瓦片的加载、缓存和渲染
 */
public class TileService {
    private static TileService instance;
    
    // 瓦片缓存
    private final ConcurrentHashMap<String, Image> tileCache = new ConcurrentHashMap<>();
    private final ExecutorService tileLoader = Executors.newFixedThreadPool(4);
    
    // 瓦片源配置
    private TileSource currentSource = TileSource.OPENSTREETMAP;
    private static final int TILE_SIZE = 256;
    
    private TileService() {}
    
    public static synchronized TileService getInstance() {
        if (instance == null) {
            instance = new TileService();
        }
        return instance;
    }
    
    /**
     * 渲染瓦片
     */
    public void renderTiles(GraphicsContext gc, MapModel mapModel, 
                           double canvasWidth, double canvasHeight) {
        LogUtils.info("TileService-renderTiles-渲染瓦片  ");

        int zoom = mapModel.getZoomLevel();
        double centerX = mapModel.getCenterX();
        double centerY = mapModel.getCenterY();
        
        // 计算可见的瓦片范围
        int[] tileRange = calculateTileRange(centerX, centerY, zoom, canvasWidth, canvasHeight);
        
        // 绘制瓦片
        for (int x = tileRange[0]; x <= tileRange[2]; x++) {
            for (int y = tileRange[1]; y <= tileRange[3]; y++) {
                drawTile(gc, x, y, zoom, canvasWidth, canvasHeight);
            }
        }
    }
    
    /**
     * 计算可见瓦片范围
     */
    private int[] calculateTileRange(double centerLon, double centerLat, int zoom,
                                    double canvasWidth, double canvasHeight) {
        LogUtils.info("TileService-calculateTileRange-计算可见瓦片范围  ");

        // 将中心点转换为瓦片坐标
        int centerTileX = lonToTileX(centerLon, zoom);
        int centerTileY = latToTileY(centerLat, zoom);
        
        // 计算画布能显示的瓦片数量
        int tilesX = (int) Math.ceil(canvasWidth / TILE_SIZE) + 2;
        int tilesY = (int) Math.ceil(canvasHeight / TILE_SIZE) + 2;
        
        int minX = centerTileX - tilesX / 2;
        int maxX = centerTileX + tilesX / 2;
        int minY = centerTileY - tilesY / 2;
        int maxY = centerTileY + tilesY / 2;
        
        // 限制瓦片坐标范围
        int maxTile = (int) Math.pow(2, zoom) - 1;
        minX = Math.max(0, minX);
        maxX = Math.min(maxTile, maxX);
        minY = Math.max(0, minY);
        maxY = Math.min(maxTile, maxY);
        
        return new int[]{minX, minY, maxX, maxY};
    }
    
    /**
     * 绘制单个瓦片
     */
    private void drawTile(GraphicsContext gc, int x, int y, int zoom,
                         double canvasWidth, double canvasHeight) {
        LogUtils.info("TileService-drawTile-绘制单个瓦片  ");

        try {
            // 获取瓦片图像
            Image tile = getTile(x, y, zoom);
            if (tile == null) {
                return; // 瓦片加载失败
            }
            
            // 计算瓦片在画布上的位置
            double tileX = calculateTileScreenX(x, zoom, canvasWidth);
            double tileY = calculateTileScreenY(y, zoom, canvasHeight);
            
            // 绘制瓦片
            gc.drawImage(tile, tileX, tileY, TILE_SIZE, TILE_SIZE);
            
        } catch (Exception e) {
            LogUtils.error("绘制瓦片失败", e);
        }
    }
    
    /**
     * 获取瓦片（带缓存）
     */
    private Image getTile(int x, int y, int zoom) {
        LogUtils.info("TileService-getTile-获取瓦片（带缓存）");

        String cacheKey = String.format("%d_%d_%d", zoom, x, y);
        
        // 检查缓存
        Image cached = tileCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 异步加载瓦片
        loadTileAsync(x, y, zoom, cacheKey);
        
        // 返回占位符或null
        return createPlaceholderTile();
    }
    
    /**
     * 异步加载瓦片
     */
    private void loadTileAsync(int x, int y, int zoom, String cacheKey) {
        LogUtils.info("TileService-loadTileAsync-异步加载瓦片");

        tileLoader.submit(() -> {
            try {
                String url = currentSource.getTileUrl(x, y, zoom);
                if (url == null || url.isEmpty()) {
                    return;
                }
                
                Image tile = new Image(url, true); // 后台加载
                tileCache.put(cacheKey, tile);
                
                LogUtils.debug("加载瓦片: " + cacheKey);
                
            } catch (Exception e) {
                LogUtils.error("加载瓦片失败: " + cacheKey, e);
            }
        });
    }
    
    /**
     * 创建占位符瓦片
     */
    private Image createPlaceholderTile() {
        LogUtils.info("TileService-createPlaceholderTile-创建占位符瓦片");

        // 创建一个简单的占位符
        // 实际实现中可以创建一个带有网格的占位符图像
        return null;
    }
    
    /**
     * 计算瓦片在屏幕上的X坐标
     */
    private double calculateTileScreenX(int tileX, int zoom, double canvasWidth) {
        LogUtils.info("TileService-calculateTileScreenX-计算瓦片在屏幕上的X坐标");

        // 计算地图总宽度（像素）
        double mapWidth = Math.pow(2, zoom) * TILE_SIZE;
        
        // 计算瓦片相对于地图中心的偏移
        double tileCenterX = (tileX + 0.5) * TILE_SIZE;
        double mapCenterX = mapWidth / 2;
        
        // 转换为屏幕坐标
        return canvasWidth / 2 + (tileCenterX - mapCenterX);
    }
    
    /**
     * 计算瓦片在屏幕上的Y坐标
     */
    private double calculateTileScreenY(int tileY, int zoom, double canvasHeight) {
        LogUtils.info("TileService-calculateTileScreenY-计算瓦片在屏幕上的Y坐标");

        double mapHeight = Math.pow(2, zoom) * TILE_SIZE;
        double tileCenterY = (tileY + 0.5) * TILE_SIZE;
        double mapCenterY = mapHeight / 2;
        
        return canvasHeight / 2 + (tileCenterY - mapCenterY);
    }
    
    /**
     * 经度转瓦片X坐标
     */
    private int lonToTileX(double lon, int zoom) {
        LogUtils.info("TileService-lonToTileX-经度转瓦片X坐标");

        double x = (lon + 180.0) / 360.0 * Math.pow(2, zoom);
        return (int) Math.floor(x);
    }
    
    /**
     * 纬度转瓦片Y坐标
     */
    private int latToTileY(double lat, int zoom) {
        LogUtils.info("TileService-latToTileY-纬度转瓦片Y坐标");

        double latRad = Math.toRadians(lat);
        double y = (1.0 - Math.log(Math.tan(latRad) + 1.0 / Math.cos(latRad)) / Math.PI) / 2.0 * Math.pow(2, zoom);
        return (int) Math.floor(y);
    }
    
    /**
     * 设置瓦片源
     */
    public void setTileSource(TileSource source) {
        LogUtils.info("TileService-setTileSource-设置瓦片源");

        this.currentSource = source;
        clearCache(); // 切换源时清空缓存
    }
    
    /**
     * 清空瓦片缓存
     */
    public void clearCache() {
        LogUtils.info("TileService-clearCache-清空瓦片缓存");

        tileCache.clear();
        LogUtils.info("瓦片缓存已清空");
    }
    
    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        LogUtils.info("TileService-getCacheSize-获取缓存大小"+tileCache.size());

        return tileCache.size();
    }
    
    /**
     * 关闭服务
     */
    public void shutdown() {
        LogUtils.info("TileService-shutdown-关闭服务");

        tileLoader.shutdown();
        try {
            if (!tileLoader.awaitTermination(5, TimeUnit.SECONDS)) {
                tileLoader.shutdownNow();
            }
        } catch (InterruptedException e) {
            tileLoader.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
