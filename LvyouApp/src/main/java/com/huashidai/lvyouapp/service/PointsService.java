package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.entity.PointsRecord;
import com.huashidai.lvyouapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 积分服务接口
 */
public interface PointsService {
    
    /**
     * 增加积分
     */
    void addPoints(User user, Integer points, String description, String relatedType, Long relatedId);
    
    /**
     * 扣除积分
     */
    boolean deductPoints(User user, Integer points, String description, String relatedType, Long relatedId);
    
    /**
     * 获取用户当前积分
     */
    Integer getUserPoints(User user);
    
    /**
     * 获取用户积分记录
     */
    Page<PointsRecord> getUserPointsRecords(User user, Pageable pageable);
    
    /**
     * 检查用户积分是否足够
     */
    boolean hasEnoughPoints(User user, Integer requiredPoints);
    
    /**
     * 发布帖子获得积分
     */
    void earnPointsForPost(User user, Long postId);
    
    /**
     * 发布攻略获得积分
     */
    void earnPointsForGuide(User user, Long guideId);
    
    /**
     * 发布活动获得积分
     */
    void earnPointsForActivity(User user, Long activityId);
    
    /**
     * 评论扣除积分
     */
    boolean spendPointsForComment(User user, Long commentId);
    
    /**
     * 点赞扣除积分
     */
    boolean spendPointsForLike(User user, Long likeId);
    
    /**
     * 检查用户积分余额并返回详细信息
     */
    PointsBalanceInfo checkPointsBalance(User user, Integer requiredPoints);

    /**
     * 校验发表评论所需积分（1 分），不足时抛出 {@link com.huashidai.lvyouapp.exception.BusinessException}，code 为 POINTS_INSUFFICIENT
     */
    void assertEnoughPointsForComment(User user);
    
    /**
     * 积分余额信息类
     */
    class PointsBalanceInfo {
        private final Integer currentPoints;
        private final Integer requiredPoints;
        private final boolean hasEnough;
        
        public PointsBalanceInfo(Integer currentPoints, Integer requiredPoints, boolean hasEnough) {
            this.currentPoints = currentPoints;
            this.requiredPoints = requiredPoints;
            this.hasEnough = hasEnough;
        }
        
        public Integer getCurrentPoints() {
            return currentPoints;
        }
        
        public Integer getRequiredPoints() {
            return requiredPoints;
        }
        
        public boolean isHasEnough() {
            return hasEnough;
        }
        
        public Integer getShortage() {
            return Math.max(0, requiredPoints - currentPoints);
        }
        
        public String getMessage() {
            if (hasEnough) {
                return String.format("当前积分: %d，需要积分: %d", currentPoints, requiredPoints);
            } else {
                return String.format("积分不足！当前积分: %d，需要积分: %d，还差: %d", 
                        currentPoints, requiredPoints, getShortage());
            }
        }
    }
}
