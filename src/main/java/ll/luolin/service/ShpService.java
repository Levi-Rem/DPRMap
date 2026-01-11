// service/ShpService.java
package ll.luolin.service;

import ll.luolin.model.LayerModel;
import ll.luolin.utils.LogUtils;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.util.URLs;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SHP文件服务
 * 负责加载和管理SHP文件
 */
public class ShpService {
    private static ShpService instance;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    
    private ShpService() {}
    
    public static synchronized ShpService getInstance() {
        LogUtils.info("ShpService-getInstance-单例");

        if (instance == null) {
            instance = new ShpService();
        }
        return instance;
    }
    
    public LayerModel loadShpFile(File file) throws Exception {
        LogUtils.info("ShpService-loadShpFile-加载shp文件");

        String cacheKey = file.getAbsolutePath();
        CacheEntry cached = cache.get(cacheKey);
        
        // 检查缓存
        if (cached != null && !isFileModified(file, cached)) {
            return createLayerModel(file, cached.featureSource);
        }
        
        // 加载SHP文件
        SimpleFeatureSource featureSource = loadFeatureSource(file);
        
        // 更新缓存
        cache.put(cacheKey, new CacheEntry(featureSource, file.lastModified(), file.length()));
        
        return createLayerModel(file, featureSource);
    }
    
    private SimpleFeatureSource loadFeatureSource(File file) throws Exception {
        LogUtils.info("ShpService-loadFeatureSource-加载特性源文件");

        Map<String, Object> params = new HashMap<>();
        params.put("url", URLs.fileToUrl(file));
        
        DataStore dataStore = DataStoreFinder.getDataStore(params);
        if (dataStore == null) {
            throw new RuntimeException("无法创建DataStore: " + file.getAbsolutePath());
        }
        
        // 设置字符编码（处理中文）
        if (dataStore instanceof org.geotools.data.shapefile.ShapefileDataStore) {
            ((org.geotools.data.shapefile.ShapefileDataStore) dataStore)
                .setCharset(Charset.forName("GBK"));
        }
        
        String typeName = dataStore.getTypeNames()[0];
        return dataStore.getFeatureSource(typeName);
    }
    
    private LayerModel createLayerModel(File file, SimpleFeatureSource featureSource) 
            throws Exception {
        LogUtils.info("ShpService-createLayerModel-创建图层模型");

        LayerModel layer = new LayerModel(
            file.getName().replace(".shp", ""),
            file
        );
        
        layer.setFeatureSource(featureSource);
        layer.setFeatureCount(featureSource.getCount(org.geotools.data.Query.ALL));
        
        // 获取几何类型
        String geometryType = featureSource.getSchema()
            .getGeometryDescriptor()
            .getType()
            .getBinding()
            .getSimpleName();
        layer.setGeometryType(geometryType);
        
        // 获取坐标系
        CoordinateReferenceSystem crs = featureSource.getSchema()
            .getCoordinateReferenceSystem();
        if (crs != null) {
            layer.setCrs(org.geotools.referencing.CRS.lookupIdentifier(crs, true));
        }
        
        // 创建默认样式
        layer.setStyle(StyleService.getInstance().createStyle(geometryType));
        
        return layer;
    }
    
    private boolean isFileModified(File file, CacheEntry cached) {
        LogUtils.info("ShpService-isFileModified-文件是否被修改");

        return file.lastModified() > cached.lastModified || 
               file.length() != cached.fileSize;
    }
    
    public void clearCache() {
        cache.clear();
    }
    
    public void clearCache(String filePath) {
        cache.remove(filePath);
    }
    
    // 缓存条目
    private static class CacheEntry {

        final SimpleFeatureSource featureSource;
        final long lastModified;
        final long fileSize;
        
        CacheEntry(SimpleFeatureSource featureSource, long lastModified, long fileSize) {
            LogUtils.info("ShpService-CacheEntry-CacheEntry-缓存");

            this.featureSource = featureSource;
            this.lastModified = lastModified;
            this.fileSize = fileSize;
        }
    }
}
