// view/MapView.java - 修改部分
package ll.luolin.view;

import javafx.application.Platform;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import ll.luolin.controller.MapController;
import ll.luolin.controller.NavPointController;
import ll.luolin.model.LayerModel;
import ll.luolin.model.MapModel;
import ll.luolin.model.NavPointLayerModel;
import ll.luolin.model.NavPointModel;
import ll.luolin.service.AutoLoadService;
import ll.luolin.utils.LogUtils;

import java.io.File;
import java.util.List;

public class MapView extends Application {

    private MapController controller;
    private MapCanvas mapCanvas;
    private ListView<LayerModel> layerList;

    // 状态栏标签
    private Label coordLabel;
    private Label mouseCoordLabel;
    private Label scaleLabel;
    private Label layerLabel;
    // 添加导航点控制器
    private NavPointController navPointController;


    @Override
    public void start(Stage primaryStage) {
        LogUtils.info("MapView-start-程序初始化");

        // 初始化控制器
        controller = new MapController();
        navPointController = new NavPointController();

        // 创建UI组件
        mapCanvas = new MapCanvas(1200, 800);
        layerList = createLayerListView();

        // 创建左侧面板（使用TabPane）
        TabPane leftTabPane = createLeftTabPane();

        // 创建布局
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar(primaryStage));
        root.setCenter(mapCanvas);
        root.setLeft(leftTabPane);  // 改为TabPane
        root.setBottom(createStatusBar());

        // 设置场景
        Scene scene = new Scene(root, 1400, 900);
        primaryStage.setTitle("地图可视化工具 - 重构版");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 初始化地图
        initializeMap();

        // 绑定控制器
        controller.setMapCanvas(mapCanvas);
        navPointController.setMapCanvas(mapCanvas);
        // 设置导航点工具提示
       // NavPointTooltip.setupTooltips(mapCanvas, navPointController);

        // 加载自动加载的导航点
        loadAutoLoadedNavPoints();
        // 绑定鼠标坐标到状态栏
        bindMouseCoordinates();
    }

    /**
     * 加载自动加载的导航点
     */
    private void loadAutoLoadedNavPoints() {
        LogUtils.info("MapView-loadAutoLoadedNavPoints-加载自动加载的导航点");

        try {
            AutoLoadService autoLoadService = AutoLoadService.getInstance();
            List<NavPointLayerModel> autoLayers = autoLoadService.getAllAutoLoadedLayers();

            if (!autoLayers.isEmpty()) {
                LogUtils.info("自动加载导航点图层: " + autoLayers.size() + " 个");

                for (NavPointLayerModel layer : autoLayers) {
                    navPointController.getNavPointLayers().add(layer);
                    mapCanvas.addNavPointLayer(layer);
                }

                // 只在控制台显示信息，不弹窗
                System.out.println("已自动加载 " + autoLayers.size() + " 个ASF文件");
            } else {
                System.out.println("未找到ASF文件，请将文件放置在 asf_files 目录下");
            }

        } catch (Exception e) {
            LogUtils.error("自动加载导航点失败", e);
            System.err.println("自动加载失败: " + e.getMessage());
        }
    }


    private void showAutoLoadNotification(int layerCount) {
        // 只在控制台显示，不弹窗
        System.out.println("已自动加载 " + layerCount + " 个导航点图层");
    }

    private TabPane createLeftTabPane() {
        LogUtils.info("MapView-createLeftTabPane-创建左侧面板（使用TabPane）");

        TabPane tabPane = new TabPane();
        tabPane.setPrefWidth(350);

        // SHP图层Tab
        Tab shpTab = new Tab("SHP图层");
        shpTab.setClosable(false);
        shpTab.setContent(createShpFileListPanel());

        // 导航点Tab
        Tab navTab = new Tab("导航点");
        navTab.setClosable(false);
        navTab.setContent(new NavPointPanel(navPointController));

        // 工具Tab
        Tab toolTab = new Tab("工具");
        toolTab.setClosable(false);
        toolTab.setContent(createToolPanel());

        tabPane.getTabs().addAll(shpTab, navTab, toolTab);

        return tabPane;
    }

    private VBox createShpFileListPanel() {
        LogUtils.info("MapView-createShpFileListPanel-SHP图层Tab");

        VBox panel = new VBox();
        panel.setPadding(new Insets(10));
        panel.setSpacing(10);
        panel.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label("SHP文件列表");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        layerList.setPrefHeight(600);
        layerList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                LayerModel selectedItem = layerList.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    loadSelectedShpFile(selectedItem);
                }
            }
        });

        Button refreshButton = new Button("刷新列表");
        refreshButton.setOnAction(e -> refreshShpFileList());

        Button addButton = new Button("添加文件");
        addButton.setOnAction(e -> copyFileToResourceDir());

        panel.getChildren().addAll(titleLabel, layerList, refreshButton, addButton);

        return panel;
    }

    private VBox createToolPanel() {
        LogUtils.info("MapView-createToolPanel-工具Tab");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label("地图工具");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // 测量工具
        Button measureButton = new Button("距离测量");
        measureButton.setPrefWidth(150);

        // 查询工具
        Button queryButton = new Button("属性查询");
        queryButton.setPrefWidth(150);

        // 导航点搜索
        TextField searchField = new TextField();
        searchField.setPromptText("搜索导航点...");
        Button searchButton = new Button("搜索");

        searchButton.setOnAction(e -> {
            String keyword = searchField.getText();
            if (!keyword.trim().isEmpty()) {
                searchNavPoints(keyword);
            }
        });

        HBox searchBox = new HBox(5, searchField, searchButton);

        panel.getChildren().addAll(
                titleLabel,
                new Separator(),
                measureButton,
                queryButton,
                new Separator(),
                new Label("导航点搜索:"),
                searchBox
        );

        return panel;
    }

    private void searchNavPoints(String keyword) {
        LogUtils.info("MapView-searchNavPoints-导航点搜索");

        List<NavPointModel> results = navPointController.searchNavPoints(keyword);

        if (results.isEmpty()) {
            showAlert("搜索结果", "未找到匹配的导航点",
                    javafx.scene.control.Alert.AlertType.INFORMATION);
        } else {
            // 创建结果对话框
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("搜索结果");
            dialog.setHeaderText("找到 " + results.size() + " 个匹配的导航点");

            // 创建结果列表
            ListView<NavPointModel> resultList = new ListView<>();
            resultList.getItems().addAll(results);
            resultList.setCellFactory(param -> new NavPointResultCell());

            // 双击跳转到导航点
            resultList.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    NavPointModel selected = resultList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        mapCanvas.setMapCenter(selected.getLongitude(), selected.getLatitude());
                        mapCanvas.setZoomLevel(10); // 放大到合适级别
                        dialog.close();
                    }
                }
            });

            dialog.getDialogPane().setContent(resultList);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.show();
        }
    }

    // 导航点搜索结果单元格
    private static class NavPointResultCell extends ListCell<NavPointModel> {

        private final Label nameLabel = new Label();
        private final Label typeLabel = new Label();
        private final Label coordLabel = new Label();
        private final HBox content = new HBox(10, nameLabel, typeLabel, coordLabel);

        public NavPointResultCell() {

            super();
            LogUtils.info("MapView-NavPointResultCell-导航点搜索结果单元格初始化");
            content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            nameLabel.setStyle("-fx-font-weight: bold;");
            typeLabel.setStyle("-fx-text-fill: gray;");
            coordLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 10px;");
        }

        @Override
        protected void updateItem(NavPointModel point, boolean empty) {
            LogUtils.info("MapView-NavPointResultCell-updateItem-更新");

            super.updateItem(point, empty);

            if (empty || point == null) {
                setText(null);
                setGraphic(null);
            } else {
                nameLabel.setText(point.getName());
                typeLabel.setText(point.getType());
                coordLabel.setText(String.format("%.4f°E, %.4f°N",
                        point.getLongitude(), point.getLatitude()));
                setGraphic(content);
            }
        }
    }

    private void showAlert(String title, String message,
                           javafx.scene.control.Alert.AlertType type) {
        LogUtils.info("MapView-showAlert-显示告警");

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // view/MapView.java - 简化菜单栏
    private MenuBar createMenuBar(Stage stage) {
        LogUtils.info("MapView-createMenuBar-简化菜单栏");

        MenuBar menuBar = new MenuBar();

        // 文件菜单
        Menu fileMenu = new Menu("文件");
        MenuItem loadShpItem = new MenuItem("加载SHP文件");
        MenuItem loadAsfItem = new MenuItem("加载ASF文件");
        MenuItem reloadAsfItem = new MenuItem("重新加载ASF文件");
        MenuItem exportItem = new MenuItem("导出图片");
        MenuItem exitItem = new MenuItem("退出");

        loadShpItem.setOnAction(e -> controller.loadShpFile(stage));
        loadAsfItem.setOnAction(e -> navPointController.loadAsfFile(stage));
        reloadAsfItem.setOnAction(e -> reloadAsfFiles());
        exportItem.setOnAction(e -> controller.exportMap(stage));
        exitItem.setOnAction(e -> System.exit(0));

        fileMenu.getItems().addAll(loadShpItem, loadAsfItem, reloadAsfItem,
                new SeparatorMenuItem(), exportItem,
                new SeparatorMenuItem(), exitItem);

        // 视图菜单
        Menu viewMenu = new Menu("视图");
        CheckMenuItem gridItem = new CheckMenuItem("显示网格");
        CheckMenuItem debugItem = new CheckMenuItem("调试模式");

        gridItem.setSelected(true);
        gridItem.selectedProperty().addListener((obs, oldVal, newVal) -> {
            controller.setGridVisible(newVal);
        });

        debugItem.selectedProperty().addListener((obs, oldVal, newVal) -> {
            controller.setDebugMode(newVal);
        });

        viewMenu.getItems().addAll(gridItem, debugItem);

        // 帮助菜单
        Menu helpMenu = new Menu("帮助");
        MenuItem aboutItem = new MenuItem("关于");

        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().addAll(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }
    private void showAboutDialog() {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("关于");
        about.setHeaderText("地图可视化工具");
        about.setContentText("版本: 2.0.0\n" +
                "作者: ll.luolin\n" +
                "功能: 支持SHP文件和ASF导航点文件\n" +
                "日期: 2024年");
        about.showAndWait();
    }
    /**
     * 重新加载ASF文件
     */
    private void reloadAsfFiles() {
        LogUtils.info("MapView-reloadAsfFiles-重新加载ASF文件");

        try {
            // 清除现有图层
            navPointController.clearNavPointLayers();

            // 重新加载
            AutoLoadService autoLoadService = AutoLoadService.getInstance();
            List<NavPointLayerModel> autoLayers = autoLoadService.reloadAllLayers();

            for (NavPointLayerModel layer : autoLayers) {
                navPointController.getNavPointLayers().add(layer);
                mapCanvas.addNavPointLayer(layer);
            }

            // 显示简单提示
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("重新加载");
            alert.setHeaderText(null);
            alert.setContentText(String.format("已重新加载 %d 个ASF文件", autoLayers.size()));
            alert.showAndWait();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("错误");
            alert.setHeaderText(null);
            alert.setContentText("重新加载失败: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private VBox createLeftPanel() {
        LogUtils.info("MapView-createLeftPanel-创建左侧边栏");

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(300);
        panel.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label("图层管理");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 图层列表
        layerList.setPrefHeight(600);
        layerList.setCellFactory(param -> new LayerListCell());

        // 控制按钮
        Button addButton = new Button("添加图层");
        Button removeButton = new Button("移除图层");
        Button upButton = new Button("上移");
        Button downButton = new Button("下移");

        addButton.setOnAction(e -> controller.addLayer());
        removeButton.setOnAction(e -> {
            LayerModel selected = layerList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                controller.removeLayer(selected);
            }
        });

        // 按钮布局
        VBox buttonBox = new VBox(5, addButton, removeButton, upButton, downButton);

        panel.getChildren().addAll(titleLabel, layerList, buttonBox);
        return panel;
    }

    private ListView<LayerModel> createLayerListView() {
        LogUtils.info("MapView-createLayerListView-创建UI组件");

        ListView<LayerModel> listView = new ListView<>();
        listView.setCellFactory(param -> new LayerListCell());

        // 绑定控制器中的图层列表 - 修复这里
        listView.itemsProperty().bind(controller.layersProperty());

        // 双击图层项
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                LayerModel selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    controller.zoomToLayer(selected);
                }
            }
        });

        return listView;
    }

    private HBox createStatusBar() {
        LogUtils.info("MapView-createStatusBar-创建状态组件");

        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #e0e0e0;");

        // 创建标签
        coordLabel = new Label("中心坐标: ");
        mouseCoordLabel = new Label("鼠标坐标: ");
        scaleLabel = new Label("缩放: ");
        layerLabel = new Label("图层: 0");

        // 添加分隔符
        Separator separator1 = new Separator(Orientation.VERTICAL);
        Separator separator2 = new Separator(Orientation.VERTICAL);
        Separator separator3 = new Separator(Orientation.VERTICAL);

        separator1.setPrefHeight(20);
        separator2.setPrefHeight(20);
        separator3.setPrefHeight(20);

        statusBar.getChildren().addAll(
                coordLabel, separator1,
                mouseCoordLabel, separator2,
                scaleLabel, separator3,
                layerLabel
        );

        return statusBar;
    }

    private void initializeMap() {
        LogUtils.info("MapView-initializeMap-初始化地图");

        // 设置初始视图（中国区域）
        mapCanvas.setMapCenter(104.0, 35.0);
        mapCanvas.setZoomLevel(5);
    }

    private void bindMouseCoordinates() {
        LogUtils.info("MapView-bindMouseCoordinates-绑定鼠标坐标");

        if (mapCanvas != null) {
            MapModel mapModel = mapCanvas.getMapModel();

            // 绑定中心坐标
            mapModel.centerXProperty().addListener((obs, oldVal, newVal) -> {
                updateCenterCoordinate();
            });
            mapModel.centerYProperty().addListener((obs, oldVal, newVal) -> {
                updateCenterCoordinate();
            });

            // 绑定鼠标坐标
            mapModel.mouseLonProperty().addListener((obs, oldVal, newVal) -> {
                updateMouseCoordinate();
            });
            mapModel.mouseLatProperty().addListener((obs, oldVal, newVal) -> {
                updateMouseCoordinate();
            });
            mapModel.mouseInsideProperty().addListener((obs, oldVal, newVal) -> {
                updateMouseCoordinate();
            });

            // 绑定缩放级别
            mapModel.zoomLevelProperty().addListener((obs, oldVal, newVal) -> {
                scaleLabel.setText("缩放: " + newVal + "级");
            });

            // 绑定图层数量
            controller.layersProperty().addListener((obs, oldVal, newVal) -> {
                layerLabel.setText("图层: " + newVal.size());
            });
        }
    }


    private void updateCenterCoordinate() {
        LogUtils.info("MapView-updateCenterCoordinate-更新中心点坐标");

        if (mapCanvas != null) {
            MapModel mapModel = mapCanvas.getMapModel();
            coordLabel.setText(String.format("中心: %.4f°E, %.4f°N",
                    mapModel.getCenterX(), mapModel.getCenterY()));
        }
    }

    private void updateMouseCoordinate() {
        LogUtils.info("MapView-updateMouseCoordinate-更新鼠标坐标");

        if (mapCanvas != null) {
            MapModel mapModel = mapCanvas.getMapModel();
            if (mapModel.isMouseInside()) {
                mouseCoordLabel.setText(String.format("鼠标: %.4f°E, %.4f°N",
                        mapModel.getMouseLon(), mapModel.getMouseLat()));
            } else {
                mouseCoordLabel.setText("鼠标: 离开画布");
            }
        }
    }


    // 自定义列表单元格
    private static class LayerListCell extends ListCell<LayerModel> {
        private final CheckBox visibilityCheck = new CheckBox();
        private final Label nameLabel = new Label();
        private final HBox content = new HBox(10, visibilityCheck, nameLabel);

        public LayerListCell() {
            super();
            LogUtils.info("MapView-LayerListCell-LayerListCell-自定义列表单元格");

            content.setAlignment(Pos.CENTER_LEFT);

            // 可见性切换
            visibilityCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                LayerModel layer = getItem();
                if (layer != null) {
                    layer.setVisible(newVal);
                }
            });
        }

        @Override
        protected void updateItem(LayerModel layer, boolean empty) {
            LogUtils.info("MapView-LayerListCell-updateItem-更新自定义列表单元格");

            super.updateItem(layer, empty);

            if (empty || layer == null) {
                setText(null);
                setGraphic(null);
            } else {
                nameLabel.setText(layer.getName());
                visibilityCheck.setSelected(layer.isVisible());
                setGraphic(content);
            }
        }
    }


    private void loadSelectedShpFile(LayerModel layer) {
        LogUtils.info("MapView-loadSelectedShpFile-加载选择的shp");

        if (layer != null && controller != null) {
            try {
                // 这里需要根据你的具体实现来加载SHP文件
                // 假设controller有加载图层的方法
                System.out.println("加载图层: " + layer.getName());

                // 显示加载成功的消息
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("加载成功");
                alert.setHeaderText(null);
                alert.setContentText("成功加载图层: " + layer.getName());
                alert.showAndWait();
            } catch (Exception e) {
                e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("加载失败");
                alert.setHeaderText("无法加载图层");
                alert.setContentText("错误信息: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * 刷新SHP文件列表
     */
    private void refreshShpFileList() {
        LogUtils.info("MapView-refreshShpFileList-刷新SHP文件列表");

        // 这里需要实现从资源目录获取SHP文件的逻辑
        // 暂时使用模拟数据
//        layerList.getItems().clear();
//
//        // 模拟数据
//        LayerModel layer1 = new LayerModel("中国省份", new File("data/china_provinces.shp"));
//        LayerModel layer2 = new LayerModel("主要城市", new File("data/major_cities.shp"));
//        LayerModel layer3 = new LayerModel("河流", new File("data/rivers.shp"));
//
//        layerList.getItems().addAll(layer1, layer2, layer3);
//
//        if (layerList.getItems().isEmpty()) {
//            layerList.getItems().add("(暂无SHP文件)");
//            layerList.getItems().add("请将SHP文件放置在资源目录");
//        }
    }

    /**
     * 复制文件到资源目录
     */
    private void copyFileToResourceDir() {
        LogUtils.info("MapView-copyFileToResourceDir-复制文件到资源目录");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择SHP文件并复制到资源目录");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Shapefile (*.shp)", "*.shp"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(getStage());
        if (selectedFile != null) {
            try {
                // 这里需要实现文件复制逻辑
                // 暂时显示成功消息
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("复制成功");
                alert.setHeaderText(null);
                alert.setContentText("文件已复制到资源目录: " + selectedFile.getName());
                alert.showAndWait();

                // 刷新列表
                refreshShpFileList();

            } catch (Exception e) {
                e.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("复制失败");
                alert.setHeaderText("无法复制文件");
                alert.setContentText("错误信息: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * 获取当前Stage
     */
    private Stage getStage() {
        LogUtils.info("MapView-getStage-获取当前Stage");

        return (Stage) layerList.getScene().getWindow();
    }

    public static void main(String[] args) {
        launch(args);
    }


}
