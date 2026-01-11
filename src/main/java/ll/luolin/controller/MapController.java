// controller/MapController.java - 修改部分
package ll.luolin.controller;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ll.luolin.model.LayerModel;
import ll.luolin.service.ShpService;
import ll.luolin.utils.LogUtils;
import ll.luolin.view.MapCanvas;

import java.io.File;
import java.util.List;

/**
 * 地图控制器
 * 负责协调视图和模型之间的交互
 */
public class MapController {

    // 使用SimpleListProperty来包装ObservableList
    private final ListProperty<LayerModel> layersProperty = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final StringProperty coordinate = new SimpleStringProperty("0.0000°E, 0.0000°N");
    private final IntegerProperty zoomLevel = new SimpleIntegerProperty(5);

    // 视图组件
    private MapCanvas mapCanvas;

    // 服务
    private final ShpService shpService = ShpService.getInstance();

    public MapController() {
        // 初始化
        LogUtils.info("MapController-MapController-初始化");
    }

    public void setMapCanvas(MapCanvas canvas) {
        this.mapCanvas = canvas;
    }

    /**
     * 加载SHP文件
     */
    public void loadShpFile(Stage stage) {
        LogUtils.info("MapController-loadShpFile-加载SHP文件");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择SHP文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Shapefile (*.shp)", "*.shp"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                // 验证SHP文件
                if (!validateShpFile(selectedFile)) {
                    showError("文件不完整", "请确保同时存在.shp、.shx和.dbf文件");
                    return;
                }

                // 加载图层
                LayerModel layer = shpService.loadShpFile(selectedFile);

                // 添加到列表和地图
                layersProperty.add(layer);
                mapCanvas.addLayer(layer);

                // 自动调整视图到图层范围
                mapCanvas.fitToLayers();

                showInfo("加载成功", "成功加载SHP文件: " + selectedFile.getName());

            } catch (Exception e) {
                e.printStackTrace();
                showError("加载失败", "错误信息: " + e.getMessage());
            }
        }

        LogUtils.info("MapController-loadShpFile-加载SHP文件");
    }

    /**
     * 批量加载SHP文件
     */
    public void loadShpFiles(List<File> files) {
        LogUtils.info("MapController-loadShpFiles-批量加载SHP文件");
        for (File file : files) {
            try {
                LayerModel layer = shpService.loadShpFile(file);
                layersProperty.add(layer);
                mapCanvas.addLayer(layer);
            } catch (Exception e) {
                System.err.println("加载文件失败: " + file.getName() + " - " + e.getMessage());
            }
        }

        if (!files.isEmpty()) {
            mapCanvas.fitToLayers();
        }
        LogUtils.info("MapController-loadShpFiles-批量加载SHP文件");
    }

    /**
     * 移除图层
     */
    public void removeLayer(LayerModel layer) {
        LogUtils.info("MapController-removeLayer-移除图层");
        if (layer != null) {
            layersProperty.remove(layer);
            mapCanvas.removeLayer(layer);
        }
        LogUtils.info("MapController-removeLayer-移除图层");

    }

    /**
     * 清除所有图层
     */
    public void clearLayers() {
        LogUtils.info("MapController-clearLayers-清除所有图层");
        layersProperty.clear();
        mapCanvas.clearLayers();
        LogUtils.info("MapController-clearLayers-清除所有图层");

    }

    /**
     * 导出地图
     */
    public void exportMap(Stage stage) {
        LogUtils.info("MapController-exportMap-导出地图");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出地图图片");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG图片 (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("JPEG图片 (*.jpg)", "*.jpg")
        );

        File outputFile = fileChooser.showSaveDialog(stage);
        if (outputFile != null) {
            try {
                mapCanvas.exportToImage(outputFile.getAbsolutePath());
                showInfo("导出成功", "地图已导出到: " + outputFile.getAbsolutePath());
            } catch (Exception e) {
                showError("导出失败", "错误信息: " + e.getMessage());
            }
        }
        LogUtils.info("MapController-exportMap-导出地图");
    }

    /**
     * 缩放到图层
     */
    public void zoomToLayer(LayerModel layer) {
        LogUtils.info("MapController-zoomToLayer-缩放到图层");

        if (layer != null && mapCanvas != null) {
            // 这里需要实现具体的缩放逻辑
            System.out.println("缩放到图层: " + layer.getName());
            // 临时实现：将地图中心设置为图层的中心
            // 实际应该计算图层的包围盒
            mapCanvas.setMapCenter(104.0, 35.0);
        }
        LogUtils.info("MapController-zoomToLayer-缩放到图层");

    }

    /**
     * 设置网格可见性
     */
    public void setGridVisible(boolean visible) {
        LogUtils.info("MapController-setGridVisible-设置网格可见性");

        if (mapCanvas != null) {
            // 这里需要通过MapModel设置，暂时留空
        }
        LogUtils.info("MapController-setGridVisible-设置网格可见性");

    }

    /**
     * 设置调试模式
     */
    public void setDebugMode(boolean debug) {
        LogUtils.info("MapController-setDebugMode-设置调试模式");

        if (mapCanvas != null) {
            // 这里需要通过MapModel设置，暂时留空
        }
        LogUtils.info("MapController-setDebugMode-设置调试模式");

    }

    /**
     * 添加图层（通过对话框）
     */
    public void addLayer() {
        LogUtils.info("MapController-addLayer-添加图层（通过对话框）");


        // 打开文件选择对话框
        // 实际实现中需要Stage参数
    }

    /**
     * 验证SHP文件完整性
     */
    private boolean validateShpFile(File shpFile) {
        LogUtils.info("MapController-validateShpFile-验证SHP文件完整性");

        if (!shpFile.exists()) {
            return false;
        }

        String baseName = shpFile.getName().replace(".shp", "");
        File dir = shpFile.getParentFile();

        // 检查必需的辅助文件
        String[] requiredExtensions = {".shx", ".dbf"};
        for (String ext : requiredExtensions) {
            File auxFile = new File(dir, baseName + ext);
            if (!auxFile.exists()) {
                return false;
            }
        }
        LogUtils.info("MapController-validateShpFile-验证SHP文件完整性");
        return true;


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

    // 属性访问器 - 修复这里
    public ObservableList<LayerModel> getLayers() {

        return layersProperty.get();
    }

    public ListProperty<LayerModel> layersProperty() {
        return layersProperty;
    }

    public String getCoordinate() {
        return coordinate.get();
    }

    public void setCoordinate(String coord) {
        coordinate.set(coord);
    }

    public StringProperty coordinateProperty() {
        return coordinate;
    }

    public int getZoomLevel() {
        return zoomLevel.get();
    }

    public void setZoomLevel(int level) {
        zoomLevel.set(level);
    }

    public IntegerProperty zoomLevelProperty() {
        return zoomLevel;
    }
}
