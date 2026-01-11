// Main.java - 简化版
package ll.luolin;

import ll.luolin.view.MapView;
import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        // 设置必要的系统属性
        System.setProperty("org.geotools.referencing.forceXY", "true");
        System.setProperty("java.awt.headless", "false");

        // 启动JavaFX应用
        Application.launch(MapView.class, args);
    }
}
