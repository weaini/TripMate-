package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.AttractionCreateRequest;
import com.huashidai.lvyouapp.dto.AttractionResponse;
import com.huashidai.lvyouapp.dto.AttractionUpdateRequest;
import com.huashidai.lvyouapp.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 管理员景点管理控制器
 */
@RestController
@RequestMapping("/api/admin/attractions")
@RequiredArgsConstructor
public class AdminAttractionController {
    
    private final AttractionService attractionService;
    
    /**
     * 创建景点
     */
    @PostMapping
    public ResponseEntity<AttractionResponse> createAttraction(
            @Valid @RequestBody AttractionCreateRequest request) {
        AttractionResponse response = attractionService.createAttraction(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新景点
     */
    @PutMapping("/{id}")
    public ResponseEntity<AttractionResponse> updateAttraction(
            @PathVariable Long id,
            @Valid @RequestBody AttractionUpdateRequest request) {
        AttractionResponse response = attractionService.updateAttraction(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 删除景点
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAttraction(@PathVariable Long id) {
        attractionService.deleteAttraction(id);
        return ResponseEntity.ok("景点删除成功");
    }
    
    /**
     * 获取所有景点（包括已删除的，用于管理）
     */
    @GetMapping
    public ResponseEntity<Page<AttractionResponse>> getAllAttractions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String sort,
            Pageable pageable) {
        Page<AttractionResponse> response = attractionService.getAllAttractionsForAdmin(
                keyword, type, city, province, level, sort, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 上传景点图片
     */
    @PostMapping("/{attractionId}/images")
    public ResponseEntity<?> uploadImages(
            @PathVariable Long attractionId,
            @RequestParam("files") List<MultipartFile> files) {
        attractionService.uploadImages(attractionId, files);
        return ResponseEntity.ok("图片上传成功");
    }
}

