// model/NavPointModel.java
package ll.luolin.model.ASFModel;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import ll.luolin.utils.LogUtils;

/**
 * 导航点数据模型
 */
public class NavPointModel extends PointModel {
    private final StringProperty name = new SimpleStringProperty();      // 点名称
    private final DoubleProperty longitude = new SimpleDoubleProperty(); // 经度
    private final DoubleProperty latitude = new SimpleDoubleProperty();  // 纬度
    private final StringProperty type = new SimpleStringProperty();      // 点类型
    private final BooleanProperty visible = new SimpleBooleanProperty(true); // 是否可见

    // 图标（根据类型动态加载）
    private final ObjectProperty<Image> icon = new SimpleObjectProperty<>();

    public NavPointModel() {
        LogUtils.info("NavPointModel-NavPointModel-构造函数初始化");

        // 默认构造函数
    }

    public NavPointModel(String name, double longitude, double latitude, String type) {
        LogUtils.info("NavPointModel-NavPointModel-构造函数初始化");

        this.name.set(name);
        this.longitude.set(longitude);
        this.latitude.set(latitude);
        this.type.set(type);
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

    public double getLongitude() {
        return longitude.get();
    }

    public void setLongitude(double longitude) {
        this.longitude.set(longitude);
    }

    public DoubleProperty longitudeProperty() {
        return longitude;
    }

    public double getLatitude() {
        return latitude.get();
    }

    public void setLatitude(double latitude) {
        this.latitude.set(latitude);
    }

    public DoubleProperty latitudeProperty() {
        return latitude;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public Image getIcon() {
        return icon.get();
    }

    public void setIcon(Image icon) {
        this.icon.set(icon);
    }

    public ObjectProperty<Image> iconProperty() {
        return icon;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %.4f°E, %.4f°N",
                getName(), getType(), getLongitude(), getLatitude());
    }
}
