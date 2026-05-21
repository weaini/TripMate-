package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.StrategyCreateRequest;
import com.huashidai.lvyouapp.dto.StrategyResponse;
import com.huashidai.lvyouapp.service.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 攻略控制器
 */
@RestController
@RequestMapping("/api/strategies")
@RequiredArgsConstructor
public class StrategyController {
    
    private final StrategyService strategyService;
    
    /**
     * 创建攻略
     */
    @PostMapping
    public ResponseEntity<StrategyResponse> createStrategy(@Valid @RequestBody StrategyCreateRequest request) {
        StrategyResponse response = strategyService.createStrategy(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 上传攻略图片
     */
    @PostMapping("/{strategyId}/images")
    public ResponseEntity<?> uploadImages(@PathVariable Long strategyId, @RequestParam("files") List<MultipartFile> files) {
        strategyService.uploadImages(strategyId, files);
        return ResponseEntity.ok("图片上传成功");
    }
    
    /**
     * 获取攻略列表
     */
    @GetMapping
    public ResponseEntity<Page<StrategyResponse>> getStrategies(Pageable pageable) {
        Page<StrategyResponse> response = strategyService.getStrategies(pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取攻略详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<StrategyResponse> getStrategy(@PathVariable Long id) {
        StrategyResponse response = strategyService.getStrategy(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 点赞攻略
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeStrategy(@PathVariable Long id) {
        strategyService.likeStrategy(id);
        return ResponseEntity.ok("点赞成功");
    }
    
    /**
     * 取消点赞
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikeStrategy(@PathVariable Long id) {
        strategyService.unlikeStrategy(id);
        return ResponseEntity.ok("取消点赞成功");
    }
    
    /**
     * 删除攻略
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStrategy(@PathVariable Long id) {
        strategyService.deleteStrategy(id);
        return ResponseEntity.ok("删除成功");
    }
}
