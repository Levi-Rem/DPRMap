// service/AutoLoadService.java
package ll.luolin.service;

import ll.luolin.model.NavPointLayerModel;
import ll.luolin.model.ASFModel.NavPointModel;
import ll.luolin.service.parserService.CHARACTERISTIC_POINTS_Service;
import ll.luolin.utils.LogUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动加载服务（简化版）
 * 只负责读取指定路径下的ASF文件并自动加载
 */
public class AutoLoadService {
    private static AutoLoadService instance;
    private final CHARACTERISTIC_POINTS_Service CHARACTERISTICPOINTSService = CHARACTERISTIC_POINTS_Service.getInstance();

    // 默认ASF文件路径
    private static final String DEFAULT_ASF_PATH = "ASF/OLD-GZTM/GLOBAL";

    private AutoLoadService() {}

    public static synchronized AutoLoadService getInstance() {
        LogUtils.info("AutoLoadService-AutoLoadService-初始化AutoLoadService");

        if (instance == null) {
            instance = new AutoLoadService();
        }
        return instance;
    }

    /**
     * 获取所有自动加载的图层
     */
    public List<NavPointLayerModel> getAllAutoLoadedLayers() {
        LogUtils.info("AutoLoadService-getAllAutoLoadedLayers-获取所有自动加载的图层");

        return loadAsfFilesFromDirectory(DEFAULT_ASF_PATH);
    }

    /**
     * 从指定路径加载ASF文件
     */
    public List<NavPointLayerModel> loadAsfFilesFromDirectory(String directoryPath) {
        LogUtils.info("AutoLoadService-loadAsfFilesFromDirectory-从指定路径加载ASF文件");

        List<NavPointLayerModel> loadedLayers = new ArrayList<>();

        try {
            File asfDir = new File(directoryPath);

            // 检查目录是否存在
            if (!asfDir.exists() || !asfDir.isDirectory()) {
                LogUtils.warn("ASF目录不存在或不是目录: " + directoryPath);
                return loadedLayers;
            }

            // 获取所有ASF文件
            File[] asfFiles = asfDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".asf") ||
                            name.toLowerCase().endsWith(".txt")
            );

            if (asfFiles == null || asfFiles.length == 0) {
                LogUtils.info("目录中没有找到ASF文件: " + directoryPath);
                return loadedLayers;
            }

            LogUtils.info(String.format("找到 %d 个ASF文件", asfFiles.length));

            for (File file : asfFiles) {
                try {
                    // 解析文件
                    List<NavPointModel> points = CHARACTERISTIC_POINTS_Service.getInstance().parserFile(file);

                    if (!points.isEmpty()) {
                        // 创建图层
                        NavPointLayerModel layer = new NavPointLayerModel(
                                getLayerName(file),
                                file
                        );
                        layer.addNavPoints(points);

                        loadedLayers.add(layer);

                        LogUtils.info(String.format("自动加载文件: %s (%d 个点)",
                                file.getName(), points.size()));
                    }

                } catch (Exception e) {
                    LogUtils.error("自动加载文件失败: " + file.getName(), e);
                }
            }

        } catch (Exception e) {
            LogUtils.error("加载目录文件失败", e);
        }

        return loadedLayers;
    }

    /**
     * 获取图层名称（去掉文件扩展名）
     */
    private String getLayerName(File file) {
        LogUtils.info("AutoLoadService-getLayerName-获取图层名称（去掉文件扩展名）");

        String name = file.getName();
        if (name.toLowerCase().endsWith(".asf")) {
            return name.substring(0, name.length() - 4);
        } else if (name.toLowerCase().endsWith(".txt")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }

    /**
     * 设置ASF文件路径
     */
    public void setAsfDirectory(String path) {
        LogUtils.info("AutoLoadService-setAsfDirectory-设置ASF文件路径");

        // 这里可以添加路径设置逻辑
        // 目前使用默认路径
    }

    /**
     * 重新加载所有ASF文件
     */
    public List<NavPointLayerModel> reloadAllLayers() {
        LogUtils.info("AutoLoadService-reloadAllLayers-重新加载所有ASF文件");

        // 清除缓存
        CHARACTERISTICPOINTSService.clearCache();

        // 重新加载
        return getAllAutoLoadedLayers();
    }
}
