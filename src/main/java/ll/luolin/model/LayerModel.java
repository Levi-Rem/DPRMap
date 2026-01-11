// model/LayerModel.java
package ll.luolin.model;

import javafx.beans.property.*;
import ll.luolin.utils.LogUtils;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.styling.Style;
import java.io.File;

/**
 * 图层数据模型
 */
public class LayerModel {
    private final StringProperty name = new SimpleStringProperty();
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    private final BooleanProperty selectable = new SimpleBooleanProperty(true);
    private final DoubleProperty opacity = new SimpleDoubleProperty(1.0);

    // 数据源
    private SimpleFeatureSource featureSource;
    private Style style;

    // 元数据
    private String geometryType;
    private int featureCount;
    private String crs;

    public LayerModel(String name, File file) {
        LogUtils.info("LayerModel-LayerModel-初始化");
        this.name.set(name);
        this.file.set(file);
    }

    // Getter/Setter方法
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public File getFile() { return file.get(); }
    public void setFile(File file) { this.file.set(file); }
    public ObjectProperty<File> fileProperty() { return file; }

    public boolean isVisible() { return visible.get(); }
    public void setVisible(boolean visible) { this.visible.set(visible); }
    public BooleanProperty visibleProperty() { return visible; }

    public boolean isSelectable() { return selectable.get(); }
    public void setSelectable(boolean selectable) { this.selectable.set(selectable); }
    public BooleanProperty selectableProperty() { return selectable; }

    public double getOpacity() { return opacity.get(); }
    public void setOpacity(double opacity) { this.opacity.set(opacity); }
    public DoubleProperty opacityProperty() { return opacity; }

    public SimpleFeatureSource getFeatureSource() { return featureSource; }
    public void setFeatureSource(SimpleFeatureSource featureSource) {
        this.featureSource = featureSource;
    }

    public Style getStyle() { return style; }
    public void setStyle(Style style) { this.style = style; }

    public String getGeometryType() { return geometryType; }
    public void setGeometryType(String geometryType) { this.geometryType = geometryType; }

    public int getFeatureCount() { return featureCount; }
    public void setFeatureCount(int featureCount) { this.featureCount = featureCount; }

    public String getCrs() { return crs; }
    public void setCrs(String crs) { this.crs = crs; }

    @Override
    public String toString() {
        return String.format("%s (%s, %d features)",
                getName(), getGeometryType(), getFeatureCount());
    }
}
