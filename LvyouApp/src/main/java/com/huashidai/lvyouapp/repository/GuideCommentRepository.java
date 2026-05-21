package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Guide;
import com.huashidai.lvyouapp.entity.GuideComment;
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
 * 攻略评论数据访问层
 */
@Repository
public interface GuideCommentRepository extends JpaRepository<GuideComment, Long> {
    
    /**
     * 根据攻略查询评论（分页）
     */
    Page<GuideComment> findByGuideAndStatusOrderByCreatedAtDesc(Guide guide, GuideComment.CommentStatus status, Pageable pageable);
    
    /**
     * 根据攻略查询顶级评论（无父评论）
     */
    @Query("SELECT c FROM GuideComment c WHERE c.guide = :guide AND c.parent IS NULL AND c.status = :status ORDER BY c.createdAt DESC")
    Page<GuideComment> findTopLevelCommentsByGuide(@Param("guide") Guide guide, @Param("status") GuideComment.CommentStatus status, Pageable pageable);
    
    /**
     * 根据父评论查询回复
     */
    List<GuideComment> findByParentAndStatusOrderByCreatedAtAsc(GuideComment parent, GuideComment.CommentStatus status);
    
    /**
     * 根据用户查询评论
     */
    Page<GuideComment> findByUserAndStatusOrderByCreatedAtDesc(User user, GuideComment.CommentStatus status, Pageable pageable);
    
    /**
     * 统计攻略的评论数量
     */
    @Query("SELECT COUNT(c) FROM GuideComment c WHERE c.guide = :guide AND c.status = :status")
    Long countByGuideAndStatus(@Param("guide") Guide guide, @Param("status") GuideComment.CommentStatus status);
    
    /**
     * 统计用户的评论数量
     */
    @Query("SELECT COUNT(c) FROM GuideComment c WHERE c.user = :user AND c.status = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") GuideComment.CommentStatus status);
    
    /**
     * 检查用户是否已评论过该攻略
     */
    @Query("SELECT COUNT(c) > 0 FROM GuideComment c WHERE c.guide = :guide AND c.user = :user AND c.status = :status")
    boolean existsByGuideAndUserAndStatus(@Param("guide") Guide guide, @Param("user") User user, @Param("status") GuideComment.CommentStatus status);
}
