package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.PointsRecord;
import com.huashidai.lvyouapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 积分记录数据访问层
 */
@Repository
public interface PointsRecordRepository extends JpaRepository<PointsRecord, Long> {
    
    /**
     * 根据用户查找积分记录
     */
    Page<PointsRecord> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * 根据用户和类型查找积分记录
     */
    Page<PointsRecord> findByUserAndTypeOrderByCreatedAtDesc(User user, PointsRecord.PointsType type, Pageable pageable);
    
    /**
     * 统计用户总积分
     */
    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PointsRecord p WHERE p.user = :user")
    Integer sumPointsByUser(@Param("user") User user);
    
    /**
     * 统计用户指定类型的积分
     */
    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PointsRecord p WHERE p.user = :user AND p.type = :type")
    Integer sumPointsByUserAndType(@Param("user") User user, @Param("type") PointsRecord.PointsType type);
    
    /**
     * 查找用户最近的积分记录
     */
    List<PointsRecord> findTop10ByUserOrderByCreatedAtDesc(User user);
    
    /**
     * 统计指定时间范围内的积分变化
     */
    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PointsRecord p WHERE p.user = :user AND p.createdAt BETWEEN :startTime AND :endTime")
    Integer sumPointsByUserAndTimeRange(@Param("user") User user, 
                                       @Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime);
}
