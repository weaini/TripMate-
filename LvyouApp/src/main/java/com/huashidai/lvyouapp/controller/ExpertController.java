package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.ExpertApplicationRequest;
import com.huashidai.lvyouapp.dto.ExpertApplicationResponse;
import com.huashidai.lvyouapp.service.ExpertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 达人控制器
 */
@RestController
@RequestMapping("/api/expert")
@RequiredArgsConstructor
public class ExpertController {
    
    private final ExpertService expertService;
    
    /**
     * 申请成为旅游达人
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyForExpert(@Valid @RequestBody ExpertApplicationRequest request) {
        expertService.applyForExpert(request);
        return ResponseEntity.ok("达人申请已提交");
    }
    
    /**
     * 获取我的达人申请状态
     */
    @GetMapping("/my-application")
    public ResponseEntity<ExpertApplicationResponse> getMyApplication() {
        ExpertApplicationResponse application = expertService.getMyApplication();
        return ResponseEntity.ok(application);
    }
    
    /**
     * 取消达人申请
     */
    @DeleteMapping("/my-application")
    public ResponseEntity<?> cancelApplication() {
        expertService.cancelApplication();
        return ResponseEntity.ok("申请已取消");
    }
}
