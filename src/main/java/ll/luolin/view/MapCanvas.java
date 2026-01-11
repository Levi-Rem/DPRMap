// view/MapCanvas.java - 修改鼠标事件处理
package ll.luolin.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.*;
import javafx.animation.AnimationTimer;
import ll.luolin.model.MapModel;
import ll.luolin.model.LayerModel;
import ll.luolin.model.NavPointLayerModel;
import ll.luolin.service.NavPointRenderService;
import ll.luolin.service.TileService;
import ll.luolin.service.RenderService;
import ll.luolin.utils.CoordinateFormatter;
import ll.luolin.utils.LogUtils;
import ll.luolin.controller.MapController;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 统一的地图画布类
 */
public class MapCanvas extends Canvas {

    // 模型
    private final MapModel mapModel;
    private final List<LayerModel> layers = new CopyOnWriteArrayList<>();

    // 服务
    private final TileService tileService;
    private final RenderService renderService;

    // 渲染相关
    private final GraphicsContext gc;
    private AnimationTimer renderTimer;
    private boolean isRendering = false;

    // 交互状态
    private double lastMouseX, lastMouseY;
    private boolean isDragging = false;
    private double scale = 1.0;

    private final ObservableList<NavPointLayerModel> navPointLayers = FXCollections.observableArrayList();
    private final NavPointRenderService navPointRenderService = NavPointRenderService.getInstance();


    public MapCanvas(double width, double height) {

        super(width, height);
        LogUtils.info("MapCanvas-createNdbIcon-初始化MapCanvas width：%d  + height：%d " + width + height);

        this.gc = getGraphicsContext2D();

        // 初始化模型和服务
        this.mapModel = new MapModel();
        this.tileService = TileService.getInstance();
        this.renderService = RenderService.getInstance();

        // 初始化画布
        initializeCanvas();

        // 设置事件监听
        setupEventHandlers();

        // 启动渲染循环
        startRenderLoop();
    }

    private void initializeCanvas() {
        LogUtils.info("MapCanvas-initializeCanvas-初始化initializeCanvas");

        // 设置背景
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // 绘制初始文本
        gc.setFill(Color.BLACK);
        gc.fillText("地图可视化工具", 10, 20);
        gc.fillText("支持SHP、GeoJSON等格式", 10, 40);
    }

    private void setupEventHandlers() {
        LogUtils.info("MapCanvas-setupEventHandlers-setupEventHandlers");

        // 鼠标按下
        setOnMousePressed(this::handleMousePressed);

        // 鼠标拖动
        setOnMouseDragged(this::handleMouseDragged);

        // 鼠标释放
        setOnMouseReleased(this::handleMouseReleased);

        // 鼠标滚轮缩放
        setOnScroll(this::handleScroll);

        // 鼠标移动（显示坐标）
        setOnMouseMoved(this::handleMouseMoved);

        // 鼠标进入
        setOnMouseEntered(this::handleMouseEntered);

        // 鼠标离开
        setOnMouseExited(this::handleMouseExited);

        // 画布大小变化
        widthProperty().addListener((obs, oldVal, newVal) -> redraw());
        heightProperty().addListener((obs, oldVal, newVal) -> redraw());
    }

    private void handleMousePressed(MouseEvent event) {
        LogUtils.info("MapCanvas-handleMousePressed-鼠标按下");

        if (event.getButton() == MouseButton.PRIMARY) {
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            isDragging = true;
        }
    }

    private void handleMouseDragged(MouseEvent event) {
        LogUtils.info("MapCanvas-handleMouseDragged-鼠标拖动");

        if (isDragging) {
            double dx = event.getX() - lastMouseX;
            double dy = event.getY() - lastMouseY;

            // 更新地图中心点
            mapModel.pan(dx, dy);

            lastMouseX = event.getX();
            lastMouseY = event.getY();

            // 更新鼠标坐标
            mapModel.updateMousePosition(event.getX(), event.getY(), true);

            requestRedraw();
        }
    }

    private void handleMouseReleased(MouseEvent event) {
        LogUtils.info("MapCanvas-handleMouseReleased-鼠标释放");

        isDragging = false;
    }

    private void handleScroll(ScrollEvent event) {
        LogUtils.info("MapCanvas-handleScroll-鼠标滚动");


        double delta = event.getDeltaY();
        double mouseX = event.getX();
        double mouseY = event.getY();

        // 检查当前缩放级别
        boolean canZoomIn = !mapModel.isAtMaxZoom();
        boolean canZoomOut = !mapModel.isAtMinZoom();

        if (delta > 0 && canZoomIn) {
            // 放大
            mapModel.zoomIn(mouseX, mouseY);
//            showZoomFeedback("放大", true);
        } else if (delta < 0 && canZoomOut) {
            // 缩小
            mapModel.zoomOut(mouseX, mouseY);
//            showZoomFeedback("缩小", true);
        } else {
            // 达到限制，显示提示
            if (delta > 0) {
//                showZoomFeedback("已到最大缩放级别", false);
            } else {
//                showZoomFeedback("已到最小缩放级别", false);
            }
        }



        // 更新鼠标坐标
        mapModel.updateMousePosition(mouseX, mouseY, true);

        requestRedraw();
        event.consume();
    }


//    /**
//     * 显示缩放反馈
//     */
//    private void showZoomFeedback(String message, boolean success) {
//        // 在状态栏或画布上显示反馈
//        System.out.println(message);
//
//        // 可以在画布上绘制临时提示
//        if (!success) {
//            drawZoomLimitMessage(message);
//        }
//    }

//    /**
//     * 绘制缩放限制提示
//     */
//    private void drawZoomLimitMessage(String message) {
//        gc.save(); // 保存当前绘图状态
//
//        // 设置半透明背景
//        gc.setFill(new Color(0, 0, 0, 0.7));
//        gc.fillRect(getWidth()/2 - 100, getHeight() - 50, 200, 30);
//
//        // 设置文字
//        gc.setFill(Color.WHITE);
//        gc.setFont(javafx.scene.text.Font.font("Arial", 14));
//        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
//        gc.fillText(message, getWidth()/2, getHeight() - 30);
//
//        gc.restore(); // 恢复绘图状态
//
//        // 2秒后清除提示
//        new Thread(() -> {
//            try {
//                Thread.sleep(2000);
//                Platform.runLater(this::requestRedraw);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }).start();
//    }


    private void handleMouseMoved(MouseEvent event) {
        LogUtils.info("MapCanvas-handleMouseMoved-鼠标移动");

        // 更新鼠标坐标
        mapModel.updateMousePosition(event.getX(), event.getY(), true);

        // 触发重绘以更新坐标显示
        requestRedraw();
    }

    private void handleMouseEntered(MouseEvent event) {
        LogUtils.info("MapCanvas-handleMouseEntered-鼠标进入");

        mapModel.updateMousePosition(event.getX(), event.getY(), true);
        requestRedraw();
    }

    private void handleMouseExited(MouseEvent event) {
        LogUtils.info("MapCanvas-handleMouseExited-鼠标退出");

        mapModel.updateMousePosition(0, 0, false);
        requestRedraw();
    }

    private void startRenderLoop() {
        LogUtils.info("MapCanvas-startRenderLoop-启动渲染循环");

        renderTimer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                // 限制渲染频率（最多60FPS）
                if (now - lastUpdate >= 16_666_666) { // ~60 FPS
                    if (isRendering) {
                        render();
                        isRendering = false;
                    }
                    lastUpdate = now;
                }
            }
        };
        renderTimer.start();
    }

    public void requestRedraw() {
        LogUtils.info("MapCanvas-requestRedraw-重绘");

        isRendering = true;
    }

    private void render() {
        LogUtils.info("MapCanvas-render-渲染");

        gc.clearRect(0, 0, getWidth(), getHeight());

        // 绘制背景
        drawBackground();

        // 绘制瓦片
        drawTiles();

        // 绘制SHP图层
        drawLayers();

        // 绘制叠加层
        drawOverlays();

        // 绘制调试信息
        drawDebugInfo();
        // 绘制导航点
        drawNavPoints();

    }

    private void drawNavPoints() {

        if (navPointRenderService != null) {
            LogUtils.info("MapCanvas-drawNavPoints-绘制点信息");
            navPointRenderService.renderNavPointLayers(gc, navPointLayers, mapModel);
        }
    }

    // 添加导航点图层管理方法
    public void addNavPointLayer(NavPointLayerModel layer) {
        LogUtils.info("MapCanvas-addNavPointLayer-添加导航点图层管理方法");
        navPointLayers.add(layer);
        requestRedraw();
    }

    public void removeNavPointLayer(NavPointLayerModel layer) {
        LogUtils.info("MapCanvas-removeNavPointLayer-移除导航点图层管理方法");

        navPointLayers.remove(layer);
        requestRedraw();
    }

    public void clearNavPointLayers() {
        LogUtils.info("MapCanvas-clearNavPointLayers-清除导航点图层管理方法");

        navPointLayers.clear();
        requestRedraw();
    }

    public ObservableList<NavPointLayerModel> getNavPointLayers() {
        LogUtils.info("MapCanvas-getNavPointLayers-获取导航点图层管理方法");

        return navPointLayers;
    }


    /**
     * 绘制导航点
     */

    private void drawBackground() {
        LogUtils.info("MapCanvas-drawBackground-绘制导航点");

        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawTiles() {
        LogUtils.info("MapCanvas-drawTiles-使用TileService获取并绘制瓦片");

        // 使用TileService获取并绘制瓦片
        if (tileService != null) {
            tileService.renderTiles(gc, mapModel, getWidth(), getHeight());
        }
    }

    private void drawLayers() {
        LogUtils.info("MapCanvas-drawLayers-绘制图层");

        if (renderService != null) {
            for (LayerModel layer : layers) {
                if (layer.isVisible()) {
                    renderService.renderLayer(gc, layer, mapModel);
                }
            }
        }
    }

    private void drawOverlays() {
        LogUtils.info("MapCanvas-drawOverlays-绘制叠加层");

        // 绘制网格
        if (mapModel.isGridVisible()) {
            drawGrid();
        }

        // 绘制坐标信息
        drawCoordinateInfo();
    }

    private void drawGrid() {
        LogUtils.info("MapCanvas-drawGrid-绘制网格");

        gc.setStroke(new Color(0.7, 0.7, 0.7, 0.5));
        gc.setLineWidth(0.5);

        double gridSize = 50 * scale;
        for (double x = 0; x < getWidth(); x += gridSize) {
            gc.strokeLine(x, 0, x, getHeight());
        }
        for (double y = 0; y < getHeight(); y += gridSize) {
            gc.strokeLine(0, y, getWidth(), y);
        }
    }

    // 在drawCoordinateInfo()中使用格式化工具
    private void drawCoordinateInfo() {
        LogUtils.info("MapCanvas-drawCoordinateInfo-在drawCoordinateInfo()中使用格式化工具");

        gc.setFill(Color.BLACK);
        gc.setFont(javafx.scene.text.Font.font("Arial", 12));

        // 中心点坐标信息
        String centerInfo = String.format("中心: %.4f°E, %.4f°N | 缩放: %d级", mapModel.getCenterX(), mapModel.getCenterY(), mapModel.getZoomLevel());
        gc.fillText(centerInfo, 10, getHeight() - 30);

        // 鼠标坐标信息
        if (mapModel.isMouseInside()) {
            // 使用格式化工具
            String mouseInfo = CoordinateFormatter.formatSimple(mapModel.getMouseLon(), mapModel.getMouseLat(), 4);
            gc.fillText("鼠标: " + mouseInfo, 10, getHeight() - 10);

            // 或者使用度分秒格式
            // String mouseInfo = CoordinateFormatter.formatToDMS(
            //     mapModel.getMouseLon(), mapModel.getMouseLat());
            // gc.fillText("鼠标: " + mouseInfo, 10, getHeight() - 10);
        } else {
            gc.fillText("鼠标: 离开画布区域", 10, getHeight() - 10);
        }

        // 添加分隔线
        gc.setStroke(new Color(0, 0, 0, 0.3));
        gc.setLineWidth(1);
        gc.strokeLine(0, getHeight() - 40, getWidth(), getHeight() - 40);
    }


    private void drawDebugInfo() {
        LogUtils.info("MapCanvas-drawDebugInfo-绘制调试信息");

        if (mapModel.isDebugMode()) {
            gc.setFill(Color.RED);
            gc.fillText(String.format("图层数: %d | 缓存: %d", layers.size(), tileService != null ? tileService.getCacheSize() : 0), 10, 30);
        }
    }

    // 公共API
    public void addLayer(LayerModel layer) {
        LogUtils.info("MapCanvas-addLayer-添加图层 公共API ");

        layers.add(layer);
        requestRedraw();
    }

    public void removeLayer(LayerModel layer) {
        LogUtils.info("MapCanvas-addLayer-移除图层");

        layers.remove(layer);
        requestRedraw();
    }

    public void clearLayers() {
        LogUtils.info("MapCanvas-clearLayers-清除图层");

        layers.clear();
        requestRedraw();
    }

    public void setMapCenter(double lon, double lat) {
        LogUtils.info("MapCanvas-setMapCenter-将地图中心设置为图层的中心");

        mapModel.setCenter(lon, lat);
        requestRedraw();
    }

    public void setZoomLevel(int zoom) {
        LogUtils.info("MapCanvas-setZoomLevel-放大级别");

        mapModel.setZoomLevel(zoom);
        requestRedraw();
    }

    public void fitToLayers() {
        LogUtils.info("MapCanvas-fitToLayers-自动调整视图到图层范围");

        if (!layers.isEmpty()) {
            // 计算所有图层的包围盒
            // 调整地图视图
            requestRedraw();
        }
    }

    public void exportToImage(String filePath) {
        LogUtils.info("MapCanvas-exportToImage-导出地图图片");

        // 导出当前视图为图片
        try {
            javafx.scene.image.WritableImage snapshot = snapshot(null, null);
            javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(snapshot, null), "PNG", new java.io.File(filePath));
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    public void redraw() {
        LogUtils.info("MapCanvas-redraw-重绘");

        requestRedraw();
    }

    // 添加获取MapModel的方法
    public MapModel getMapModel() {
        LogUtils.info("MapCanvas-getMapModel-添加获取MapModel的方法");

        return mapModel;
    }







}
