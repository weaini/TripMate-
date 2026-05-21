package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.util.FileStorageUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "OK");
        return ResponseEntity.ok(resp);
    }
    
    /**
     * 通用图片上传接口（用于上传封面图片等）
     */
    @PostMapping("/upload/image")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "attractions") String folder) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "文件不能为空"));
            }
            
            if (!FileStorageUtil.isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "只能上传图片文件"));
            }
            
            if (!FileStorageUtil.isValidFileSize(file, 10 * 1024 * 1024)) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "图片大小不能超过10MB"));
            }
            
            String imageUrl = FileStorageUtil.saveFile(file, folder);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }
}

