// service/StyleService.java
package ll.luolin.service;

import ll.luolin.utils.LogUtils;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.*;
import org.opengis.filter.FilterFactory;
import java.awt.Color;

/**
 * 样式服务
 * 修复原MapStyleFactory中的颜色兼容性问题
 */
public class StyleService {
    private static StyleService instance;
    private final StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory();
    private final FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory();
    
    private StyleService() {}
    
    public static synchronized StyleService getInstance() {
        LogUtils.info("StyleService-StyleService-单例化");

        if (instance == null) {
            instance = new StyleService();
        }
        return instance;
    }
    
    public Style createStyle(String geometryType) {
        LogUtils.info("StyleService-createStyle-地图元素类型");

        if (geometryType.contains("Polygon")) {
            return createPolygonStyle();
        } else if (geometryType.contains("Line")) {
            return createLineStyle();
        } else if (geometryType.contains("Point")) {
            return createPointStyle();
        } else {
            return createGenericStyle();
        }
    }
    
    public Style createPolygonStyle() {
        LogUtils.info("StyleService-createPolygonStyle-创建多边形");

        Stroke stroke = styleFactory.createStroke(
            filterFactory.literal(Color.BLACK),  // 使用java.awt.Color
            filterFactory.literal(1.0)
        );
        
        Fill fill = styleFactory.createFill(
            filterFactory.literal(new Color(0, 0, 0, 0))  // 半透明蓝色
//            filterFactory.literal(new Color(51, 128, 204, 128))  // 半透明蓝色
        );
        
        PolygonSymbolizer symbolizer = styleFactory.createPolygonSymbolizer(stroke, fill, null);
        return createStyleFromSymbolizer(symbolizer);
    }
    
    public Style createLineStyle() {
        Stroke stroke = styleFactory.createStroke(
            filterFactory.literal(Color.RED),
            filterFactory.literal(2.0)
        );
        
        LineSymbolizer symbolizer = styleFactory.createLineSymbolizer(stroke, null);
        return createStyleFromSymbolizer(symbolizer);
    }
    
    public Style createPointStyle() {
        LogUtils.info("StyleService-createPointStyle-创建点");

        Mark mark = styleFactory.getCircleMark();
        mark.setStroke(styleFactory.createStroke(
            filterFactory.literal(Color.BLACK),
            filterFactory.literal(1.0)
        ));
        mark.setFill(styleFactory.createFill(
            filterFactory.literal(Color.YELLOW)
        ));
        
        Graphic graphic = styleFactory.createGraphic(
            null,
            new Mark[] { mark },
            null,
            filterFactory.literal(8),
            null,
            null
        );
        
        PointSymbolizer symbolizer = styleFactory.createPointSymbolizer(graphic, null);
        return createStyleFromSymbolizer(symbolizer);
    }
    
    public Style createGenericStyle() {
        LogUtils.info("StyleService-createGenericStyle-创建通用样式");

        Stroke stroke = styleFactory.createStroke(
            filterFactory.literal(Color.BLUE),
            filterFactory.literal(1.0)
        );
        
        Fill fill = styleFactory.createFill(
            filterFactory.literal(new Color(77, 153, 230, 102))
        );
        
        PolygonSymbolizer symbolizer = styleFactory.createPolygonSymbolizer(stroke, fill, null);
        return createStyleFromSymbolizer(symbolizer);
    }
    
    private Style createStyleFromSymbolizer(Symbolizer symbolizer) {
        LogUtils.info("StyleService-createStyleFromSymbolizer-从点样式创建样式");

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(symbolizer);
        
        FeatureTypeStyle featureTypeStyle = styleFactory.createFeatureTypeStyle();
        featureTypeStyle.rules().add(rule);
        
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(featureTypeStyle);
        
        return style;
    }
    
    public Style createClassifiedStyle(String attributeName, String[] classes, Color[] colors) {
        LogUtils.info("StyleService-createClassifiedStyle-  ");

        // 实现分类渲染
        // 根据属性值创建不同颜色的样式
        return createPolygonStyle(); // 暂返回默认
    }
}
