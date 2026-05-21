package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Post;
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
 * 动态数据访问层
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {
    
    Page<Post> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Post.PostStatus status, Pageable pageable);

    Page<Post> findByUserIdAndStatus(Long userId, Post.PostStatus status, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id = :userId AND p.status <> :excludedStatus")
    Page<Post> findByUserIdAndStatusNot(@Param("userId") Long userId,
                                        @Param("excludedStatus") Post.PostStatus excludedStatus,
                                        Pageable pageable);
    
    Page<Post> findByStatusOrderByCreatedAtDesc(Post.PostStatus status, Pageable pageable);
    
    Page<Post> findByTypeAndStatusOrderByCreatedAtDesc(Post.PostType type, Post.PostStatus status, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.status = :status AND (p.content LIKE %:keyword% OR p.location LIKE %:keyword%)")
    Page<Post> findByStatusAndKeyword(@Param("status") Post.PostStatus status, @Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.status = :status AND p.createdAt >= :startTime AND p.createdAt <= :endTime")
    Page<Post> findByStatusAndTimeRange(@Param("status") Post.PostStatus status, 
                                       @Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime, 
                                       Pageable pageable);
    
    /** 热门：仅按点赞数（空视为 0），再按发布时间 */
    @Query("SELECT p FROM Post p WHERE p.status = :status ORDER BY COALESCE(p.likeCount, 0) DESC, p.createdAt DESC")
    Page<Post> findPopularPosts(@Param("status") Post.PostStatus status, Pageable pageable);
    
    List<Post> findByUserIdAndTypeAndStatus(Long userId, Post.PostType type, Post.PostStatus status);
    
    long countByUserIdAndStatus(Long userId, Post.PostStatus status);
    
    long countByStatus(Post.PostStatus status);
}
