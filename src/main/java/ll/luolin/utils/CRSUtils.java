// utils/CRSUtils.java
package ll.luolin.utils;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * 坐标系工具类
 */
public class CRSUtils {
    
    /**
     * 安全获取WGS84坐标系
     */
    public static CoordinateReferenceSystem getWGS84() {
        LogUtils.info("CRSUtils-getWGS84-安全获取WGS84坐标系");

        try {
            return CRS.decode("EPSG:4326", true);
        } catch (FactoryException e) {
            try {
                return DefaultGeographicCRS.WGS84;
            } catch (Exception e2) {
                throw new RuntimeException("无法创建WGS84坐标系", e2);
            }
        }
    }
    
    /**
     * 获取坐标转换
     */
    public static MathTransform getTransform(CoordinateReferenceSystem sourceCRS, 
                                            CoordinateReferenceSystem targetCRS) {
        LogUtils.info("CRSUtils-MathTransform-获取坐标转换");

        try {
            return CRS.findMathTransform(sourceCRS, targetCRS, true);
        } catch (FactoryException e) {
            throw new RuntimeException("无法创建坐标转换", e);
        }
    }
    
    /**
     * 检查两个CRS是否相同
     */
    public static boolean isSameCRS(CoordinateReferenceSystem crs1, 
                                   CoordinateReferenceSystem crs2) {
        LogUtils.info("CRSUtils-isSameCRS-检查两个CRS是否相同");

        if (crs1 == crs2) return true;
        if (crs1 == null || crs2 == null) return false;
        
        try {
            return CRS.equalsIgnoreMetadata(crs1, crs2);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取CRS的名称
     */
    public static String getCRSName(CoordinateReferenceSystem crs) {
        LogUtils.info("CRSUtils-getCRSName-获取CRS的名称");

        if (crs == null) return "未知";
        
        try {
            return CRS.lookupIdentifier(crs, true);
        } catch (FactoryException e) {
            return crs.getName().toString();
        }
    }
}
