package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Attraction;
import com.huashidai.lvyouapp.entity.AttractionImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 景点图片Repository
 */
@Repository
public interface AttractionImageRepository extends JpaRepository<AttractionImage, Long> {
    
    /**
     * 根据景点查询图片列表
     */
    List<AttractionImage> findByAttractionOrderBySortOrderAsc(Attraction attraction);
    
    /**
     * 删除景点的所有图片
     */
    void deleteByAttraction(Attraction attraction);
}

