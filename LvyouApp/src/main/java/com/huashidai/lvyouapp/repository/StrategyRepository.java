package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Strategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 攻略数据访问层
 */
@Repository
public interface StrategyRepository extends JpaRepository<Strategy, Long> {
    
    Page<Strategy> findByAuthorIdAndStatusOrderByCreatedAtDesc(Long authorId, Strategy.StrategyStatus status, Pageable pageable);
    
    Page<Strategy> findByStatusOrderByCreatedAtDesc(Strategy.StrategyStatus status, Pageable pageable);
    
    @Query("SELECT s FROM Strategy s WHERE s.status = :status AND (s.title LIKE %:keyword% OR s.content LIKE %:keyword% OR s.destination LIKE %:keyword%)")
    Page<Strategy> findByStatusAndKeyword(@Param("status") Strategy.StrategyStatus status, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT s FROM Strategy s WHERE s.status = :status AND s.destination = :destination")
    Page<Strategy> findByStatusAndDestination(@Param("status") Strategy.StrategyStatus status, @Param("destination") String destination, Pageable pageable);
    
    @Query("SELECT s FROM Strategy s WHERE s.status = :status AND s.type = :type")
    Page<Strategy> findByStatusAndType(@Param("status") Strategy.StrategyStatus status, @Param("type") Strategy.StrategyType type, Pageable pageable);
    
    @Query("SELECT s FROM Strategy s WHERE s.status = :status ORDER BY s.likeCount DESC, s.createdAt DESC")
    Page<Strategy> findPopularStrategies(@Param("status") Strategy.StrategyStatus status, Pageable pageable);
    
    @Query("SELECT s FROM Strategy s WHERE s.status = :status ORDER BY s.viewCount DESC, s.createdAt DESC")
    Page<Strategy> findMostViewedStrategies(@Param("status") Strategy.StrategyStatus status, Pageable pageable);
    
    List<Strategy> findByAuthorIdAndStatus(Long authorId, Strategy.StrategyStatus status);
    
    long countByAuthorIdAndStatus(Long authorId, Strategy.StrategyStatus status);
}
