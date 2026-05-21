package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.*;
import com.huashidai.lvyouapp.entity.*;
import com.huashidai.lvyouapp.repository.*;
import com.huashidai.lvyouapp.service.AdminService;
import com.huashidai.lvyouapp.util.SecurityUtils;
import com.huashidai.lvyouapp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理员服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {
    
    private final PostRepository postRepository;
    private final ActivityRepository activityRepository;
    private final ExpertApplicationRepository expertApplicationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    
    @Override
    public Page<PostResponse> getPendingPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(Post.PostStatus.PENDING, pageable);
        return posts.map(this::convertToPostResponse);
    }
    
    @Override
    public Page<PostResponse> getPostsByStatus(String status, Pageable pageable) {
        Post.PostStatus postStatus = Post.PostStatus.valueOf(status);
        Page<Post> posts = postRepository.findByStatusOrderByCreatedAtDesc(postStatus, pageable);
        return posts.map(this::convertToPostResponse);
    }
    
    @Override
    @Transactional
    public void approvePost(Long postId, ApprovalRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        post.setStatus(Post.PostStatus.APPROVED);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
        
        log.info("管理员审核通过动态，ID: {}, 备注: {}", postId, request.getNote());

        // 通知作者
        notificationService.createNotification(
                post.getUser().getId(),
                "动态审核通过",
                "你的动态已通过审核", com.huashidai.lvyouapp.entity.Notification.NotificationType.SYSTEM,
                "/posts/" + postId);
    }
    
    @Override
    @Transactional
    public void rejectPost(Long postId, RejectionRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("动态不存在"));
        
        post.setStatus(Post.PostStatus.REJECTED);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
        
        log.info("管理员拒绝动态，ID: {}, 原因: {}", postId, request.getReason());

        notificationService.createNotification(
                post.getUser().getId(),
                "动态审核未通过",
                "你的动态未通过审核，原因：" + request.getReason(),
                com.huashidai.lvyouapp.entity.Notification.NotificationType.SYSTEM,
                "/posts/" + postId);
    }
    
    @Override
    public Page<ActivityResponse> getPendingActivities(Pageable pageable) {
        Page<Activity> activities = activityRepository.findByStatusOrderByCreatedAtDesc(Activity.ActivityStatus.PENDING, pageable);
        return activities.map(this::convertToActivityResponse);
    }
    
    @Override
    @Transactional
    public void approveActivity(Long activityId, ApprovalRequest request) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        activity.setStatus(Activity.ActivityStatus.APPROVED);
        activity.setUpdatedAt(LocalDateTime.now());
        activityRepository.save(activity);
        
        log.info("管理员审核通过活动，ID: {}, 备注: {}", activityId, request.getNote());

        notificationService.createNotification(
                activity.getOrganizer().getId(),
                "活动审核通过",
                "你的活动《" + activity.getTitle() + "》已通过审核", com.huashidai.lvyouapp.entity.Notification.NotificationType.ACTIVITY,
                "/activities/" + activityId);
    }
    
    @Override
    @Transactional
    public void rejectActivity(Long activityId, RejectionRequest request) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new RuntimeException("活动不存在"));
        
        activity.setStatus(Activity.ActivityStatus.REJECTED);
        activity.setUpdatedAt(LocalDateTime.now());
        activityRepository.save(activity);
        
        log.info("管理员拒绝活动，ID: {}, 原因: {}", activityId, request.getReason());

        notificationService.createNotification(
                activity.getOrganizer().getId(),
                "活动审核未通过",
                "你的活动《" + activity.getTitle() + "》未通过审核，原因：" + request.getReason(),
                com.huashidai.lvyouapp.entity.Notification.NotificationType.ACTIVITY,
                "/activities/" + activityId);
    }
    
    @Override
    public Page<ExpertApplicationResponse> getExpertApplications(Pageable pageable) {
        Page<ExpertApplication> applications = expertApplicationRepository.findAllByOrderByCreatedAtDesc(pageable);
        return applications.map(this::convertToExpertApplicationResponse);
    }
    
    @Override
    @Transactional
    public void approveExpertApplication(Long applicationId, ApprovalRequest request) {
        ExpertApplication application = expertApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        // 更新申请状态
        application.setStatus(ExpertApplication.ApplicationStatus.APPROVED);
        application.setAdminNote(request.getNote());
        application.setReviewedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        
        // 更新用户角色为达人
        User user = application.getUser();
        user.setRole(User.UserRole.EXPERT);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        expertApplicationRepository.save(application);
        
        log.info("管理员审核通过达人申请，申请ID: {}, 用户ID: {}", applicationId, user.getId());

        notificationService.createNotification(
                user.getId(),
                "达人申请通过",
                "恭喜，您的达人申请已通过！", com.huashidai.lvyouapp.entity.Notification.NotificationType.SYSTEM,
                "/profile");
    }
    
    @Override
    @Transactional
    public void rejectExpertApplication(Long applicationId, RejectionRequest request) {
        ExpertApplication application = expertApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        application.setStatus(ExpertApplication.ApplicationStatus.REJECTED);
        application.setRejectReason(request.getReason());
        application.setReviewedAt(LocalDateTime.now());
        application.setUpdatedAt(LocalDateTime.now());
        expertApplicationRepository.save(application);
        
        log.info("管理员拒绝达人申请，申请ID: {}, 原因: {}", applicationId, request.getReason());

        notificationService.createNotification(
                application.getUser().getId(),
                "达人申请未通过",
                "很遗憾，您的达人申请未通过，原因：" + request.getReason(), com.huashidai.lvyouapp.entity.Notification.NotificationType.SYSTEM,
                "/profile");
    }
    
    @Override
    public AdminStatisticsResponse getStatistics() {
        AdminStatisticsResponse statistics = new AdminStatisticsResponse();
        
        // 统计用户数量
        statistics.setTotalUsers(userRepository.count());
        
        // 统计动态数量
        statistics.setTotalPosts(postRepository.count());
        statistics.setPendingPosts(postRepository.countByStatus(Post.PostStatus.PENDING));
        
        // 统计活动数量
        statistics.setTotalActivities(activityRepository.count());
        statistics.setPendingActivities(activityRepository.countByStatus(Activity.ActivityStatus.PENDING));
        
        // 统计攻略数量
        statistics.setTotalStrategies(0L); // 暂时设为0，后续完善
        
        // 统计达人申请数量
        statistics.setPendingExpertApplications(expertApplicationRepository.countPendingApplications());
        
        // 今日新增数据（简化处理）
        statistics.setTodayNewUsers(0L);
        statistics.setTodayNewPosts(0L);
        statistics.setTodayNewActivities(0L);
        
        return statistics;
    }
    
    @Override
    public Page<UserResponse> getUsers(String role, String keyword, Pageable pageable) {
        Page<User> users;
        
        if (role != null && !role.isEmpty()) {
            try {
                User.UserRole userRole = User.UserRole.valueOf(role);
                if (keyword != null && !keyword.isEmpty()) {
                    users = userRepository.findByRoleAndKeyword(userRole, keyword, pageable);
                } else {
                    users = userRepository.findByRoleOrderByCreatedAtDesc(userRole, pageable);
                }
            } catch (IllegalArgumentException e) {
                log.warn("无效的用户角色: {}", role);
                users = userRepository.findAllByOrderByCreatedAtDesc(pageable);
            }
        } else if (keyword != null && !keyword.isEmpty()) {
            users = userRepository.findByKeyword(keyword, pageable);
        } else {
            users = userRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        return users.map(this::convertToUserResponse);
    }
    
    @Override
    @Transactional
    public void updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        try {
            User.UserStatus userStatus = User.UserStatus.valueOf(status);
            user.setStatus(userStatus);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("管理员更新用户状态，用户ID: {}, 新状态: {}", userId, status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的用户状态: " + status);
        }
    }
    
    @Override
    @Transactional
    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        try {
            User.UserRole userRole = User.UserRole.valueOf(role);
            user.setRole(userRole);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("管理员更新用户角色，用户ID: {}, 新角色: {}", userId, role);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("无效的用户角色: " + role);
        }
    }
    
    private PostResponse convertToPostResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setContent(post.getContent());
        response.setLocation(post.getLocation());
        response.setType(post.getType());
        response.setStatus(post.getStatus());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setShareCount(post.getShareCount());
        response.setViewCount(post.getViewCount());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        
        // 设置用户信息
        if (post.getUser() != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(post.getUser().getId());
            userResponse.setUsername(post.getUser().getUsername());
            userResponse.setNickname(post.getUser().getNickname());
            userResponse.setAvatar(post.getUser().getAvatar());
            response.setUser(userResponse);
        }
        
        return response;
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
        
        // 设置组织者信息
        if (activity.getOrganizer() != null) {
            UserResponse organizerResponse = new UserResponse();
            organizerResponse.setId(activity.getOrganizer().getId());
            organizerResponse.setUsername(activity.getOrganizer().getUsername());
            organizerResponse.setNickname(activity.getOrganizer().getNickname());
            organizerResponse.setAvatar(activity.getOrganizer().getAvatar());
            response.setOrganizer(organizerResponse);
        }
        
        return response;
    }
    
    private ExpertApplicationResponse convertToExpertApplicationResponse(ExpertApplication application) {
        ExpertApplicationResponse response = new ExpertApplicationResponse();
        response.setId(application.getId());
        response.setUserId(application.getUser().getId());
        response.setUsername(application.getUser().getUsername());
        response.setNickname(application.getUser().getNickname());
        response.setReason(application.getReason());
        response.setBio(application.getBio());
        response.setExpertise(application.getExpertise());
        response.setContactInfo(application.getContactInfo());
        response.setStatus(application.getStatus().name());
        response.setAdminNote(application.getAdminNote());
        response.setRejectReason(application.getRejectReason());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        response.setReviewedAt(application.getReviewedAt());
        
        return response;
    }
    
    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setGender(user.getGender());
        response.setAge(user.getAge());
        response.setLocation(user.getLocation());
        response.setBio(user.getBio());
        response.setBirthday(user.getBirthday());
        response.setInterests(user.getInterests());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        response.setPoints(user.getPoints());
        response.setFollowersCount(user.getFollowersCount());
        response.setFollowingCount(user.getFollowingCount());
        response.setIsVerified(user.getIsVerified());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        
        return response;
    }
}
