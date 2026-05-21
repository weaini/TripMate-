package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.ExpertApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 达人申请Repository
 */
@Repository
public interface ExpertApplicationRepository extends JpaRepository<ExpertApplication, Long> {
    
    /**
     * 根据用户ID查找申请
     */
    Optional<ExpertApplication> findByUserId(Long userId);
    
    /**
     * 根据状态查找申请列表
     */
    Page<ExpertApplication> findByStatusOrderByCreatedAtDesc(ExpertApplication.ApplicationStatus status, Pageable pageable);
    
    /**
     * 查找所有申请（分页）
     */
    Page<ExpertApplication> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    /**
     * 统计待审核申请数量
     */
    @Query("SELECT COUNT(e) FROM ExpertApplication e WHERE e.status = 'PENDING'")
    Long countPendingApplications();
}
