package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Comment;
import com.huashidai.lvyouapp.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 动态评论数据访问层
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 根据动态查询顶级评论（分页）
     */
    @Query("SELECT c FROM Comment c WHERE c.post = :post AND c.parent IS NULL AND c.status = :status ORDER BY c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByPost(@Param("post") Post post, @Param("status") Comment.CommentStatus status, Pageable pageable);
    
    /**
     * 根据动态查询所有评论（分页）
     */
    Page<Comment> findByPostAndStatusOrderByCreatedAtDesc(Post post, Comment.CommentStatus status, Pageable pageable);
    
    /**
     * 根据父评论查询回复
     */
    List<Comment> findByParentAndStatusOrderByCreatedAtAsc(Comment parent, Comment.CommentStatus status);
    
    /**
     * 根据用户查询评论
     */
    Page<Comment> findByUserAndStatusOrderByCreatedAtDesc(com.huashidai.lvyouapp.entity.User user, Comment.CommentStatus status, Pageable pageable);
    
    /**
     * 统计动态的评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post AND c.status = :status")
    Long countByPostAndStatus(@Param("post") Post post, @Param("status") Comment.CommentStatus status);
    
    /**
     * 统计用户的评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.user = :user AND c.status = :status")
    Long countByUserAndStatus(@Param("user") com.huashidai.lvyouapp.entity.User user, @Param("status") Comment.CommentStatus status);
    
    /**
     * 统计动态的顶级评论数量
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post AND c.parent IS NULL AND c.status = :status")
    Long countTopLevelCommentsByPost(@Param("post") Post post, @Param("status") Comment.CommentStatus status);
}