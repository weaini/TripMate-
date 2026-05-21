package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Attraction;
import com.huashidai.lvyouapp.entity.AttractionFavorite;
import com.huashidai.lvyouapp.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 景点收藏Repository
 */
@Repository
public interface AttractionFavoriteRepository extends JpaRepository<AttractionFavorite, Long> {
    
    /**
     * 检查用户是否已收藏景点
     */
    boolean existsByAttractionAndUser(Attraction attraction, User user);
    
    /**
     * 根据景点和用户查找收藏记录
     */
    Optional<AttractionFavorite> findByAttractionAndUser(Attraction attraction, User user);
    
    /**
     * 删除收藏记录
     */
    void deleteByAttractionAndUser(Attraction attraction, User user);
    
    /**
     * 查询用户的收藏列表
     */
    Page<AttractionFavorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    /**
     * 统计景点的收藏数
     */
    long countByAttraction(Attraction attraction);
}

