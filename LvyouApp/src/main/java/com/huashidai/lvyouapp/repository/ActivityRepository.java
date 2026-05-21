package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动数据访问层
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {
    
    Page<Activity> findByOrganizerIdAndStatusOrderByCreatedAtDesc(Long organizerId, Activity.ActivityStatus status, Pageable pageable);
    
    Page<Activity> findByStatusOrderByCreatedAtDesc(Activity.ActivityStatus status, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.status = :status AND (a.title LIKE %:keyword% OR a.description LIKE %:keyword% OR a.destination LIKE %:keyword%)")
    Page<Activity> findByStatusAndKeyword(@Param("status") Activity.ActivityStatus status, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.status = :status AND a.destination = :destination")
    Page<Activity> findByStatusAndDestination(@Param("status") Activity.ActivityStatus status, @Param("destination") String destination, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.status = :status AND a.type = :type")
    Page<Activity> findByStatusAndType(@Param("status") Activity.ActivityStatus status, @Param("type") Activity.ActivityType type, Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.status = :status AND a.startTime >= :startTime AND a.startTime <= :endTime")
    Page<Activity> findByStatusAndTimeRange(@Param("status") Activity.ActivityStatus status, 
                                          @Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime, 
                                          Pageable pageable);
    
    @Query("SELECT a FROM Activity a WHERE a.status = :status ORDER BY a.likeCount DESC, a.createdAt DESC")
    Page<Activity> findPopularActivities(@Param("status") Activity.ActivityStatus status, Pageable pageable);
    
    List<Activity> findByStatusAndStartTimeAfter(Activity.ActivityStatus status, LocalDateTime startTime);
    
    long countByOrganizerIdAndStatus(Long organizerId, Activity.ActivityStatus status);
    
    long countByStatus(Activity.ActivityStatus status);
    
    // 获取我的活动相关方法
    Page<Activity> findByOrganizerOrderByCreatedAtDesc(com.huashidai.lvyouapp.entity.User organizer, Pageable pageable);
    
    Page<Activity> findByOrganizerAndStatusOrderByCreatedAtDesc(com.huashidai.lvyouapp.entity.User organizer, Activity.ActivityStatus status, Pageable pageable);
}
