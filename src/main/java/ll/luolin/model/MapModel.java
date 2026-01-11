// model/MapModel.java - 添加鼠标坐标属性
package ll.luolin.model;

import javafx.beans.property.*;
import ll.luolin.utils.LogUtils;

/**
 * 地图数据模型
 */
public class MapModel {
    private final DoubleProperty centerX = new SimpleDoubleProperty(104.0); // 经度
    private final DoubleProperty centerY = new SimpleDoubleProperty(35.0);  // 纬度
    private final IntegerProperty zoomLevel = new SimpleIntegerProperty(5) {
        @Override
        public void set(int value) {
            // 限制在范围内
            int clampedValue = clampZoomLevel(value);
            super.set(clampedValue);

        }
    };
    private final BooleanProperty gridVisible = new SimpleBooleanProperty(true);
    private final BooleanProperty debugMode = new SimpleBooleanProperty(false);

    // 添加鼠标坐标属性
    private final DoubleProperty mouseLon = new SimpleDoubleProperty(0.0);
    private final DoubleProperty mouseLat = new SimpleDoubleProperty(0.0);
    private final BooleanProperty mouseInside = new SimpleBooleanProperty(false);

    // 瓦片相关
    private static final int TILE_SIZE = 256;
    private static final double EARTH_RADIUS = 6378137.0;


    // 添加缩放范围常量
    private static final int MIN_ZOOM = 7;
    private static final int MAX_ZOOM = 13;

    public MapModel() {
        // 初始化
    }

    /**
     * 限制缩放级别在有效范围内
     */
    private int clampZoomLevel(int level) {
        if (level < MIN_ZOOM) return MIN_ZOOM;
        if (level > MAX_ZOOM) return MAX_ZOOM;
        return level;
    }

    // 坐标转换方法
    public double pixelToLon(double px) {
        LogUtils.info("MapModel-pixelToLon-坐标转换方法");

        double mapWidth = Math.pow(2, zoomLevel.get()) * TILE_SIZE;
        double lonPerPixel = 360.0 / mapWidth;
        return centerX.get() + (px - getViewportWidth() / 2) * lonPerPixel;
    }

    public double pixelToLat(double py) {
        LogUtils.info("MapModel-pixelToLat-坐标转换方法");

        double mapHeight = Math.pow(2, zoomLevel.get()) * TILE_SIZE;
        double latPerPixel = 180.0 / mapHeight;
        return centerY.get() - (py - getViewportHeight() / 2) * latPerPixel;
    }

    // 更新鼠标坐标
    public void updateMousePosition(double mouseX, double mouseY, boolean inside) {
        LogUtils.info("MapModel-updateMousePosition-更新鼠标坐标");

        if (inside) {
            mouseLon.set(pixelToLon(mouseX));
            mouseLat.set(pixelToLat(mouseY));
        }
        mouseInside.set(inside);
    }

    public void pan(double dx, double dy) {
        LogUtils.info("MapModel-pan-pan");

        double lonPerPixel = 360.0 / (Math.pow(2, zoomLevel.get()) * TILE_SIZE);
        double latPerPixel = 180.0 / (Math.pow(2, zoomLevel.get()) * TILE_SIZE);

        centerX.set(centerX.get() - dx * lonPerPixel);
        centerY.set(centerY.get() + dy * latPerPixel);
    }

    public void zoomIn(double mouseX, double mouseY) {
        LogUtils.info("MapModel-zoomIn-pan");

        if (zoomLevel.get() < MAX_ZOOM) {
            // 保存鼠标位置的经纬度
            double mouseLon = pixelToLon(mouseX);
            double mouseLat = pixelToLat(mouseY);

            // 增加缩放级别
            zoomLevel.set(zoomLevel.get() + 1);

            // 调整中心点使鼠标位置保持不动
            adjustCenterAfterZoom(mouseLon, mouseLat, mouseX, mouseY);
        }
    }

    public void zoomOut(double mouseX, double mouseY) {
        LogUtils.info("MapModel-zoomIn-zoomOut");
        if (zoomLevel.get() > MIN_ZOOM) {
            double mouseLon = pixelToLon(mouseX);
            double mouseLat = pixelToLat(mouseY);

            zoomLevel.set(zoomLevel.get() - 1);
            adjustCenterAfterZoom(mouseLon, mouseLat, mouseX, mouseY);
        }
    }


    private void adjustCenterAfterZoom(double mouseLon, double mouseLat,
                                       double mouseX, double mouseY) {
        LogUtils.info("MapModel-adjustCenterAfterZoom-调整中心点");

        // 计算新的像素位置
        double newMouseX = lonToPixelX(mouseLon);
        double newMouseY = latToPixelY(mouseLat);

        double dx = newMouseX - mouseX;
        double dy = newMouseY - mouseY;

        double lonPerPixel = 360.0 / (Math.pow(2, zoomLevel.get()) * TILE_SIZE);
        double latPerPixel = 180.0 / (Math.pow(2, zoomLevel.get()) * TILE_SIZE);

        centerX.set(centerX.get() - dx * lonPerPixel);
        centerY.set(centerY.get() + dy * latPerPixel);
        LogUtils.info("MapModel-adjustCenterAfterZoom-调整中心点");

    }

    /**
     * 直接设置缩放级别（带范围检查）
     */
    public void setZoomLevel(int level) {
        zoomLevel.set(clampZoomLevel(level));
    }

    /**
     * 获取最小缩放级别
     */
    public int getMinZoomLevel() {
        return MIN_ZOOM;
    }

    /**
     * 获取最大缩放级别
     */
    public int getMaxZoomLevel() {
        return MAX_ZOOM;
    }

    /**
     * 检查是否在最小缩放级别
     */
    public boolean isAtMinZoom() {
        return zoomLevel.get() == MIN_ZOOM;
    }

    /**
     * 检查是否在最大缩放级别
     */
    public boolean isAtMaxZoom() {
        return zoomLevel.get() == MAX_ZOOM;
    }


    private double lonToPixelX(double lon) {
        LogUtils.info("MapModel-lonToPixelX-lonToPixelX");

        double mapWidth = Math.pow(2, zoomLevel.get()) * TILE_SIZE;
        double lonPerPixel = 360.0 / mapWidth;
        return (lon - centerX.get()) / lonPerPixel + getViewportWidth() / 2;
    }

    private double latToPixelY(double lat) {
        LogUtils.info("MapModel-latToPixelY-latToPixelY");

        double mapHeight = Math.pow(2, zoomLevel.get()) * TILE_SIZE;
        double latPerPixel = 180.0 / mapHeight;
        return (centerY.get() - lat) / latPerPixel + getViewportHeight() / 2;
    }

    private double getViewportWidth() {
        return 1200; // 应从画布获取
    }

    private double getViewportHeight() {
        return 800; // 应从画布获取
    }

    // Getter/Setter方法
    public double getCenterX() {
        return centerX.get();
    }

    public void setCenterX(double x) {
        centerX.set(x);
    }

    public DoubleProperty centerXProperty() {
        return centerX;
    }

    public double getCenterY() {
        return centerY.get();
    }

    public void setCenterY(double y) {
        centerY.set(y);
    }

    public DoubleProperty centerYProperty() {
        return centerY;
    }

    public int getZoomLevel() {
        return zoomLevel.get();
    }



    public IntegerProperty zoomLevelProperty() {
        return zoomLevel;
    }

    public boolean isGridVisible() {
        return gridVisible.get();
    }

    public void setGridVisible(boolean visible) {
        gridVisible.set(visible);
    }

    public BooleanProperty gridVisibleProperty() {
        return gridVisible;
    }

    public boolean isDebugMode() {
        return debugMode.get();
    }

    public void setDebugMode(boolean debug) {
        debugMode.set(debug);
    }

    public BooleanProperty debugModeProperty() {
        return debugMode;
    }

    // 鼠标坐标的getter
    public double getMouseLon() {
        return mouseLon.get();
    }

    public DoubleProperty mouseLonProperty() {
        return mouseLon;
    }

    public double getMouseLat() {
        return mouseLat.get();
    }

    public DoubleProperty mouseLatProperty() {
        return mouseLat;
    }

    public boolean isMouseInside() {
        return mouseInside.get();
    }

    public BooleanProperty mouseInsideProperty() {
        return mouseInside;
    }

    public void setCenter(double lon, double lat) {
        centerX.set(lon);
        centerY.set(lat);
    }
}
