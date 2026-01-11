// utils/IconFactory.java
package ll.luolin.utils;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * 图标工厂（用于创建默认图标）
 */
public class IconFactory {
    
    /**
     * 创建默认图标
     */
    public static Image createDefaultIcon(Color color, int size) {
        LogUtils.info("IconFactory-createDefaultIcon-创建默认图标");

        WritableImage image = new WritableImage(size, size);
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                // 计算到中心的距离
                double centerX = size / 2.0;
                double centerY = size / 2.0;
                double distance = Math.sqrt(
                    Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)
                );
                
                if (distance <= size / 2.0) {
                    image.getPixelWriter().setColor(x, y, color);
                } else {
                    image.getPixelWriter().setColor(x, y, Color.TRANSPARENT);
                }
            }
        }
        
        return image;
    }
    
    /**
     * 创建机场图标
     */
    public static Image createAirportIcon() {
        LogUtils.info("IconFactory-createAirportIcon-创建机场图标");

        return createDefaultIcon(Color.RED, 16);
    }
    
    /**
     * 创建VOR图标
     */
    public static Image createVorIcon() {
        LogUtils.info("IconFactory-createVorIcon-创建VOR图标");

        return createDefaultIcon(Color.BLUE, 16);
    }
    
    /**
     * 创建NDB图标
     */
    public static Image createNdbIcon() {
        LogUtils.info("IconFactory-createNdbIcon-创建NDB图标");

        return createDefaultIcon(Color.GREEN, 16);
    }
    
    /**
     * 创建报告点图标
     */
    public static Image createReportIcon() {
        LogUtils.info("IconFactory-createNdbIcon-创建报告点图标");

        return createDefaultIcon(Color.ORANGE, 16);
    }
    
    /**
     * 创建默认点图标
     */
    public static Image createPointIcon() {
        LogUtils.info("IconFactory-createNdbIcon-创建默认点图标");

        return createDefaultIcon(Color.PURPLE, 16);
    }
}
