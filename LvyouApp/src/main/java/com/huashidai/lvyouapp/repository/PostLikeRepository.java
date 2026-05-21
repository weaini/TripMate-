package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 动态点赞数据访问层
 */
@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    /**
     * 检查用户是否已点赞某个动态
     */
    boolean existsByPostIdAndUserId(Long postId, Long userId);
    
    /**
     * 查找用户对某个动态的点赞记录
     */
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    
    /**
     * 删除用户对某个动态的点赞记录
     */
    void deleteByPostIdAndUserId(Long postId, Long userId);
    
    /**
     * 统计某个动态的点赞数量
     */
    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);
}
