package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.AttractionResponse;
import com.huashidai.lvyouapp.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 景点公开控制器（用户查看和收藏）
 */
@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {
    
    private final AttractionService attractionService;
    
    /**
     * 获取景点列表（公开）
     */
    @GetMapping
    public ResponseEntity<Page<AttractionResponse>> getAttractions(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String sort,
            Pageable pageable) {
        Page<AttractionResponse> response = attractionService.getAttractions(
                keyword, type, city, province, level, sort, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取景点详情（公开）
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttractionResponse> getAttraction(@PathVariable Long id) {
        AttractionResponse response = attractionService.getAttraction(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 收藏景点
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> favoriteAttraction(@PathVariable Long id) {
        attractionService.favoriteAttraction(id);
        return ResponseEntity.ok("收藏成功");
    }
    
    /**
     * 取消收藏景点
     */
    @DeleteMapping("/{id}/favorite")
    public ResponseEntity<?> unfavoriteAttraction(@PathVariable Long id) {
        attractionService.unfavoriteAttraction(id);
        return ResponseEntity.ok("取消收藏成功");
    }
    
    /**
     * 获取我的收藏列表
     */
    @GetMapping("/favorites")
    public ResponseEntity<Page<AttractionResponse>> getMyFavorites(Pageable pageable) {
        Page<AttractionResponse> response = attractionService.getUserFavorites(pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 检查是否已收藏
     */
    @GetMapping("/{id}/favorite/status")
    public ResponseEntity<Boolean> isFavorited(@PathVariable Long id) {
        boolean isFavorited = attractionService.isFavorited(id);
        return ResponseEntity.ok(isFavorited);
    }
}

