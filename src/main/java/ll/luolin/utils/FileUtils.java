// utils/FileUtils.java
package ll.luolin.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类
 */
public class FileUtils {
    
    /**
     * 获取目录下的所有SHP文件
     */
    public static List<File> getShpFiles(String directoryPath) {
        LogUtils.info("FileUtils-getShpFiles-获取目录下的所有SHP文件");

        List<File> shpFiles = new ArrayList<>();
        File dir = new File(directoryPath);
        
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((file, name) -> 
                name.toLowerCase().endsWith(".shp")
            );
            
            if (files != null) {
                for (File file : files) {
                    if (validateShpFile(file)) {
                        shpFiles.add(file);
                    }
                }
            }
        }
        
        return shpFiles;
    }
    
    /**
     * 验证SHP文件完整性
     */
    public static boolean validateShpFile(File shpFile) {
        LogUtils.info("FileUtils-validateShpFile-验证SHP文件完整性");

        if (!shpFile.exists()) {
            return false;
        }
        
        String baseName = shpFile.getName().replace(".shp", "");
        File dir = shpFile.getParentFile();
        
        // 检查必需的辅助文件
        String[] requiredExtensions = {".shx", ".dbf"};
        for (String ext : requiredExtensions) {
            File auxFile = new File(dir, baseName + ext);
            if (!auxFile.exists()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 复制SHP文件及其辅助文件
     */
    public static void copyShpFile(File sourceFile, String destDir) throws IOException {
        LogUtils.info("FileUtils-copyShpFile-复制SHP文件及其辅助文件");

        String baseName = sourceFile.getName().replace(".shp", "");
        File sourceDir = sourceFile.getParentFile();
        
        // 需要复制的文件扩展名
        String[] extensions = {".shp", ".shx", ".dbf", ".prj", ".sbn", ".sbx", ".cpg", ".qix"};
        
        for (String ext : extensions) {
            File source = new File(sourceDir, baseName + ext);
            if (source.exists()) {
                Path dest = Paths.get(destDir, baseName + ext);
                Files.copy(source.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
    /**
     * 创建目录（如果不存在）
     */
    public static void createDirectoryIfNotExists(String path) throws IOException {
        LogUtils.info("FileUtils-createDirectoryIfNotExists-创建目录（如果不存在）");

        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(File file) {
        LogUtils.info("FileUtils-getFileExtension-获取文件扩展名");

        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1).toLowerCase() : "";
    }
}
