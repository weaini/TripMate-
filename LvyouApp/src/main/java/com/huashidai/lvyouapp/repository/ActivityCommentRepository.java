package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Activity;
import com.huashidai.lvyouapp.entity.ActivityComment;
import com.huashidai.lvyouapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 活动评论数据访问层
 */
@Repository
public interface ActivityCommentRepository extends JpaRepository<ActivityComment, Long> {
    
    /**
     * 根据活动ID查找评论（分页）
     */
    Page<ActivityComment> findByActivityAndStatusOrderByCreatedAtDesc(Activity activity, ActivityComment.CommentStatus status, Pageable pageable);
    
    /**
     * 根据活动ID查找顶级评论（分页）
     */
    Page<ActivityComment> findByActivityAndParentIsNullAndStatusOrderByCreatedAtDesc(Activity activity, ActivityComment.CommentStatus status, Pageable pageable);
    
    /**
     * 根据父评论ID查找回复
     */
    List<ActivityComment> findByParentAndStatusOrderByCreatedAtAsc(ActivityComment parent, ActivityComment.CommentStatus status);
    
    /**
     * 统计活动的评论数量
     */
    @Query("SELECT COUNT(c) FROM ActivityComment c WHERE c.activity = :activity AND c.status = :status")
    long countByActivityAndStatus(@Param("activity") Activity activity, @Param("status") ActivityComment.CommentStatus status);
    
    /**
     * 根据用户查找评论
     */
    Page<ActivityComment> findByUserAndStatusOrderByCreatedAtDesc(User user, ActivityComment.CommentStatus status, Pageable pageable);
}
