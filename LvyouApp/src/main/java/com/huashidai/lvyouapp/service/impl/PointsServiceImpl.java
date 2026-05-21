package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.entity.PointsRecord;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.exception.BusinessException;
import com.huashidai.lvyouapp.exception.ErrorCode;
import com.huashidai.lvyouapp.repository.PointsRecordRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.service.PointsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 积分服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PointsServiceImpl implements PointsService {
    
    private final PointsRecordRepository pointsRecordRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public void addPoints(User user, Integer points, String description, String relatedType, Long relatedId) {
        if (points <= 0) {
            throw new IllegalArgumentException("积分必须大于0");
        }
        
        // 重新查询用户信息，确保获取最新的积分数据
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        try {
            // 创建积分记录
            PointsRecord record = new PointsRecord();
            record.setUser(currentUser);
            record.setPoints(points);
            record.setType(PointsRecord.PointsType.EARN);
            record.setDescription(description);
            record.setRelatedType(relatedType);
            record.setRelatedId(relatedId);
            
            pointsRecordRepository.save(record);
            
            // 更新用户积分
            currentUser.setPoints(currentUser.getPoints() + points);
            userRepository.save(currentUser);
            
            log.info("用户 {} 获得 {} 积分，描述: {}，当前积分: {}", 
                    currentUser.getUsername(), points, description, currentUser.getPoints());
            
        } catch (Exception e) {
            log.error("增加用户 {} 积分失败: {}", currentUser.getUsername(), e.getMessage(), e);
            throw new RuntimeException("积分增加失败，请重试");
        }
    }
    
    @Override
    @Transactional
    public boolean deductPoints(User user, Integer points, String description, String relatedType, Long relatedId) {
        if (points <= 0) {
            throw new IllegalArgumentException("积分必须大于0");
        }
        
        // 重新查询用户信息，确保获取最新的积分数据
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查积分是否足够
        if (currentUser.getPoints() < points) {
            log.warn("用户 {} 积分不足，当前积分: {}，需要积分: {}", 
                    currentUser.getUsername(), currentUser.getPoints(), points);
            return false;
        }
        
        try {
            // 创建积分记录
            PointsRecord record = new PointsRecord();
            record.setUser(currentUser);
            record.setPoints(-points); // 负数表示扣除
            record.setType(PointsRecord.PointsType.SPEND);
            record.setDescription(description);
            record.setRelatedType(relatedType);
            record.setRelatedId(relatedId);
            
            pointsRecordRepository.save(record);
            
            // 更新用户积分
            currentUser.setPoints(currentUser.getPoints() - points);
            userRepository.save(currentUser);
            
            log.info("用户 {} 扣除 {} 积分，描述: {}，剩余积分: {}", 
                    currentUser.getUsername(), points, description, currentUser.getPoints());
            return true;
            
        } catch (Exception e) {
            log.error("扣除用户 {} 积分失败: {}", currentUser.getUsername(), e.getMessage(), e);
            throw new RuntimeException("积分扣除失败，请重试");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Integer getUserPoints(User user) {
        return user.getPoints();
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<PointsRecord> getUserPointsRecords(User user, Pageable pageable) {
        return pointsRecordRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean hasEnoughPoints(User user, Integer requiredPoints) {
        return user.getPoints() >= requiredPoints;
    }
    
    @Override
    public void earnPointsForPost(User user, Long postId) {
        addPoints(user, 10, "发布帖子", "POST", postId);
    }
    
    @Override
    public void earnPointsForGuide(User user, Long guideId) {
        addPoints(user, 10, "发布攻略", "GUIDE", guideId);
    }
    
    @Override
    public void earnPointsForActivity(User user, Long activityId) {
        addPoints(user, 10, "发布活动", "ACTIVITY", activityId);
    }
    
    @Override
    public boolean spendPointsForComment(User user, Long commentId) {
        return deductPoints(user, 1, "发表评论", "COMMENT", commentId);
    }
    
    @Override
    public boolean spendPointsForLike(User user, Long likeId) {
        return deductPoints(user, 1, "点赞", "LIKE", likeId);
    }
    
    /**
     * 检查用户积分余额并返回详细信息
     */
    @Override
    @Transactional(readOnly = true)
    public PointsService.PointsBalanceInfo checkPointsBalance(User user, Integer requiredPoints) {
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Integer currentPoints = currentUser.getPoints();
        boolean hasEnough = currentPoints >= requiredPoints;
        
        return new PointsService.PointsBalanceInfo(currentPoints, requiredPoints, hasEnough);
    }

    @Override
    public void assertEnoughPointsForComment(User user) {
        PointsBalanceInfo b = checkPointsBalance(user, 1);
        if (b.isHasEnough()) {
            return;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentPoints", b.getCurrentPoints());
        data.put("requiredPoints", b.getRequiredPoints());
        data.put("shortage", b.getShortage());
        String msg = String.format(
                "发表评论需消耗 %d 积分，您当前为 %d，还差 %d。可通过发布动态、攻略或活动获取积分。",
                b.getRequiredPoints(), b.getCurrentPoints(), b.getShortage());
        throw new BusinessException(ErrorCode.POINTS_INSUFFICIENT.getCode(), msg, data);
    }
}
