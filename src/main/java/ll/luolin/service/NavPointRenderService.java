// service/NavPointRenderService.java
package ll.luolin.service;

import javafx.scene.canvas.GraphicsContext;
import ll.luolin.model.MapModel;
import ll.luolin.model.NavPointLayerModel;
import ll.luolin.model.ASFModel.NavPointModel;
import ll.luolin.utils.LogUtils;
import java.util.List;

/**
 * 导航点渲染服务
 */
public class NavPointRenderService {
    private static NavPointRenderService instance;

    // 图标大小
    private static final int ICON_SIZE = 16;

    // 颜色定义
    private static final javafx.scene.paint.Color AIRPORT_COLOR = javafx.scene.paint.Color.RED;
    private static final javafx.scene.paint.Color VOR_COLOR = javafx.scene.paint.Color.BLUE;
    private static final javafx.scene.paint.Color NDB_COLOR = javafx.scene.paint.Color.GREEN;
    private static final javafx.scene.paint.Color REPORT_COLOR = javafx.scene.paint.Color.ORANGE;
    private static final javafx.scene.paint.Color DEFAULT_COLOR = javafx.scene.paint.Color.PURPLE;
    private static final javafx.scene.paint.Color LABEL_COLOR = javafx.scene.paint.Color.BLACK;
    private static final javafx.scene.paint.Color BORDER_COLOR = javafx.scene.paint.Color.BLACK;

    private NavPointRenderService() {}

    public static synchronized NavPointRenderService getInstance() {
        if (instance == null) {
            instance = new NavPointRenderService();
        }
        return instance;
    }

    /**
     *      * 渲染导航点图层
     */
    public void renderNavPointLayers(GraphicsContext gc, List<NavPointLayerModel> layers, MapModel mapModel) {
        LogUtils.info("NavPointRenderService-renderNavPointLayers-渲染导航点图层");

        if (layers == null || layers.isEmpty()) {
            return;
        }

        for (NavPointLayerModel layer : layers) {
            if (layer.isVisible()) {
                renderNavPointLayer(gc, layer, mapModel);
            }
        }
    }

    /**
     * 渲染单个导航点图层
     */
    private void renderNavPointLayer(GraphicsContext gc, NavPointLayerModel layer, MapModel mapModel) {
        LogUtils.info("NavPointRenderService-renderNavPointLayer-渲染单个导航点图层");

        try {
            List<NavPointModel> visiblePoints = layer.getVisibleNavPoints();
            if (visiblePoints.isEmpty()) {
                return;
            }

            // 按类型分组渲染
            renderPointsByType(gc, visiblePoints, mapModel, "AIRPORT");
            renderPointsByType(gc, visiblePoints, mapModel, "VOR");
            renderPointsByType(gc, visiblePoints, mapModel, "NDB");
            renderPointsByType(gc, visiblePoints, mapModel, "REPORT");
            renderOtherPoints(gc, visiblePoints, mapModel);

        } catch (Exception e) {
            LogUtils.error("渲染导航点图层失败: " + layer.getName(), e);
        }
    }

    /**
     * 按类型渲染点
     */
    private void renderPointsByType(GraphicsContext gc, List<NavPointModel> points,
                                    MapModel mapModel, String typeFilter) {
        LogUtils.info("NavPointRenderService-renderPointsByType-按类型渲染点");

        for (NavPointModel point : points) {
            if (!point.isVisible()) continue;

            String pointType = point.getType().toUpperCase();
            if (pointType.contains(typeFilter)) {
                renderSinglePoint(gc, point, mapModel);
            }
        }
    }

    /**
     * 渲染其他类型的点
     */
    private void renderOtherPoints(GraphicsContext gc, List<NavPointModel> points, MapModel mapModel) {
        LogUtils.info("NavPointRenderService-renderOtherPoints-渲染其他类型的点");

        for (NavPointModel point : points) {
            if (!point.isVisible()) continue;

            String pointType = point.getType().toUpperCase();
            if (!pointType.contains("AIRPORT") &&
                    !pointType.contains("VOR") &&
                    !pointType.contains("NDB") &&
                    !pointType.contains("REPORT")) {
                renderSinglePoint(gc, point, mapModel);
            }
        }
    }

    /**
     * 渲染单个导航点
     */
    private void renderSinglePoint(GraphicsContext gc, NavPointModel point, MapModel mapModel) {
        LogUtils.info("NavPointRenderService-renderSinglePoint-渲染单个导航点");

        try {
            // 将经纬度转换为像素坐标
            double pixelX = lonToPixelX(point.getLongitude(), mapModel);
            double pixelY = latToPixelY(point.getLatitude(), mapModel);

            // 检查点是否在可见范围内
            if (!isPointVisible(pixelX, pixelY, gc.getCanvas().getWidth(), gc.getCanvas().getHeight())) {
                return;
            }

            // 获取点的颜色
            javafx.scene.paint.Color pointColor = getColorForType(point.getType());

            // 绘制点
            drawPoint(gc, point, pixelX, pixelY, pointColor);

            // 绘制标签（如果缩放级别足够大）
            if (mapModel.getZoomLevel() > 7) {
                drawPointLabel(gc, point, pixelX, pixelY);
            }

        } catch (Exception e) {
            LogUtils.error("渲染导航点失败: " + point.getName(), e);
        }
    }

    /**
     * 绘制点
     */
    private void drawPoint(GraphicsContext gc, NavPointModel point, double x, double y,
                           javafx.scene.paint.Color color) {
        LogUtils.info("NavPointRenderService-drawPoint-绘制点");

        // 绘制圆形点
        double radius = 4;

        // 填充
        gc.setFill(color);
        gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // 边框
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(1);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // 如果是重要点（如机场），绘制更大的标记
        String type = point.getType().toUpperCase();
        if (type.contains("AIRPORT")) {
            drawAirportMarker(gc, x, y);
        } else if (type.contains("VOR")) {
            drawVorMarker(gc, x, y);
        }
    }

    /**
     * 绘制机场标记
     */
    private void drawAirportMarker(GraphicsContext gc, double x, double y) {
        LogUtils.info("NavPointRenderService-drawAirportMarker-绘制机场标记");

        // 绘制飞机形状的简化标记
        double size = 8;

        // 机身
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(2);
        gc.strokeLine(x - size, y, x + size, y);

        // 机翼
        gc.strokeLine(x, y - size/2, x, y + size/2);

        // 机尾
        gc.strokeLine(x + size/2, y - size/4, x + size, y);
        gc.strokeLine(x + size/2, y + size/4, x + size, y);
    }

    /**
     * 绘制VOR标记
     */
    private void drawVorMarker(GraphicsContext gc, double x, double y) {
        LogUtils.info("NavPointRenderService-drawVorMarker-绘制VOR标记");

        // 绘制VOR的简化标记（带方向的圆圈）
        double radius = 6;

        // 圆圈
        gc.setStroke(BORDER_COLOR);
        gc.setLineWidth(1);
        gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);

        // 方向线
        gc.strokeLine(x, y - radius, x, y - radius - 4);
        gc.strokeLine(x + radius, y, x + radius + 4, y);
        gc.strokeLine(x, y + radius, x, y + radius + 4);
        gc.strokeLine(x - radius, y, x - radius - 4, y);
    }

    /**
     * 绘制点标签
     */
    private void drawPointLabel(GraphicsContext gc, NavPointModel point, double x, double y) {
        LogUtils.info("NavPointRenderService-drawPointLabel-绘制点标签");

        gc.setFill(LABEL_COLOR);
        gc.setFont(javafx.scene.text.Font.font("Arial", 10));

        // 绘制名称
        String label = point.getName();
        gc.fillText(label, x + 8, y - 4);

        // 如果缩放级别更大，绘制类型
        // if (mapModel.getZoomLevel() > 10) {
        //     gc.fillText(point.getType(), x + 8, y + 10);
        // }
    }

    /**
     * 根据类型获取颜色
     */
    private javafx.scene.paint.Color getColorForType(String type) {
        LogUtils.info("NavPointRenderService-getColorForType-根据类型获取颜色");

        if (type == null) {
            return DEFAULT_COLOR;
        }

        String upperType = type.toUpperCase();
        if (upperType.contains("AIRPORT")) {
            return AIRPORT_COLOR;
        } else if (upperType.contains("VOR")) {
            return VOR_COLOR;
        } else if (upperType.contains("NDB")) {
            return NDB_COLOR;
        } else if (upperType.contains("REPORT")) {
            return REPORT_COLOR;
        } else {
            return DEFAULT_COLOR;
        }
    }

    /**
     * 经度转像素X坐标
     */
    private double lonToPixelX(double lon, MapModel mapModel) {
        LogUtils.info("NavPointRenderService-lonToPixelX-经度转像素X坐标");

        double canvasWidth = 1200; // 默认画布宽度
        double mapWidth = Math.pow(2, mapModel.getZoomLevel()) * 256;
        double lonPerPixel = 360.0 / mapWidth;
        return (lon - mapModel.getCenterX()) / lonPerPixel + canvasWidth / 2;
    }

    /**
     * 纬度转像素Y坐标
     */
    private double latToPixelY(double lat, MapModel mapModel) {
        LogUtils.info("NavPointRenderService-latToPixelY-纬度转像素Y坐标");

        double canvasHeight = 800; // 默认画布高度
        double mapHeight = Math.pow(2, mapModel.getZoomLevel()) * 256;
        double latPerPixel = 180.0 / mapHeight;
        return (mapModel.getCenterY() - lat) / latPerPixel + canvasHeight / 2;
    }

    /**
     * 检查点是否在可见范围内
     */
    private boolean isPointVisible(double x, double y, double width, double height) {
        LogUtils.info("NavPointRenderService-isPointVisible-检查点是否在可见范围内");

        // 扩大可见范围，允许点稍微超出画布
        double margin = 50;
        return x >= -margin && x <= width + margin &&
                y >= -margin && y <= height + margin;
    }

    /**
     * 批量渲染所有点（优化版本）
     */
    public void renderAllPoints(GraphicsContext gc, List<NavPointModel> points,
                                MapModel mapModel, double canvasWidth, double canvasHeight) {
        LogUtils.info("NavPointRenderService-renderAllPoints- 批量渲染所有点（优化版本）");

        if (points == null || points.isEmpty()) {
            return;
        }

        // 预计算转换参数
        double mapWidth = Math.pow(2, mapModel.getZoomLevel()) * 256;
        double mapHeight = Math.pow(2, mapModel.getZoomLevel()) * 256;
        double lonPerPixel = 360.0 / mapWidth;
        double latPerPixel = 180.0 / mapHeight;
        double centerX = mapModel.getCenterX();
        double centerY = mapModel.getCenterY();

        for (NavPointModel point : points) {
            if (!point.isVisible()) continue;

            try {
                // 快速坐标转换
                double pixelX = (point.getLongitude() - centerX) / lonPerPixel + canvasWidth / 2;
                double pixelY = (centerY - point.getLatitude()) / latPerPixel + canvasHeight / 2;

                // 快速可见性检查
                if (pixelX >= -50 && pixelX <= canvasWidth + 50 &&
                        pixelY >= -50 && pixelY <= canvasHeight + 50) {

                    // 绘制点
                    javafx.scene.paint.Color color = getColorForType(point.getType());
                    drawPoint(gc, point, pixelX, pixelY, color);

                    // 绘制标签
                    if (mapModel.getZoomLevel() > 7) {
                        drawPointLabel(gc, point, pixelX, pixelY);
                    }
                }

            } catch (Exception e) {
                // 忽略单个点的渲染错误
            }
        }
    }
}
