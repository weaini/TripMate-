package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Guide;
import com.huashidai.lvyouapp.entity.GuideLike;
import com.huashidai.lvyouapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 攻略点赞数据访问层
 */
@Repository
public interface GuideLikeRepository extends JpaRepository<GuideLike, Long> {
    
    /**
     * 检查用户是否已点赞攻略
     */
    boolean existsByGuideAndUser(Guide guide, User user);
    
    /**
     * 根据攻略和用户查找点赞记录
     */
    Optional<GuideLike> findByGuideAndUser(Guide guide, User user);
    
    /**
     * 统计攻略的点赞数量
     */
    long countByGuide(Guide guide);
    
    /**
     * 删除用户对攻略的点赞
     */
    void deleteByGuideAndUser(Guide guide, User user);
    
    /**
     * 统计用户的点赞数量
     */
    @Query("SELECT COUNT(gl) FROM GuideLike gl WHERE gl.user = :user")
    long countByUser(@Param("user") User user);
}
