// model/AirspaceModel.java
package ll.luolin.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

/**
 * 空域模型（复用LayerModel的设计模式）
 */
public class AirspaceModel {
    public enum AirspaceType {
        FIR("飞行情报区"),
        SECTOR("扇区"),
        EUROCAT_T_AREA("EUROCAT_T区域"),
        VOLUME("空域体积"),
        UNKNOWN("未知");
        
        private final String displayName;
        
        AirspaceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static AirspaceType fromString(String type) {
            if (type == null) return UNKNOWN;
            
            String upperType = type.toUpperCase();
            if (upperType.contains("FIR")) {
                return FIR;
            } else if (upperType.contains("SECTOR")) {
                return SECTOR;
            } else if (upperType.contains("EUROCAT_T_AREA") || upperType.contains("EUROCAT")) {
                return EUROCAT_T_AREA;
            } else if (upperType.contains("VOLUME")) {
                return VOLUME;
            } else {
                return UNKNOWN;
            }
        }
    }
    
    // 基本属性（复用LayerModel的命名模式）
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<AirspaceType> type = new SimpleObjectProperty<>(AirspaceType.UNKNOWN);
    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    private final DoubleProperty opacity = new SimpleDoubleProperty(0.3);
    
    // 坐标点列表（用于绘制多边形）
    private final ObservableList<CoordinatePoint> points = FXCollections.observableArrayList();
    
    // 样式属性
    private final ObjectProperty<javafx.scene.paint.Color> fillColor = 
        new SimpleObjectProperty<>(javafx.scene.paint.Color.TRANSPARENT);
    private final ObjectProperty<javafx.scene.paint.Color> strokeColor = 
        new SimpleObjectProperty<>(javafx.scene.paint.Color.BLACK);
    private final DoubleProperty strokeWidth = new SimpleDoubleProperty(1.0);
    
    public AirspaceModel() {
        // 设置默认样式
        type.addListener((obs, oldVal, newVal) -> setDefaultStyle(newVal));
    }
    
    public AirspaceModel(String name, AirspaceType type) {
        this();
        this.name.set(name);
        this.type.set(type);
        setDefaultStyle(type);
    }
    
    private void setDefaultStyle(AirspaceType type) {
        switch (type) {
            case FIR:
                fillColor.set(javafx.scene.paint.Color.rgb(255, 255, 200, 0.2)); // 浅黄色半透明
                strokeColor.set(javafx.scene.paint.Color.rgb(255, 200, 0)); // 橙色
                strokeWidth.set(2.0);
                break;
            case SECTOR:
                fillColor.set(javafx.scene.paint.Color.rgb(200, 255, 200, 0.2)); // 浅绿色半透明
                strokeColor.set(javafx.scene.paint.Color.rgb(0, 150, 0)); // 绿色
                strokeWidth.set(1.5);
                break;
            case EUROCAT_T_AREA:
                fillColor.set(javafx.scene.paint.Color.rgb(200, 200, 255, 0.2)); // 浅蓝色半透明
                strokeColor.set(javafx.scene.paint.Color.rgb(0, 0, 255)); // 蓝色
                strokeWidth.set(1.0);
                break;
            case VOLUME:
                fillColor.set(javafx.scene.paint.Color.rgb(255, 200, 200, 0.1)); // 浅红色半透明
                strokeColor.set(javafx.scene.paint.Color.rgb(255, 0, 0)); // 红色
                strokeWidth.set(0.5);
                break;
            default:
                fillColor.set(javafx.scene.paint.Color.TRANSPARENT);
                strokeColor.set(javafx.scene.paint.Color.GRAY);
                strokeWidth.set(1.0);
        }
    }
    
    // 坐标点内部类
    public static class CoordinatePoint {
        private final double longitude;
        private final double latitude;
        
        public CoordinatePoint(double longitude, double latitude) {
            this.longitude = longitude;
            this.latitude = latitude;
        }
        
        public double getLongitude() { return longitude; }
        public double getLatitude() { return latitude; }
        
        @Override
        public String toString() {
            return String.format("%.6f°E, %.6f°N", longitude, latitude);
        }
    }
    
    // 添加点
    public void addPoint(double lon, double lat) {
        points.add(new CoordinatePoint(lon, lat));
    }
    
    public void addPoint(CoordinatePoint point) {
        points.add(point);
    }
    
    public void addPoints(List<CoordinatePoint> points) {
        this.points.addAll(points);
    }
    
    // Getters and Setters
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }
    
    public AirspaceType getType() { return type.get(); }
    public void setType(AirspaceType type) { this.type.set(type); }
    public ObjectProperty<AirspaceType> typeProperty() { return type; }
    
    public boolean isVisible() { return visible.get(); }
    public void setVisible(boolean visible) { this.visible.set(visible); }
    public BooleanProperty visibleProperty() { return visible; }
    
    public double getOpacity() { return opacity.get(); }
    public void setOpacity(double opacity) { this.opacity.set(opacity); }
    public DoubleProperty opacityProperty() { return opacity; }
    
    public ObservableList<CoordinatePoint> getPoints() { return points; }
    
    public javafx.scene.paint.Color getFillColor() { return fillColor.get(); }
    public void setFillColor(javafx.scene.paint.Color color) { this.fillColor.set(color); }
    public ObjectProperty<javafx.scene.paint.Color> fillColorProperty() { return fillColor; }
    
    public javafx.scene.paint.Color getStrokeColor() { return strokeColor.get(); }
    public void setStrokeColor(javafx.scene.paint.Color color) { this.strokeColor.set(color); }
    public ObjectProperty<javafx.scene.paint.Color> strokeColorProperty() { return strokeColor; }
    
    public double getStrokeWidth() { return strokeWidth.get(); }
    public void setStrokeWidth(double width) { this.strokeWidth.set(width); }
    public DoubleProperty strokeWidthProperty() { return strokeWidth; }
    
    @Override
    public String toString() {
        return String.format("%s (%s, %d points)", 
                getName(), getType().getDisplayName(), points.size());
    }
}
