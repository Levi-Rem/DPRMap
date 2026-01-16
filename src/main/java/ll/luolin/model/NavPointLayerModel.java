// model/NavPointLayerModel.java - 完整修复版
package ll.luolin.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ll.luolin.model.ASFModel.NavPointModel;
import ll.luolin.model.ASFModel.PointModel;
import ll.luolin.utils.LogUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;

/**
 * 导航点图层模型
 */
public class NavPointLayerModel {
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    private final DoubleProperty opacity = new SimpleDoubleProperty(1.0);

    // 使用ListProperty来包装导航点列表
    private final ListProperty<NavPointModel> navPointsProperty =
            new SimpleListProperty<>(FXCollections.observableArrayList());

    // 类型过滤
    private final BooleanProperty showAirports = new SimpleBooleanProperty(true);
    private final BooleanProperty showVors = new SimpleBooleanProperty(true);
    private final BooleanProperty showNdbs = new SimpleBooleanProperty(true);
    private final BooleanProperty showReports = new SimpleBooleanProperty(true);

    public NavPointLayerModel(String name, File file) {
        LogUtils.info("NavPointLayerModel-NavPointLayerModel-初始化");

        this.name.set(name);
        this.file.set(file);

        // 监听类型过滤变化
        setupTypeFilterListeners();
    }

    private void setupTypeFilterListeners() {
        LogUtils.info("NavPointLayerModel-setupTypeFilterListeners-类型监听");

        // 当类型过滤变化时，更新点的可见性
        showAirports.addListener((obs, oldVal, newVal) -> updatePointsVisibility());
        showVors.addListener((obs, oldVal, newVal) -> updatePointsVisibility());
        showNdbs.addListener((obs, oldVal, newVal) -> updatePointsVisibility());
        showReports.addListener((obs, oldVal, newVal) -> updatePointsVisibility());

        // 当图层可见性变化时，更新点的可见性
        visible.addListener((obs, oldVal, newVal) -> updatePointsVisibility());
    }

    /**
     * 更新点的可见性
     */
    private void updatePointsVisibility() {
        LogUtils.info("NavPointLayerModel-setupTypeFilterListeners-类型监听");

        for (NavPointModel point : navPointsProperty) {
            String type = point.getType().toUpperCase();
            boolean shouldShow = false;

            if (type.contains("AIRPORT")) {
                shouldShow = showAirports.get();
            } else if (type.contains("VOR")) {
                shouldShow = showVors.get();
            } else if (type.contains("NDB")) {
                shouldShow = showNdbs.get();
            } else if (type.contains("REPORT")) {
                shouldShow = showReports.get();
            } else {
                shouldShow = true; // 其他类型默认显示
            }

            point.setVisible(shouldShow && visible.get());
        }
        LogUtils.info("NavPointLayerModel-setupTypeFilterListeners-类型监听");

    }

    /**
     * 添加导航点
     */
    public void addNavPoint(NavPointModel point) {
        LogUtils.info("NavPointLayerModel-addNavPoint-添加导航点");

        navPointsProperty.add(point);
        updatePointVisibility(point);
    }

    /**
     * 批量添加导航点
     */
    public void addNavPoints(List<? extends PointModel> points) {
        LogUtils.info("NavPointLayerModel-addNavPoints-批量添加导航点");

        navPointsProperty.addAll((Collection<? extends NavPointModel>) points);
        updatePointsVisibility();
    }

    /**
     * 更新单个点的可见性
     */
    private void updatePointVisibility(NavPointModel point) {
        LogUtils.info("NavPointLayerModel-updatePointVisibility-更新单个点的可见性");

        String type = point.getType().toUpperCase();
        boolean shouldShow = false;

        if (type.contains("AIRPORT")) {
            shouldShow = showAirports.get();
        } else if (type.contains("VOR")) {
            shouldShow = showVors.get();
        } else if (type.contains("NDB")) {
            shouldShow = showNdbs.get();
        } else if (type.contains("REPORT")) {
            shouldShow = showReports.get();
        } else {
            shouldShow = true;
        }

        point.setVisible(shouldShow && visible.get());
        LogUtils.info("NavPointLayerModel-updatePointVisibility-更新单个点的可见性");

    }

    /**
     * 清除所有导航点
     */
    public void clearNavPoints() {
        LogUtils.info("NavPointLayerModel-clearNavPoints-清除所有导航点");

        navPointsProperty.clear();
    }

    /**
     * 获取可见的导航点
     */
    public ObservableList<NavPointModel> getVisibleNavPoints() {
        LogUtils.info("NavPointLayerModel-getVisibleNavPoints-获取可见的导航点");

        ObservableList<NavPointModel> visiblePoints = FXCollections.observableArrayList();
        for (NavPointModel point : navPointsProperty) {
            if (point.isVisible()) {
                visiblePoints.add(point);
            }
        }
        return visiblePoints;
    }

    // Getters and Setters
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public File getFile() {
        return file.get();
    }

    public void setFile(File file) {
        this.file.set(file);
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
        updatePointsVisibility();
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public double getOpacity() {
        return opacity.get();
    }

    public void setOpacity(double opacity) {
        this.opacity.set(opacity);
    }

    public DoubleProperty opacityProperty() {
        return opacity;
    }

    public ObservableList<NavPointModel> getNavPoints() {
        return navPointsProperty.get();
    }

    public ListProperty<NavPointModel> navPointsProperty() {
        return navPointsProperty;
    }

    public boolean isShowAirports() {
        return showAirports.get();
    }

    public void setShowAirports(boolean show) {
        showAirports.set(show);
    }

    public BooleanProperty showAirportsProperty() {
        return showAirports;
    }

    public boolean isShowVors() {
        return showVors.get();
    }

    public void setShowVors(boolean show) {
        showVors.set(show);
    }

    public BooleanProperty showVorsProperty() {
        return showVors;
    }

    public boolean isShowNdbs() {
        return showNdbs.get();
    }

    public void setShowNdbs(boolean show) {
        showNdbs.set(show);
    }

    public BooleanProperty showNdbsProperty() {
        return showNdbs;
    }

    public boolean isShowReports() {
        return showReports.get();
    }

    public void setShowReports(boolean show) {
        showReports.set(show);
    }

    public BooleanProperty showReportsProperty() {
        return showReports;
    }

    @Override
    public String toString() {
        return String.format("%s (%d points)", getName(), navPointsProperty.size());
    }
}
