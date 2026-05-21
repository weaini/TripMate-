package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Attraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 景点Repository
 */
@Repository
public interface AttractionRepository extends JpaRepository<Attraction, Long> {
    
    /**
     * 根据状态查询景点
     */
    Page<Attraction> findByStatusOrderByCreatedAtDesc(Attraction.AttractionStatus status, Pageable pageable);
    
    /**
     * 根据类型查询景点
     */
    Page<Attraction> findByTypeAndStatusOrderByCreatedAtDesc(
            Attraction.AttractionType type, 
            Attraction.AttractionStatus status, 
            Pageable pageable);
    
    /**
     * 根据城市查询景点
     */
    Page<Attraction> findByCityAndStatusOrderByCreatedAtDesc(
            String city, 
            Attraction.AttractionStatus status, 
            Pageable pageable);
    
    /**
     * 根据省份查询景点
     */
    Page<Attraction> findByProvinceAndStatusOrderByCreatedAtDesc(
            String province, 
            Attraction.AttractionStatus status, 
            Pageable pageable);
    
    /**
     * 搜索景点（名称、描述、城市）
     * 注意：使用Pageable中的Sort，不在这里硬编码排序
     */
    @Query("SELECT a FROM Attraction a WHERE a.status = :status AND " +
           "(a.name LIKE %:keyword% OR a.description LIKE %:keyword% OR a.city LIKE %:keyword%)")
    Page<Attraction> searchAttractions(
            @Param("keyword") String keyword,
            @Param("status") Attraction.AttractionStatus status,
            Pageable pageable);
    
    /**
     * 根据等级查询景点
     */
    Page<Attraction> findByLevelAndStatusOrderByCreatedAtDesc(
            Attraction.AttractionLevel level,
            Attraction.AttractionStatus status,
            Pageable pageable);
    
    /**
     * 查询热门景点（按浏览量排序）
     */
    Page<Attraction> findByStatusOrderByViewCountDesc(
            Attraction.AttractionStatus status,
            Pageable pageable);
    
    /**
     * 查询收藏最多的景点
     */
    Page<Attraction> findByStatusOrderByFavoriteCountDesc(
            Attraction.AttractionStatus status,
            Pageable pageable);
    
    /**
     * 根据ID和状态查询
     */
    Optional<Attraction> findByIdAndStatus(Long id, Attraction.AttractionStatus status);
    
    /**
     * 查询所有景点（不限制状态，用于管理员）
     */
    Page<Attraction> findAll(Pageable pageable);
    
    /**
     * 根据类型查询所有景点（不限制状态）
     */
    Page<Attraction> findByType(Attraction.AttractionType type, Pageable pageable);
    
    /**
     * 根据城市查询所有景点（不限制状态）
     */
    Page<Attraction> findByCity(String city, Pageable pageable);
    
    /**
     * 搜索所有景点（不限制状态）
     */
    @Query("SELECT a FROM Attraction a WHERE " +
           "(a.name LIKE %:keyword% OR a.description LIKE %:keyword% OR a.city LIKE %:keyword%)")
    Page<Attraction> searchAllAttractions(
            @Param("keyword") String keyword,
            Pageable pageable);
}

