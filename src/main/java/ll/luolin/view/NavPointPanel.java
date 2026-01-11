// view/NavPointPanel.java
package ll.luolin.view;

import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.image.ImageView;
import ll.luolin.controller.NavPointController;
import ll.luolin.model.NavPointLayerModel;
import ll.luolin.model.NavPointModel;
import ll.luolin.utils.LogUtils;


/**
 * 导航点图层面板
 */
public class NavPointPanel extends VBox {

    private final NavPointController controller;
    private final ListView<NavPointLayerModel> layerList;
    private final Accordion detailAccordion;

    public NavPointPanel(NavPointController controller) {
        LogUtils.info("NavPointPanel-NavPointPanel-初始化");

        this.controller = controller;

        // 设置面板样式
        setPadding(new Insets(10));
        setSpacing(10);
        setPrefWidth(300);
        setStyle("-fx-background-color: #f5f5f5;");

        // 创建标题
        Label titleLabel = new Label("导航点管理");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // 创建图层列表
        layerList = createLayerListView();

        // 创建详细面板
        detailAccordion = createDetailAccordion();

        // 创建按钮栏
        HBox buttonBar = createButtonBar();

        // 添加组件
        getChildren().addAll(titleLabel, layerList, detailAccordion, buttonBar);

        // 绑定数据
        bindData();
    }

    private ListView<NavPointLayerModel> createLayerListView() {
        LogUtils.info("NavPointPanel-createLayerListView-创建listview");

        ListView<NavPointLayerModel> listView = new ListView<>();
        listView.setPrefHeight(200);
        listView.setCellFactory(param -> new LayerListCell());

        // 选择监听
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> updateDetailPanel(newVal)
        );

        return listView;
    }

    private Accordion createDetailAccordion() {
        LogUtils.info("NavPointPanel-createDetailAccordion-创建详细面板");

        Accordion accordion = new Accordion();
        accordion.setPrefHeight(300);

        // 默认折叠
        accordion.setExpandedPane(null);

        return accordion;
    }

    private HBox createButtonBar() {
        LogUtils.info("NavPointPanel-createButtonBar-创建按钮栏");

        HBox buttonBar = new HBox(5);
        buttonBar.setPadding(new Insets(5, 0, 0, 0));

        Button loadButton = new Button("加载ASF");
        Button removeButton = new Button("移除");
        Button clearButton = new Button("清空");
        Button exportButton = new Button("导出");

        // 设置按钮事件
        loadButton.setOnAction(e -> controller.loadAsfFile(getStage()));
        removeButton.setOnAction(e -> removeSelectedLayer());
        clearButton.setOnAction(e -> controller.clearNavPointLayers());
        exportButton.setOnAction(e -> exportSelectedLayer());

        // 禁用按钮直到选择图层
        removeButton.disableProperty().bind(
                layerList.getSelectionModel().selectedItemProperty().isNull()
        );
        exportButton.disableProperty().bind(
                layerList.getSelectionModel().selectedItemProperty().isNull()
        );

        buttonBar.getChildren().addAll(loadButton, removeButton, clearButton, exportButton);
        return buttonBar;
    }

    private void bindData() {
        LogUtils.info("NavPointPanel-bindData-绑定数据");

        // 绑定图层列表数据
//        layerList.itemsProperty().bind(controller.navPointLayersProperty());
        Bindings.bindContent(layerList.getItems(), controller.getNavPointLayers());
    }

    private void updateDetailPanel(NavPointLayerModel layer) {
        LogUtils.info("NavPointPanel-updateDetailPanel-选择监听");

        detailAccordion.getPanes().clear();

        if (layer == null) {
            return;
        }

        // 创建统计信息面板
        TitledPane statsPane = createStatsPane(layer);

        // 创建类型过滤面板
        TitledPane filterPane = createFilterPane(layer);

        // 创建点列表面板
        TitledPane pointsPane = createPointsPane(layer);

        detailAccordion.getPanes().addAll(statsPane, filterPane, pointsPane);
    }

    private TitledPane createStatsPane(NavPointLayerModel layer) {
        LogUtils.info("NavPointPanel-createStatsPane-创建统计信息面板");

        VBox content = new VBox(5);
        content.setPadding(new Insets(10));

        Label fileLabel = new Label("文件: " + layer.getFile().getName());
        Label countLabel = new Label("总点数: " + layer.getNavPoints().size());

        // 统计各类型点数
        long airportCount = layer.getNavPoints().stream()
                .filter(p -> p.getType().toUpperCase().contains("AIRPORT"))
                .count();
        long vorCount = layer.getNavPoints().stream()
                .filter(p -> p.getType().toUpperCase().contains("VOR"))
                .count();
        long ndbCount = layer.getNavPoints().stream()
                .filter(p -> p.getType().toUpperCase().contains("NDB"))
                .count();
        long reportCount = layer.getNavPoints().stream()
                .filter(p -> p.getType().toUpperCase().contains("REPORT"))
                .count();

        Label typeLabel = new Label(String.format(
                "机场: %d, VOR: %d, NDB: %d, 报告点: %d",
                airportCount, vorCount, ndbCount, reportCount
        ));

        content.getChildren().addAll(fileLabel, countLabel, typeLabel);

        return new TitledPane("统计信息", content);
    }

    private TitledPane createFilterPane(NavPointLayerModel layer) {
        LogUtils.info("NavPointPanel-createFilterPane-创建类型过滤面板");

        VBox content = new VBox(5);
        content.setPadding(new Insets(10));

        // 创建类型过滤复选框
        CheckBox airportCheck = new CheckBox("显示机场");
        CheckBox vorCheck = new CheckBox("显示VOR");
        CheckBox ndbCheck = new CheckBox("显示NDB");
        CheckBox reportCheck = new CheckBox("显示报告点");

        // 绑定到图层属性
        airportCheck.selectedProperty().bindBidirectional(layer.showAirportsProperty());
        vorCheck.selectedProperty().bindBidirectional(layer.showVorsProperty());
        ndbCheck.selectedProperty().bindBidirectional(layer.showNdbsProperty());
        reportCheck.selectedProperty().bindBidirectional(layer.showReportsProperty());

        content.getChildren().addAll(airportCheck, vorCheck, ndbCheck, reportCheck);

        return new TitledPane("类型过滤", content);
    }

    private TitledPane createPointsPane(NavPointLayerModel layer) {
        LogUtils.info("NavPointPanel-createPointsPane-创建点列表面板");

        ListView<NavPointModel> pointsList = new ListView<>();
        pointsList.setPrefHeight(150);
        pointsList.setCellFactory(param -> new PointListCell());

        // 绑定数据
        pointsList.itemsProperty().bind(layer.navPointsProperty());

        // 搜索框
        TextField searchField = new TextField();
        searchField.setPromptText("搜索导航点...");
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterPointsList(pointsList, layer, newVal);
        });

        VBox content = new VBox(5);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(searchField, pointsList);

        return new TitledPane("导航点列表", content);
    }

    private void filterPointsList(ListView<NavPointModel> listView,
                                  NavPointLayerModel layer, String keyword) {
        LogUtils.info("NavPointPanel-filterPointsList-搜索框列表");

        if (keyword == null || keyword.trim().isEmpty()) {
            listView.itemsProperty().bind(layer.navPointsProperty());
        } else {
            String searchTerm = keyword.toLowerCase();
            javafx.collections.transformation.FilteredList<NavPointModel> filtered =
                    layer.getNavPoints().filtered(p ->
                            p.getName().toLowerCase().contains(searchTerm) ||
                                    p.getType().toLowerCase().contains(searchTerm)
                    );
            listView.setItems(filtered);
        }
    }

    private void removeSelectedLayer() {
        LogUtils.info("NavPointPanel-removeSelectedLayer-移除选择的图层");

        NavPointLayerModel selected = layerList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            controller.removeNavPointLayer(selected);
        }
    }

    private void exportSelectedLayer() {
        LogUtils.info("NavPointPanel-exportSelectedLayer-导出选择的图层");

        NavPointLayerModel selected = layerList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            controller.exportNavPoints(getStage(), selected);
        }
    }

    private javafx.stage.Stage getStage() {
        LogUtils.info("NavPointPanel-getStage-getStage");

        return (javafx.stage.Stage) getScene().getWindow();
    }

    // 自定义列表单元格
    private static class LayerListCell extends ListCell<NavPointLayerModel> {

        private final CheckBox visibilityCheck = new CheckBox();
        private final Label nameLabel = new Label();
        private final HBox content = new HBox(10, visibilityCheck, nameLabel);

        public LayerListCell() {
            super();
            LogUtils.info("NavPointPanel-LayerListCell-LayerListCell-自定义列表单元格");

            content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // 可见性切换
            visibilityCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                NavPointLayerModel layer = getItem();
                if (layer != null) {
                    layer.setVisible(newVal);
                }
            });
        }

        @Override
        protected void updateItem(NavPointLayerModel layer, boolean empty) {
            super.updateItem(layer, empty);
            LogUtils.info("NavPointPanel-LayerListCell-updateItem-更新自定义列表单元格");


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

    private static class PointListCell extends ListCell<NavPointModel> {
        private final Label nameLabel = new Label();
        private final Label typeLabel = new Label();
        private final Label coordLabel = new Label();
        private final HBox content = new HBox(10, nameLabel, typeLabel, coordLabel);

        public PointListCell() {
            super();
            LogUtils.info("NavPointPanel-PointListCell-PointListCell-自定义点列表单元格");

            content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // 设置标签样式
            nameLabel.setStyle("-fx-font-weight: bold;");
            typeLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");
            coordLabel.setStyle("-fx-text-fill: blue; -fx-font-size: 9px;");
        }

        @Override
        protected void updateItem(NavPointModel point, boolean empty) {
            super.updateItem(point, empty);
            LogUtils.info("NavPointPanel-PointListCell-PointListCell-更新自定义点列表单元格");


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
}
