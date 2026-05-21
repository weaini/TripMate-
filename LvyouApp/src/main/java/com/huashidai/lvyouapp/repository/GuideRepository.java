package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Guide;
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
 * 攻略数据访问层
 */
@Repository
public interface GuideRepository extends JpaRepository<Guide, Long> {
    
    /**
     * 根据状态分页查询攻略
     */
    Page<Guide> findByStatusOrderByCreatedAtDesc(Guide.GuideStatus status, Pageable pageable);
    
    /**
     * 根据类型和状态分页查询攻略
     */
    Page<Guide> findByTypeAndStatusOrderByCreatedAtDesc(Guide.GuideType type, Guide.GuideStatus status, Pageable pageable);
    
    /**
     * 根据作者分页查询攻略
     */
    Page<Guide> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
    
    /**
     * 根据作者和状态分页查询攻略
     */
    Page<Guide> findByAuthorAndStatusOrderByCreatedAtDesc(User author, Guide.GuideStatus status, Pageable pageable);
    
    /**
     * 根据作者和类型分页查询攻略
     */
    Page<Guide> findByAuthorAndTypeOrderByCreatedAtDesc(User author, Guide.GuideType type, Pageable pageable);
    
    /**
     * 根据作者、类型和状态分页查询攻略
     */
    Page<Guide> findByAuthorAndTypeAndStatusOrderByCreatedAtDesc(User author, Guide.GuideType type, Guide.GuideStatus status, Pageable pageable);
    
    /**
     * 搜索攻略（标题和内容）
     */
    @Query("SELECT g FROM Guide g WHERE g.status = :status AND (g.title LIKE %:keyword% OR g.content LIKE %:keyword% OR g.summary LIKE %:keyword%) ORDER BY g.createdAt DESC")
    Page<Guide> searchGuides(@Param("status") Guide.GuideStatus status, @Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 获取热门攻略（按浏览量排序）
     */
    @Query("SELECT g FROM Guide g WHERE g.status = :status ORDER BY g.viewCount DESC, g.createdAt DESC")
    Page<Guide> findPopularGuides(@Param("status") Guide.GuideStatus status, Pageable pageable);
    
    /**
     * 获取最新攻略
     */
    @Query("SELECT g FROM Guide g WHERE g.status = :status ORDER BY g.createdAt DESC")
    Page<Guide> findLatestGuides(@Param("status") Guide.GuideStatus status, Pageable pageable);
    
    /**
     * 获取最受欢迎的攻略（按点赞数排序）
     */
    @Query("SELECT g FROM Guide g WHERE g.status = :status ORDER BY g.likeCount DESC, g.createdAt DESC")
    Page<Guide> findMostLikedGuides(@Param("status") Guide.GuideStatus status, Pageable pageable);
    
    /**
     * 统计用户的攻略数量
     */
    @Query("SELECT COUNT(g) FROM Guide g WHERE g.author = :author AND g.status = :status")
    Long countByAuthorAndStatus(@Param("author") User author, @Param("status") Guide.GuideStatus status);
    
    /**
     * 统计各类型攻略数量
     */
    @Query("SELECT g.type, COUNT(g) FROM Guide g WHERE g.status = :status GROUP BY g.type")
    List<Object[]> countByTypeAndStatus(@Param("status") Guide.GuideStatus status);
    
    /**
     * 统计指定状态的攻略数量
     */
    Long countByStatus(Guide.GuideStatus status);
}
