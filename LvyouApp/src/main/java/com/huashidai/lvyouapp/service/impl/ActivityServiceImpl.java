package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.ActivityCreateRequest;
import com.huashidai.lvyouapp.dto.ActivityResponse;
import com.huashidai.lvyouapp.dto.ActivityUpdateRequest;
import com.huashidai.lvyouapp.dto.ParticipantResponse;
import com.huashidai.lvyouapp.dto.UserResponse;
import com.huashidai.lvyouapp.entity.Activity;
import com.huashidai.lvyouapp.entity.ActivityParticipant;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.exception.BusinessException;
import com.huashidai.lvyouapp.repository.ActivityParticipantRepository;
import com.huashidai.lvyouapp.repository.ActivityRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.service.ActivityService;
import com.huashidai.lvyouapp.service.PointsService;
import com.huashidai.lvyouapp.service.NotificationService;
import com.huashidai.lvyouapp.util.FileStorageUtil;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 活动服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final ActivityParticipantRepository activityParticipantRepository;
    private final PointsService pointsService;
    private final NotificationService notificationService;
    
    @Override
    public ActivityResponse createActivity(ActivityCreateRequest request) {
        // 检查用户权限 - 只有旅游达人才能创建活动
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        if (currentUser.getRole() != User.UserRole.EXPERT && currentUser.getRole() != User.UserRole.ADMIN) {
            throw new BusinessException("只有旅游达人才能创建活动");
        }
        
        // 创建活动
        Activity activity = new Activity();
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setDestination(request.getDestination());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setRegistrationDeadline(request.getRegistrationDeadline());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setCost(request.getCost());
        
        // 转换活动类型
        Activity.ActivityType activityType;
        switch (request.getType().toUpperCase()) {
            case "HIKING":
                activityType = Activity.ActivityType.HIKING;
                break;
            case "PHOTOGRAPHY":
                activityType = Activity.ActivityType.PHOTOGRAPHY;
                break;
            case "CAMPING":
                activityType = Activity.ActivityType.CAMPING;
                break;
            case "CULTURAL":
                activityType = Activity.ActivityType.CULTURAL;
                break;
            case "ADVENTURE":
                activityType = Activity.ActivityType.ADVENTURE;
                break;
            case "RELAXATION":
                activityType = Activity.ActivityType.RELAXATION;
                break;
            case "OTHER":
                activityType = Activity.ActivityType.OTHER;
                break;
            default:
                throw new IllegalArgumentException("不支持的活动类型: " + request.getType());
        }
        activity.setType(activityType);
        
        // 转换难度等级
        Activity.ActivityLevel activityLevel;
        switch (request.getLevel().toUpperCase()) {
            case "EASY":
                activityLevel = Activity.ActivityLevel.EASY;
                break;
            case "MEDIUM":
                activityLevel = Activity.ActivityLevel.MEDIUM;
                break;
            case "HARD":
                activityLevel = Activity.ActivityLevel.HARD;
                break;
            case "EXPERT":
                activityLevel = Activity.ActivityLevel.EXPERT;
                break;
            default:
                throw new IllegalArgumentException("不支持的难度等级: " + request.getLevel());
        }
        activity.setLevel(activityLevel);
        
        activity.setRequirements(request.getRequirements());
        activity.setContactInfo(request.getContactInfo());
        activity.setStatus(Activity.ActivityStatus.PENDING); // 需要审核
        activity.setCreatedAt(LocalDateTime.now());
        
        // 设置组织者
        activity.setOrganizer(currentUser);
        
        Activity savedActivity = activityRepository.save(activity);
        
        // 发布活动获得10积分
        pointsService.earnPointsForActivity(currentUser, savedActivity.getId());
        
        log.info("用户 {} 创建活动成功，活动ID: {}，获得10积分", currentUser.getUsername(), savedActivity.getId());
        
        return convertToActivityResponse(savedActivity);
    }
    
    @Override
    public ActivityResponse updateActivity(Long id, ActivityUpdateRequest request) {
        // 查找活动
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查权限 - 只有活动组织者可以编辑活动
        if (!activity.getOrganizer().getId().equals(currentUser.getId())) {
            throw new BusinessException("只有活动组织者可以编辑活动");
        }
        
        // 检查活动状态 - 只有待审核或已审核的活动可以编辑
        if (activity.getStatus() == Activity.ActivityStatus.CANCELLED) {
            throw new BusinessException("已取消的活动不能编辑");
        }
        
        // 更新活动信息
        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setDestination(request.getDestination());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setRegistrationDeadline(request.getRegistrationDeadline());
        activity.setMaxParticipants(request.getMaxParticipants());
        activity.setCost(request.getCost());
        
        // 转换活动类型
        Activity.ActivityType activityType;
        switch (request.getType().toUpperCase()) {
            case "HIKING":
                activityType = Activity.ActivityType.HIKING;
                break;
            case "PHOTOGRAPHY":
                activityType = Activity.ActivityType.PHOTOGRAPHY;
                break;
            case "CAMPING":
                activityType = Activity.ActivityType.CAMPING;
                break;
            case "CULTURAL":
                activityType = Activity.ActivityType.CULTURAL;
                break;
            case "ADVENTURE":
                activityType = Activity.ActivityType.ADVENTURE;
                break;
            case "RELAXATION":
                activityType = Activity.ActivityType.RELAXATION;
                break;
            case "OTHER":
                activityType = Activity.ActivityType.OTHER;
                break;
            default:
                throw new IllegalArgumentException("不支持的活动类型: " + request.getType());
        }
        activity.setType(activityType);
        
        // 转换难度等级
        Activity.ActivityLevel activityLevel;
        switch (request.getLevel().toUpperCase()) {
            case "EASY":
                activityLevel = Activity.ActivityLevel.EASY;
                break;
            case "MEDIUM":
                activityLevel = Activity.ActivityLevel.MEDIUM;
                break;
            case "HARD":
                activityLevel = Activity.ActivityLevel.HARD;
                break;
            case "EXPERT":
                activityLevel = Activity.ActivityLevel.EXPERT;
                break;
            default:
                throw new IllegalArgumentException("不支持的难度等级: " + request.getLevel());
        }
        activity.setLevel(activityLevel);
        
        activity.setRequirements(request.getRequirements());
        activity.setContactInfo(request.getContactInfo());
        
        // 如果活动已审核，编辑后需要重新审核
        if (activity.getStatus() == Activity.ActivityStatus.APPROVED) {
            activity.setStatus(Activity.ActivityStatus.PENDING);
            log.info("活动编辑后状态重置为待审核，活动ID: {}", id);
        }
        
        Activity updatedActivity = activityRepository.save(activity);
        
        log.info("活动编辑成功，活动ID: {}, 编辑者: {}", id, currentUser.getUsername());
        
        return convertToActivityResponse(updatedActivity);
    }
    
    @Override
    public void uploadImages(Long activityId, List<MultipartFile> files) {
        // 查找活动
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查权限 - 只有活动组织者可以上传图片
        if (!activity.getOrganizer().getId().equals(currentUser.getId())) {
            throw new BusinessException("只有活动组织者可以上传图片");
        }
        
        if (files == null || files.isEmpty()) {
            throw new BusinessException("没有上传文件");
        }
        
        // 处理每个上传的文件
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String imageUrl = FileStorageUtil.saveFile(file, "activities");
                    activity.setCoverImage(imageUrl);
                    activityRepository.save(activity);

                    log.info("活动图片上传成功，活动ID: {}, 文件路径: {}", activityId, imageUrl);
                } catch (Exception e) {
                    log.error("上传活动图片失败，活动ID: {}", activityId, e);
                    throw new BusinessException("图片上传失败: " + e.getMessage());
                }
            }
        }
    }
    
    @Override
    public Page<ActivityResponse> getActivities(Pageable pageable) {
        Page<Activity> activities = activityRepository.findByStatusOrderByCreatedAtDesc(Activity.ActivityStatus.APPROVED, pageable);
        return activities.map(this::convertToActivityResponse);
    }
    
    @Override
    public Page<ActivityResponse> getActivities(String keyword, String type, String status, String sort,
                                             String startDate, Double minCost, Double maxCost, Pageable pageable) {
        Activity.ActivityStatus statusFilter = Activity.ActivityStatus.APPROVED;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = Activity.ActivityStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的活动状态: {}", status);
            }
        }

        Activity.ActivityType typeFilter = null;
        if (type != null && !type.isBlank()) {
            try {
                typeFilter = Activity.ActivityType.valueOf(type.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的活动类型: {}", type);
            }
        }

        LocalDateTime startAfter = parseActivityStartDate(startDate);
        Sort sortResolved = resolveActivitySort(sort, pageable);
        Pageable pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortResolved);

        final Activity.ActivityStatus st = statusFilter;
        final Activity.ActivityType tp = typeFilter;
        final LocalDateTime startA = startAfter;
        final Double min = minCost;
        final Double max = maxCost;
        final String kw = keyword != null && !keyword.isBlank() ? keyword.trim() : null;

        Specification<Activity> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("status"), st));
            if (tp != null) {
                ps.add(cb.equal(root.get("type"), tp));
            }
            if (startA != null) {
                ps.add(cb.greaterThanOrEqualTo(root.get("startTime"), startA));
            }
            if (kw != null) {
                String pattern = "%" + kw.toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("destination")), pattern)));
            }
            Predicate costPred = buildActivityCostPredicate(root, cb, min, max);
            if (costPred != null) {
                ps.add(costPred);
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };

        Page<Activity> activities = activityRepository.findAll(spec, pageRequest);
        log.info("查询活动列表: keyword={}, type={}, status={}, sort={}, startDate={}, minCost={}, maxCost={}, 数量={}",
                keyword, type, status, sort, startDate, minCost, maxCost, activities.getTotalElements());
        return activities.map(this::convertToActivityResponse);
    }

    private static Sort resolveActivitySort(String sortParam, Pageable pageable) {
        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",");
            if (parts.length >= 2) {
                String prop = parts[0].trim();
                Sort.Direction dir = Sort.Direction.fromString(parts[1].trim());
                return Sort.by(dir, prop);
            }
        }
        if (pageable.getSort().isSorted()) {
            return pageable.getSort();
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    private static LocalDateTime parseActivityStartDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private static Predicate buildActivityCostPredicate(Root<Activity> root, CriteriaBuilder cb,
                                                      Double minCost, Double maxCost) {
        if (minCost == null && maxCost == null) {
            return null;
        }
        if (maxCost != null && maxCost == 0.0 && minCost == null) {
            return cb.or(
                    cb.isNull(root.get("cost")),
                    cb.le(root.get("cost"), BigDecimal.ZERO));
        }
        List<Predicate> sub = new ArrayList<>();
        if (minCost != null) {
            sub.add(cb.ge(root.get("cost"), BigDecimal.valueOf(minCost)));
        }
        if (maxCost != null) {
            if (maxCost == 0.0) {
                sub.add(cb.or(
                        cb.isNull(root.get("cost")),
                        cb.le(root.get("cost"), BigDecimal.ZERO)));
            } else {
                sub.add(cb.le(root.get("cost"), BigDecimal.valueOf(maxCost)));
            }
        }
        if (sub.isEmpty()) {
            return null;
        }
        if (sub.size() == 1) {
            return sub.get(0);
        }
        return cb.and(sub.toArray(new Predicate[0]));
    }
    
    @Override
    public ActivityResponse getActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        // 增加浏览量
        activity.setViewCount(activity.getViewCount() + 1);
        activityRepository.save(activity);
        
        return convertToActivityResponse(activity);
    }
    
    @Override
    public void joinActivity(Long id, String note) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查活动状态
        if (activity.getStatus() != Activity.ActivityStatus.APPROVED) {
            throw new BusinessException("活动未审核通过，无法报名");
        }
        
        // 检查是否已满员
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException("活动已满员");
        }
        
        // 报名截止：活动开始后不可报；若设置了报名截止时间且早于开始时间，则以二者中较晚的时刻前均可报（避免仅因历史截止时间误伤）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = activity.getStartTime();
        if (start != null && !now.isBefore(start)) {
            throw new BusinessException("活动已开始，无法报名");
        }
        LocalDateTime regEnd = activity.getRegistrationDeadline();
        if (regEnd != null) {
            LocalDateTime lastMoment = regEnd;
            if (start != null && start.isAfter(regEnd)) {
                lastMoment = start; // 截止填错早于开始时，放宽到开始前仍可报
            }
            if (now.isAfter(lastMoment)) {
                throw new BusinessException("报名已截止");
            }
        }
        
        // 检查是否已经报名
        Optional<ActivityParticipant> existingParticipant = activityParticipantRepository.findByActivityAndUser(activity, currentUser);
        if (existingParticipant.isPresent()) {
            ActivityParticipant.ParticipantStatus status = existingParticipant.get().getStatus();
            if (status == ActivityParticipant.ParticipantStatus.PENDING) {
                throw new BusinessException("您已经报名此活动，等待组织者审核");
            } else if (status == ActivityParticipant.ParticipantStatus.APPROVED) {
                throw new BusinessException("您已经成功报名此活动");
            } else if (status == ActivityParticipant.ParticipantStatus.REJECTED) {
                throw new BusinessException("您的报名申请已被拒绝");
            }
        }
        
        // 创建报名记录
        ActivityParticipant participant = new ActivityParticipant();
        participant.setActivity(activity);
        participant.setUser(currentUser);
        participant.setStatus(ActivityParticipant.ParticipantStatus.PENDING);
        participant.setApplicationNote(note);
        participant.setAppliedAt(LocalDateTime.now());
        
        activityParticipantRepository.save(participant);

        // 通知组织者有新的报名
        notificationService.createNotification(
                activity.getOrganizer().getId(),
                "新的活动报名",
                currentUser.getNickname() + " 申请报名你的活动: " + activity.getTitle(),
                com.huashidai.lvyouapp.entity.Notification.NotificationType.ACTIVITY,
                "/activities/" + id
        );
        
        log.info("用户报名活动，活动ID: {}, 用户ID: {}, 备注: {}", id, currentUser.getId(), note);
    }
    
    @Override
    public void cancelJoinActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 查找报名记录
        ActivityParticipant participant = activityParticipantRepository.findByActivityAndUser(activity, currentUser)
                .orElseThrow(() -> new BusinessException("您未报名此活动"));
        
        // 检查状态
        if (participant.getStatus() == ActivityParticipant.ParticipantStatus.CANCELLED) {
            throw new BusinessException("您已经取消报名");
        }
        
        // 若之前为已通过，需减少活动当前人数
        if (participant.getStatus() == ActivityParticipant.ParticipantStatus.APPROVED) {
            activity.setCurrentParticipants(activity.getCurrentParticipants() - 1);
            activityRepository.save(activity);
        }
        participant.setStatus(ActivityParticipant.ParticipantStatus.CANCELLED);
        activityParticipantRepository.save(participant);
        
        log.info("用户取消报名活动，活动ID: {}, 用户ID: {}", id, currentUser.getId());
    }
    
    @Override
    public void approveParticipant(Long activityId, Long participantId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查权限 - 只有活动组织者可以审核
        if (!activity.getOrganizer().getId().equals(currentUser.getId())) {
            throw new BusinessException("只有活动组织者可以审核报名");
        }
        
        ActivityParticipant participant = activityParticipantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException("报名记录不存在"));
        
        // 检查报名记录是否属于该活动
        if (!participant.getActivity().getId().equals(activityId)) {
            throw new BusinessException("报名记录不属于该活动");
        }
        
        // 检查状态
        if (participant.getStatus() != ActivityParticipant.ParticipantStatus.PENDING) {
            throw new BusinessException("该报名记录不是待审核状态");
        }
        
        // 检查是否已满员
        if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            throw new BusinessException("活动已满员，无法通过更多报名");
        }
        
        // 更新状态
        participant.setStatus(ActivityParticipant.ParticipantStatus.APPROVED);
        participant.setApprovedAt(LocalDateTime.now());
        activityParticipantRepository.save(participant);
        
        // 更新活动的当前参与者数量
        activity.setCurrentParticipants(activity.getCurrentParticipants() + 1);
        activityRepository.save(activity);
        
        log.info("审核通过参与者，活动ID: {}, 参与者ID: {}", activityId, participantId);

        // 通知报名者审核通过
        notificationService.createNotification(
                participant.getUser().getId(),
                "活动报名已通过",
                "你报名的活动《" + activity.getTitle() + "》已通过审核，记得准时参加哦！",
                com.huashidai.lvyouapp.entity.Notification.NotificationType.ACTIVITY,
                "/activities/" + activityId
        );
    }
    
    @Override
    public void rejectParticipant(Long activityId, Long participantId, String reason) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查权限 - 只有活动组织者可以审核
        if (!activity.getOrganizer().getId().equals(currentUser.getId())) {
            throw new BusinessException("只有活动组织者可以审核报名");
        }
        
        ActivityParticipant participant = activityParticipantRepository.findById(participantId)
                .orElseThrow(() -> new BusinessException("报名记录不存在"));
        
        // 检查报名记录是否属于该活动
        if (!participant.getActivity().getId().equals(activityId)) {
            throw new BusinessException("报名记录不属于该活动");
        }
        
        // 检查状态
        if (participant.getStatus() != ActivityParticipant.ParticipantStatus.PENDING) {
            throw new BusinessException("该报名记录不是待审核状态");
        }
        
        // 更新状态
        participant.setStatus(ActivityParticipant.ParticipantStatus.REJECTED);
        participant.setRejectionReason(reason);
        activityParticipantRepository.save(participant);
        
        log.info("拒绝参与者，活动ID: {}, 参与者ID: {}, 原因: {}", activityId, participantId, reason);

        // 通知报名者审核被拒
        notificationService.createNotification(
                participant.getUser().getId(),
                "活动报名未通过",
                "你报名的活动《" + activity.getTitle() + "》未通过审核，原因：" + reason,
                com.huashidai.lvyouapp.entity.Notification.NotificationType.ACTIVITY,
                "/activities/" + activityId
        );
    }
    
    @Override
    public void deleteActivity(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        activity.setStatus(Activity.ActivityStatus.CANCELLED);
        activityRepository.save(activity);
    }
    
    @Override
    public Page<ActivityResponse> getMyActivities(String status, Pageable pageable) {
        // 获取当前用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        Page<Activity> activities;
        
        if (status != null && !status.isEmpty()) {
            // 根据状态筛选
            Activity.ActivityStatus activityStatus = Activity.ActivityStatus.valueOf(status);
            activities = activityRepository.findByOrganizerAndStatusOrderByCreatedAtDesc(currentUser, activityStatus, pageable);
        } else {
            // 获取所有活动
            activities = activityRepository.findByOrganizerOrderByCreatedAtDesc(currentUser, pageable);
        }
        
        return activities.map(this::convertToActivityResponse);
    }
    
    private ActivityResponse convertToActivityResponse(Activity activity) {
        ActivityResponse response = new ActivityResponse();
        response.setId(activity.getId());
        response.setTitle(activity.getTitle());
        response.setDescription(activity.getDescription());
        response.setDestination(activity.getDestination());
        response.setStartTime(activity.getStartTime());
        response.setEndTime(activity.getEndTime());
        response.setRegistrationDeadline(activity.getRegistrationDeadline());
        response.setMaxParticipants(activity.getMaxParticipants());
        response.setCurrentParticipants(activity.getCurrentParticipants());
        response.setPendingParticipantCount((int) activityParticipantRepository.countByActivityAndStatus(activity, ActivityParticipant.ParticipantStatus.PENDING));
        response.setCost(activity.getCost());
        response.setType(activity.getType());
        response.setStatus(activity.getStatus());
        response.setLevel(activity.getLevel());
        response.setRequirements(activity.getRequirements());
        response.setContactInfo(activity.getContactInfo());
        response.setCoverImage(activity.getCoverImage());
        response.setViewCount(activity.getViewCount());
        response.setLikeCount(activity.getLikeCount());
        response.setCreatedAt(activity.getCreatedAt());
        response.setUpdatedAt(activity.getUpdatedAt());
        // 检查当前用户是否已报名
        User currentUser = SecurityUtils.getCurrentUser();
        boolean isJoined = false;
        if (currentUser != null) {
            Optional<ActivityParticipant> participant = activityParticipantRepository.findByActivityAndUser(activity, currentUser);
            isJoined = participant.isPresent() && 
                      (participant.get().getStatus() == ActivityParticipant.ParticipantStatus.APPROVED ||
                       participant.get().getStatus() == ActivityParticipant.ParticipantStatus.PENDING);
        }
        response.setIsJoined(isJoined);
        response.setIsLiked(false); // 简化处理
        
        // 设置组织者信息
        if (activity.getOrganizer() != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(activity.getOrganizer().getId());
            userResponse.setUsername(activity.getOrganizer().getUsername());
            userResponse.setNickname(activity.getOrganizer().getNickname());
            userResponse.setAvatar(activity.getOrganizer().getAvatar());
            response.setOrganizer(userResponse);
        }
        
        return response;
    }
    
    @Override
    public Page<ParticipantResponse> getActivityParticipants(Long activityId, String status, Pageable pageable) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        // 检查权限 - 只有活动组织者可以查看参与者
        if (!activity.getOrganizer().getId().equals(currentUser.getId())) {
            throw new BusinessException("只有活动组织者可以查看参与者");
        }
        
        Page<ActivityParticipant> participants;
        
        if (status != null && !status.isEmpty()) {
            ActivityParticipant.ParticipantStatus participantStatus = ActivityParticipant.ParticipantStatus.valueOf(status);
            participants = activityParticipantRepository.findByActivityAndStatusOrderByAppliedAtDesc(activity, participantStatus, pageable);
        } else {
            participants = activityParticipantRepository.findByActivityOrderByAppliedAtDesc(activity, pageable);
        }
        
        return participants.map(this::convertToParticipantResponse);
    }
    
    @Override
    public Page<ParticipantResponse> getMyParticipations(String status, Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        Page<ActivityParticipant> participants;
        
        if (status != null && !status.isEmpty()) {
            ActivityParticipant.ParticipantStatus participantStatus = ActivityParticipant.ParticipantStatus.valueOf(status);
            participants = activityParticipantRepository.findByUserAndStatusOrderByAppliedAtDesc(currentUser, participantStatus, pageable);
        } else {
            participants = activityParticipantRepository.findByUserOrderByAppliedAtDesc(currentUser, pageable);
        }
        
        return participants.map(this::convertToParticipantResponse);
    }
    
    @Override
    public java.util.Map<String, Object> checkUserParticipation(Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException("活动不存在"));
        
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        
        java.util.Map<String, Object> result = new HashMap<>();
        
        // 检查是否是组织者
        boolean isOrganizer = activity.getOrganizer().getId().equals(currentUser.getId());
        result.put("isOrganizer", isOrganizer);
        
        // 检查是否已参与活动
        Optional<ActivityParticipant> participant = activityParticipantRepository.findByActivityAndUser(activity, currentUser);
        boolean isParticipant = participant.isPresent();
        result.put("isParticipant", isParticipant);
        
        if (isParticipant) {
            ActivityParticipant.ParticipantStatus status = participant.get().getStatus();
            result.put("participationStatus", status.name());
            result.put("canAccessChat", status == ActivityParticipant.ParticipantStatus.APPROVED || isOrganizer);
        } else {
            result.put("participationStatus", null);
            result.put("canAccessChat", isOrganizer);
        }
        
        return result;
    }
    
    private ParticipantResponse convertToParticipantResponse(ActivityParticipant participant) {
        ParticipantResponse response = new ParticipantResponse();
        response.setId(participant.getId());
        response.setStatus(participant.getStatus().name());
        response.setApplicationNote(participant.getApplicationNote());
        response.setRejectionReason(participant.getRejectionReason());
        response.setAppliedAt(participant.getAppliedAt());
        response.setApprovedAt(participant.getApprovedAt());
        
        // 设置用户信息
        if (participant.getUser() != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(participant.getUser().getId());
            userResponse.setUsername(participant.getUser().getUsername());
            userResponse.setNickname(participant.getUser().getNickname());
            userResponse.setAvatar(participant.getUser().getAvatar());
            response.setUser(userResponse);
        }
        
        return response;
    }
}
