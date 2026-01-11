// service/RenderService.java
package ll.luolin.service;

import javafx.scene.canvas.GraphicsContext;
import ll.luolin.model.LayerModel;
import ll.luolin.model.MapModel;
import ll.luolin.utils.CRSUtils;
import ll.luolin.utils.LogUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * 渲染服务
 * 负责地理要素的渲染
 */
public class RenderService {
    private static RenderService instance;
    
    private RenderService() {}
    
    public static synchronized RenderService getInstance() {
        if (instance == null) {
            instance = new RenderService();
        }
        return instance;
    }
    
    /**
     * 渲染图层
     */
    public void renderLayer(GraphicsContext gc, LayerModel layer, MapModel mapModel) {
        LogUtils.info("NavPointRenderService-renderLayer-渲染图层");

        if (layer.getFeatureSource() == null || layer.getStyle() == null) {
            return;
        }
        
        try {
            // 创建MapContent
            MapContent mapContent = new MapContent();
            mapContent.setTitle(layer.getName());
            
            // 创建FeatureLayer
            FeatureLayer featureLayer = new FeatureLayer(
                layer.getFeatureSource(),
                layer.getStyle()
            );
            mapContent.addLayer(featureLayer);
            
            // 设置渲染范围
            ReferencedEnvelope mapBounds = calculateRenderBounds(mapModel, gc.getCanvas());
            mapContent.getViewport().setBounds(mapBounds);
            
            // 创建渲染器
            GTRenderer renderer = new StreamingRenderer();
            renderer.setMapContent(mapContent);
            
            // 创建AWT图像
            BufferedImage bufferedImage = createBufferedImage(
                (int) gc.getCanvas().getWidth(),
                (int) gc.getCanvas().getHeight()
            );
            
            // 渲染到AWT图像
            Graphics2D awtGraphics = bufferedImage.createGraphics();
            renderer.paint(awtGraphics, 
                new Rectangle(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight()),
                mapBounds);
            awtGraphics.dispose();
            
            // 转换为JavaFX图像并绘制
            javafx.scene.image.Image fxImage = convertToFXImage(bufferedImage);
            gc.drawImage(fxImage, 0, 0);
            
            // 清理资源
            mapContent.dispose();
            
        } catch (Exception e) {
            LogUtils.error("渲染图层失败: " + layer.getName(), e);
        }
    }
    
    /**
     * 计算渲染范围
     */
    private ReferencedEnvelope calculateRenderBounds(MapModel mapModel, javafx.scene.canvas.Canvas canvas) {
        LogUtils.info("NavPointRenderService-calculateRenderBounds-计算渲染范围");

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        
        // 计算可见范围
        double minLon = mapModel.pixelToLon(0);
        double maxLon = mapModel.pixelToLon(canvasWidth);
        double minLat = mapModel.pixelToLat(canvasHeight);
        double maxLat = mapModel.pixelToLat(0);
        
        return new ReferencedEnvelope(minLon, maxLon, minLat, maxLat, 
            CRSUtils.getWGS84());
    }
    
    /**
     * 创建BufferedImage
     */
    private BufferedImage createBufferedImage(int width, int height) {
        LogUtils.info("NavPointRenderService-createBufferedImage-创建BufferedImage");

        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }
    
    /**
     * 转换为JavaFX图像
     */
    private javafx.scene.image.Image convertToFXImage(BufferedImage awtImage) {
        LogUtils.info("NavPointRenderService-convertToFXImage-转换为JavaFX图像");

        return javafx.embed.swing.SwingFXUtils.toFXImage(awtImage, null);
    }
    
    /**
     * 批量渲染图层
     */
    public void renderLayers(GraphicsContext gc, java.util.List<LayerModel> layers, 
                            MapModel mapModel) {
        LogUtils.info("NavPointRenderService-renderLayers-批量渲染图层");

        // 按顺序渲染所有可见图层
        for (LayerModel layer : layers) {
            if (layer.isVisible()) {
                renderLayer(gc, layer, mapModel);
            }
        }
    }
}
