package com.huashidai.lvyouapp.util;

import com.huashidai.lvyouapp.config.UploadStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 文件存储工具类
 */
@Slf4j
public class FileStorageUtil {

    /**
     * 保存文件到本地存储
     */
    public static String saveFile(MultipartFile file, String folder) {
        try {
            Path uploadPath = UploadStorageProperties.resolveFolderPath(folder);
            Files.createDirectories(uploadPath);

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 返回相对路径
            return "/uploads/" + folder + "/" + fileName;

        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw new RuntimeException("文件保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path fullPath = UploadStorageProperties.resolveResourcePath(filePath);
            if (fullPath == null) {
                return false;
            }

            return Files.deleteIfExists(fullPath);
        } catch (Exception e) {
            log.error("文件删除失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 检查文件是否存在
     */
    public static boolean fileExists(String filePath) {
        try {
            Path fullPath = UploadStorageProperties.resolveResourcePath(filePath);
            if (fullPath == null) {
                return false;
            }

            return Files.exists(fullPath);
        } catch (Exception e) {
            log.error("检查文件存在性失败: {}", filePath, e);
            return false;
        }
    }
    
    /**
     * 获取文件大小
     */
    public static long getFileSize(String filePath) {
        try {
            Path fullPath = UploadStorageProperties.resolveResourcePath(filePath);
            if (fullPath == null) {
                return 0;
            }

            return Files.exists(fullPath) ? Files.size(fullPath) : 0;
        } catch (Exception e) {
            log.error("获取文件大小失败: {}", filePath, e);
            return 0;
        }
    }
    
    /**
     * 验证文件类型
     */
    public static boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * 验证文件大小
     */
    public static boolean isValidFileSize(MultipartFile file, long maxSizeInBytes) {
        if (file == null) {
            return false;
        }
        
        return file.getSize() <= maxSizeInBytes;
    }
}