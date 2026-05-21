package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.AttractionCreateRequest;
import com.huashidai.lvyouapp.dto.AttractionResponse;
import com.huashidai.lvyouapp.dto.AttractionUpdateRequest;
import com.huashidai.lvyouapp.entity.Attraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 景点服务接口
 */
public interface AttractionService {
    
    /**
     * 创建景点（管理员）
     */
    AttractionResponse createAttraction(AttractionCreateRequest request);
    
    /**
     * 更新景点（管理员）
     */
    AttractionResponse updateAttraction(Long id, AttractionUpdateRequest request);
    
    /**
     * 删除景点（管理员）
     */
    void deleteAttraction(Long id);
    
    /**
     * 获取景点详情（公开）
     */
    AttractionResponse getAttraction(Long id);
    
    /**
     * 获取景点列表（公开）
     */
    Page<AttractionResponse> getAttractions(
            String keyword,
            String type,
            String city,
            String province,
            String level,
            String sort,
            Pageable pageable);
    
    /**
     * 获取所有景点（管理员，包括已删除和停用的）
     */
    Page<AttractionResponse> getAllAttractionsForAdmin(
            String keyword,
            String type,
            String city,
            String province,
            String level,
            String sort,
            Pageable pageable);
    
    /**
     * 收藏景点（用户）
     */
    void favoriteAttraction(Long id);
    
    /**
     * 取消收藏景点（用户）
     */
    void unfavoriteAttraction(Long id);
    
    /**
     * 获取用户的收藏列表
     */
    Page<AttractionResponse> getUserFavorites(Pageable pageable);
    
    /**
     * 检查用户是否已收藏景点
     */
    boolean isFavorited(Long attractionId);
    
    /**
     * 上传景点图片（管理员）
     */
    void uploadImages(Long attractionId, java.util.List<org.springframework.web.multipart.MultipartFile> files);
}

