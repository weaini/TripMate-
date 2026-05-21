package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.ActivityCreateRequest;
import com.huashidai.lvyouapp.dto.ActivityResponse;
import com.huashidai.lvyouapp.dto.ActivityUpdateRequest;
import com.huashidai.lvyouapp.dto.JoinActivityRequest;
import com.huashidai.lvyouapp.dto.ParticipantResponse;
import com.huashidai.lvyouapp.dto.RejectParticipantRequest;
import com.huashidai.lvyouapp.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 活动控制器
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    
    private final ActivityService activityService;
    
    /**
     * 创建活动
     */
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(@Valid @RequestBody ActivityCreateRequest request) {
        ActivityResponse response = activityService.createActivity(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> updateActivity(@PathVariable Long id, @Valid @RequestBody ActivityUpdateRequest request) {
        ActivityResponse response = activityService.updateActivity(id, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 上传活动图片
     */
    @PostMapping("/{activityId}/images")
    public ResponseEntity<?> uploadImages(@PathVariable Long activityId, @RequestParam("files") List<MultipartFile> files) {
        activityService.uploadImages(activityId, files);
        return ResponseEntity.ok("图片上传成功");
    }
    
    /**
     * 获取活动列表
     */
    @GetMapping
    public ResponseEntity<Page<ActivityResponse>> getActivities(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) Double minCost,
            @RequestParam(required = false) Double maxCost,
            Pageable pageable) {
        Page<ActivityResponse> response = activityService.getActivities(
                keyword, type, status, sort, startDate, minCost, maxCost, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivity(@PathVariable Long id) {
        ActivityResponse response = activityService.getActivity(id);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 报名活动
     */
    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinActivity(@PathVariable Long id, @Valid @RequestBody(required = false) JoinActivityRequest request) {
        String note = request != null ? request.getApplicationNote() : null;
        activityService.joinActivity(id, note);
        return ResponseEntity.ok("报名申请已提交，等待组织者审核");
    }
    
    /**
     * 取消报名
     */
    @DeleteMapping("/{id}/join")
    public ResponseEntity<?> cancelJoinActivity(@PathVariable Long id) {
        activityService.cancelJoinActivity(id);
        return ResponseEntity.ok("取消报名成功");
    }
    
    /**
     * 检查用户是否参与活动
     */
    @GetMapping("/{id}/participation")
    public ResponseEntity<java.util.Map<String, Object>> checkParticipation(@PathVariable Long id) {
        java.util.Map<String, Object> result = activityService.checkUserParticipation(id);
        return ResponseEntity.ok(result);
    }
    
    /**
     * 审核报名
     */
    @PostMapping("/{id}/participants/{participantId}/approve")
    public ResponseEntity<?> approveParticipant(@PathVariable Long id, @PathVariable Long participantId) {
        activityService.approveParticipant(id, participantId);
        return ResponseEntity.ok("审核通过");
    }
    
    /**
     * 拒绝报名
     */
    @PostMapping("/{id}/participants/{participantId}/reject")
    public ResponseEntity<?> rejectParticipant(@PathVariable Long id, @PathVariable Long participantId, @Valid @RequestBody RejectParticipantRequest request) {
        activityService.rejectParticipant(id, participantId, request.getReason());
        return ResponseEntity.ok("已拒绝报名");
    }
    
    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.ok("删除成功");
    }
    
    /**
     * 获取我的活动
     */
    @GetMapping("/my")
    public ResponseEntity<Page<ActivityResponse>> getMyActivities(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<ActivityResponse> response = activityService.getMyActivities(status, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取活动的参与者列表
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<Page<ParticipantResponse>> getActivityParticipants(
            @PathVariable Long id,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<ParticipantResponse> response = activityService.getActivityParticipants(id, status, pageable);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 获取我的报名记录
     */
    @GetMapping("/my-participations")
    public ResponseEntity<Page<ParticipantResponse>> getMyParticipations(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<ParticipantResponse> response = activityService.getMyParticipations(status, pageable);
        return ResponseEntity.ok(response);
    }
}
