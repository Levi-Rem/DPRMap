// view/NavPointTooltip.java
package ll.luolin.view;

import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import ll.luolin.controller.NavPointController;
import ll.luolin.model.ASFModel.NavPointModel;
import ll.luolin.utils.CoordinateFormatter;
import ll.luolin.utils.LogUtils;

/**
 * 导航点工具提示
 */
public class NavPointTooltip {
    
    /**
     * 为导航点创建工具提示
     */
    public static Tooltip createForNavPoint(NavPointModel point) {
        LogUtils.info("NavPointTooltip-createForNavPoint-为导航点创建工具提示");

        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(javafx.util.Duration.millis(500));
        tooltip.setHideDelay(javafx.util.Duration.millis(200));
        
        // 创建自定义内容
        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-background-color: white; -fx-border-color: gray;");
        
        // 名称
        Label nameLabel = new Label("名称: " + point.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // 类型
        Label typeLabel = new Label("类型: " + point.getType());
        
        // 坐标（十进制）
        Label coordLabel = new Label(String.format("坐标: %.6f°E, %.6f°N", 
            point.getLongitude(), point.getLatitude()));
        
        // 坐标（度分秒）
        String dms = CoordinateFormatter.formatToDMS(
            point.getLongitude(), point.getLatitude());
        Label dmsLabel = new Label("度分秒: " + dms);
        
        content.getChildren().addAll(nameLabel, typeLabel, coordLabel, dmsLabel);
        
        // 设置图形内容
        tooltip.setGraphic(content);
        
        return tooltip;
    }
    
    /**
     * 为地图画布添加工具提示支持
     */
    public static void setupTooltips(MapCanvas canvas, NavPointController controller) {
        canvas.setOnMouseMoved(event -> {
            LogUtils.info("NavPointTooltip-setupTooltips-为地图画布添加工具提示支持");

            if (controller == null) return;
            
            // 将像素坐标转换为经纬度
            double lon = canvas.getMapModel().pixelToLon(event.getX());
            double lat = canvas.getMapModel().pixelToLat(event.getY());
            
            // 查找附近的导航点
            NavPointModel nearbyPoint = controller.getNavPointAt(lon, lat, 0.1);
            
            if (nearbyPoint != null) {
                // 显示工具提示
                Tooltip tooltip = createForNavPoint(nearbyPoint);
                Tooltip.install(canvas, tooltip);
                
                // 临时显示，2秒后自动隐藏
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            Tooltip.uninstall(canvas, tooltip);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            }
        });
    }
}
