// controller/NavPointController.java
package ll.luolin.controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ll.luolin.model.NavPointLayerModel;
import ll.luolin.model.NavPointModel;
import ll.luolin.service.AsfService;
import ll.luolin.utils.LogUtils;
import ll.luolin.view.MapCanvas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 导航点控制器
 */
public class NavPointController {

    private final ObservableList<NavPointLayerModel> navPointLayers =
            FXCollections.observableArrayList();

    private MapCanvas mapCanvas;
    private final AsfService asfService = AsfService.getInstance();

    public NavPointController() {
        // 初始化
    }

    public void setMapCanvas(MapCanvas canvas) {
        this.mapCanvas = canvas;
    }

    /**
     * 加载ASF文件
     */
    public void loadAsfFile(Stage stage) {
        LogUtils.info("MapController-loadAsfFile-加载ASF文件");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择ASF导航点文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ASF文件 (*.asf, *.txt)", "*.asf", "*.txt"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                // 解析ASF文件
                List<NavPointModel> points = asfService.parseAsfFile(selectedFile);

                if (points.isEmpty()) {
                    showWarning("文件内容为空", "未找到有效的导航点数据");
                    return;
                }

                // 创建导航点图层
                NavPointLayerModel layer = new NavPointLayerModel(
                        selectedFile.getName().replace(".asf", "").replace(".txt", ""),
                        selectedFile
                );
                layer.addNavPoints(points);

                // 添加到控制器和地图
                navPointLayers.add(layer);
                if (mapCanvas != null) {
                    mapCanvas.addNavPointLayer(layer);
                }

                showInfo("加载成功",
                        String.format("成功加载 %d 个导航点", points.size()));

            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.info("加载失败,错误信息: " + e.getMessage());

                showError("加载失败", "错误信息: " + e.getMessage());
            }
        }
        LogUtils.info("MapController-loadAsfFile-加载ASF文件");

    }

    /**
     * 移除导航点图层
     */
    public void removeNavPointLayer(NavPointLayerModel layer) {
        LogUtils.info("MapController-removeNavPointLayer-移除导航点图层");

        if (layer != null) {
            navPointLayers.remove(layer);
            if (mapCanvas != null) {
                mapCanvas.removeNavPointLayer(layer);
            }
        }
        LogUtils.info("MapController-removeNavPointLayer-移除导航点图层");

    }

    /**
     * 清除所有导航点图层
     */
    public void clearNavPointLayers() {
        LogUtils.info("MapController-clearNavPointLayers-清除所有导航点图层");

        navPointLayers.clear();
        if (mapCanvas != null) {
            mapCanvas.clearNavPointLayers();
        }
        LogUtils.info("MapController-clearNavPointLayers-清除所有导航点图层");
    }

    /**
     * 获取指定位置的导航点
     */
    public NavPointModel getNavPointAt(double lon, double lat, double tolerance) {
        LogUtils.info("MapController-getNavPointAt-获取指定位置的导航点");

        for (NavPointLayerModel layer : navPointLayers) {
            if (!layer.isVisible()) continue;

            for (NavPointModel point : layer.getNavPoints()) {
                if (!point.isVisible()) continue;

                double distance = calculateDistance(
                        point.getLongitude(), point.getLatitude(), lon, lat);

                if (distance <= tolerance) {
                    return point;
                }
            }
        }
        LogUtils.info("MapController-getNavPointAt-获取指定位置的导航点");
        //todo   返回值为NULL  待修改
        return null;
    }

    /**
     * 计算两点间距离（简化版）
     */
    private double calculateDistance(double lon1, double lat1, double lon2, double lat2) {
        LogUtils.info("MapController-calculateDistance-计算两点间距离（简化版）");

        double dx = lon1 - lon2;
        double dy = lat1 - lat2;
        LogUtils.info("MapController-calculateDistance-计算两点间距离（简化版）");

        return Math.sqrt(dx * dx + dy * dy);

    }

    /**
     * 搜索导航点
     */
    public List<NavPointModel> searchNavPoints(String keyword) {
        LogUtils.info("MapController-searchNavPoints-搜索导航点");

        List<NavPointModel> results = new ArrayList<>();
        String searchTerm = keyword.toLowerCase();

        for (NavPointLayerModel layer : navPointLayers) {
            for (NavPointModel point : layer.getNavPoints()) {
                if (point.getName().toLowerCase().contains(searchTerm) ||
                        point.getType().toLowerCase().contains(searchTerm)) {
                    results.add(point);
                }
            }
        }
        LogUtils.info("MapController-searchNavPoints-搜索导航点");
        return results;
    }

    /**
     * 导出导航点数据
     */
    public void exportNavPoints(Stage stage, NavPointLayerModel layer) {
        LogUtils.info("MapController-exportNavPoints-导出导航点数据");

        if (layer == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出导航点数据");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV文件 (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("文本文件 (*.txt)", "*.txt")
        );

        File outputFile = fileChooser.showSaveDialog(stage);
        if (outputFile != null) {
            try {
                exportToCsv(layer, outputFile);
                showInfo("导出成功", "导航点数据已导出到: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                showError("导出失败", "错误信息: " + e.getMessage());
            }
        }
        LogUtils.info("MapController-exportNavPoints-导出导航点数据");

    }

    /**
     * 导出为CSV格式
     */
    private void exportToCsv(NavPointLayerModel layer, File outputFile) throws Exception {
        LogUtils.info("MapController-exportToCsv-导出为CSV格式");

        try (java.io.PrintWriter writer = new java.io.PrintWriter(outputFile, "UTF-8")) {
            // 写入表头
            writer.println("Name,Longitude,Latitude,Type,Visible");

            // 写入数据
            for (NavPointModel point : layer.getNavPoints()) {
                writer.printf("%s,%.6f,%.6f,%s,%s%n",
                        point.getName(),
                        point.getLongitude(),
                        point.getLatitude(),
                        point.getType(),
                        point.isVisible()
                );
            }
        }
        LogUtils.info("MapController-exportToCsv-导出为CSV格式");
    }

    /**
     * 显示信息对话框
     */
    private void showInfo(String title, String message) {
        LogUtils.info("MapController-showInfo-显示信息对话框");
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        LogUtils.info("MapController-showInfo-显示信息对话框");

    }

    /**
     * 显示警告对话框
     */
    private void showWarning(String title, String message) {
        LogUtils.info("MapController-showWarning-显示警告对话框");

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.WARNING
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        LogUtils.info("MapController-showWarning-显示警告对话框");

    }

    /**
     * 显示错误对话框
     */
    private void showError(String title, String message) {
        LogUtils.info("MapController-showError-显示错误对话框");

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        LogUtils.info("MapController-showError-显示错误对话框");

    }

    // 属性访问器
    public ObservableList<NavPointLayerModel> getNavPointLayers() {
        return navPointLayers;
    }

    public ObservableList<NavPointLayerModel> navPointLayersProperty() {
        return navPointLayers;
    }
}
