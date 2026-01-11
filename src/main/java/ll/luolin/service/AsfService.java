// service/AsfService.java
package ll.luolin.service;

import ll.luolin.model.NavPointModel;
import ll.luolin.utils.CoordinateParser;
import ll.luolin.utils.IconFactory;
import ll.luolin.utils.LogUtils;

import javax.validation.constraints.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ASF文件解析服务
 */
public class AsfService {
    private static AsfService instance;
    private final ConcurrentHashMap<String, List<NavPointModel>> fileCache = new ConcurrentHashMap<>();

    private AsfService() {
    }

    public static synchronized AsfService getInstance() {
        LogUtils.info("AsfService-AsfService-单例初始化");
        if (instance == null) {
            instance = new AsfService();
        }
        return instance;
    }

    /**
     * 解析ASF文件
     */
    public List<NavPointModel> parseAsfFile(@NotNull File file) throws Exception {
        LogUtils.info("AsfService-parseAsfFile-解析ASF文件");

        String cacheKey = file.getAbsolutePath();

        // 检查缓存
        List<NavPointModel> cached = fileCache.get(cacheKey);
        if (cached != null) {
            return new ArrayList<>(cached); // 返回副本
        }

        List<NavPointModel> navPoints = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {

            String line;
            boolean inDefinitions = false;
//            boolean inDataSection = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过空行
                if (line.isEmpty()) {
                    continue;
                }

                // 跳过注释行
                if (line.startsWith("--")) {
                    continue;
                }

                // 检查是否进入定义部分
                if (line.startsWith("/DEFINITIONS/")) {
                    inDefinitions = true;
                    continue;
                }

//                // 检查是否进入数据部分
//                if (line.startsWith("--Name | Lat/Long") ||
//                    line.startsWith("-- 1   | 2")) {
//                    inDataSection = true;
//                    continue;
//                }

                // 跳过分隔线
                if (line.startsWith("--") && line.contains("---")) {
                    continue;
                }

                // 解析数据行
                if (inDefinitions && !line.startsWith("--")) {
                    NavPointModel point = parseDataLine(line);
                    if (point != null) {
                        navPoints.add(point);
                    }
                }
            }

            LogUtils.info(String.format("解析ASF文件完成: %s, 找到 %d 个导航点",
                    file.getName(), navPoints.size()));

            // 缓存结果
            fileCache.put(cacheKey, new ArrayList<>(navPoints));

            return navPoints;

        } catch (Exception e) {
            LogUtils.error("AsfService-parseAsfFile-解析ASF文件");

            LogUtils.error("解析ASF文件失败: " + file.getAbsolutePath(), e);
            throw e;
        }


    }

    /**
     * 解析数据行
     * 格式：ZSWH  | 371112N1221341E | AIRPORT_I | Y |                          | N |  N |  |
     */
    private NavPointModel parseDataLine(String line) {
        try {
            LogUtils.info("AsfService-parseDataLine-解析数据行" + "------" + line);

            // 使用管道符分割
            String[] parts = line.split("\\|");
            if (parts.length < 3) {
                return null;
            }

            // 提取字段
            String name = parts[0].trim();
            String coordinate = parts[1].trim();
            String type = parts[2].trim();

            // 验证必要字段
            if (name.isEmpty() || coordinate.isEmpty() || type.isEmpty()) {
                return null;
            }

            // 解析坐标
            double[] coords = CoordinateParser.parseDMS(coordinate);
            if (coords[0] == 0.0 && coords[1] == 0.0) {
                LogUtils.warn("坐标解析失败，跳过: " + name + " - " + coordinate);
                return null;
            }

            // 创建导航点模型
            NavPointModel point = new NavPointModel();
            point.setName(name);
            point.setLongitude(coords[0]);
            point.setLatitude(coords[1]);
            point.setType(type);

            // 设置图标
            setIconForType(point);
            LogUtils.info("AsfService-parseDataLine-解析数据行");

            return point;

        } catch (Exception e) {
            LogUtils.error("解析数据行失败: " + line, e);
            return null;
        }
    }

    // 修改AsfService.java中的setIconForType方法
    private void setIconForType(NavPointModel point) {

        LogUtils.info("AsfService-setIconForType-修改AsfService.java中的setIconForType方法");

        String type = point.getType().toUpperCase();

        try {
            // 根据类型选择图标
            if (type.contains("AIRPORT")) {
                point.setIcon(IconFactory.createAirportIcon());
            } else if (type.contains("VOR") || type.contains("VORDME")) {
                point.setIcon(IconFactory.createVorIcon());
            } else if (type.contains("NDB")) {
                point.setIcon(IconFactory.createNdbIcon());
            } else if (type.contains("REPORT")) {
                point.setIcon(IconFactory.createReportIcon());
            } else {
                point.setIcon(IconFactory.createPointIcon());
            }
        } catch (Exception e) {
            LogUtils.warn("创建图标失败: " + type);
            point.setIcon(IconFactory.createPointIcon());
        }
    }

    /**
     * 批量解析ASF文件
     */
    public List<NavPointModel> parseAsfFiles(List<File> files) {
        LogUtils.info("AsfService-parseAsfFiles-批量解析ASF文件");

        List<NavPointModel> allPoints = new ArrayList<>();

        for (File file : files) {
            try {
                List<NavPointModel> points = parseAsfFile(file);
                allPoints.addAll(points);
            } catch (Exception e) {
                LogUtils.error("解析文件失败: " + file.getName(), e);
            }
        }

        return allPoints;
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        fileCache.clear();
    }

    /**
     * 清除指定文件的缓存
     */
    public void clearCache(String filePath) {
        fileCache.remove(filePath);
    }

    /**
     * 获取支持的导航点类型
     */
    public List<String> getSupportedTypes() {
        return List.of(
                "AIRPORT_I", "AIRPORT", "VOR", "VORDME", "NDB", "REPORT"
        );
    }

    /**
     * 按类型过滤导航点
     */
    public List<NavPointModel> filterByType(List<NavPointModel> points, String type) {
        List<NavPointModel> filtered = new ArrayList<>();
        for (NavPointModel point : points) {
            if (point.getType().equalsIgnoreCase(type)) {
                filtered.add(point);
            }
        }
        return filtered;
    }
}
