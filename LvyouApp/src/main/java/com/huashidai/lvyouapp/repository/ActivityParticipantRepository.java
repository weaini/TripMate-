package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Activity;
import com.huashidai.lvyouapp.entity.ActivityParticipant;
import com.huashidai.lvyouapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 活动参与者数据访问层
 */
@Repository
public interface ActivityParticipantRepository extends JpaRepository<ActivityParticipant, Long> {
    
    /**
     * 根据活动ID查找参与者
     */
    Page<ActivityParticipant> findByActivityOrderByAppliedAtDesc(Activity activity, Pageable pageable);
    
    /**
     * 根据活动ID和状态查找参与者
     */
    Page<ActivityParticipant> findByActivityAndStatusOrderByAppliedAtDesc(Activity activity, ActivityParticipant.ParticipantStatus status, Pageable pageable);
    
    /**
     * 根据用户ID查找参与的活动
     */
    Page<ActivityParticipant> findByUserOrderByAppliedAtDesc(User user, Pageable pageable);
    
    /**
     * 根据用户ID和状态查找参与的活动
     */
    Page<ActivityParticipant> findByUserAndStatusOrderByAppliedAtDesc(User user, ActivityParticipant.ParticipantStatus status, Pageable pageable);
    
    /**
     * 检查用户是否已报名某个活动
     */
    Optional<ActivityParticipant> findByActivityAndUser(Activity activity, User user);
    
    /**
     * 统计活动的参与者数量
     */
    @Query("SELECT COUNT(p) FROM ActivityParticipant p WHERE p.activity = :activity AND p.status = :status")
    long countByActivityAndStatus(@Param("activity") Activity activity, @Param("status") ActivityParticipant.ParticipantStatus status);
    
    /**
     * 查找用户参与的所有活动
     */
    @Query("SELECT p.activity FROM ActivityParticipant p WHERE p.user = :user AND p.status = :status")
    List<Activity> findActivitiesByUserAndStatus(@Param("user") User user, @Param("status") ActivityParticipant.ParticipantStatus status);
}
