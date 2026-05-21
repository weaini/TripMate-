package com.huashidai.lvyouapp.service;

import com.huashidai.lvyouapp.dto.StrategyCreateRequest;
import com.huashidai.lvyouapp.dto.StrategyResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 攻略服务接口
 */
public interface StrategyService {
    
    /**
     * 创建攻略
     */
    StrategyResponse createStrategy(StrategyCreateRequest request);
    
    /**
     * 上传攻略图片
     */
    void uploadImages(Long strategyId, List<MultipartFile> files);
    
    /**
     * 获取攻略列表
     */
    Page<StrategyResponse> getStrategies(Pageable pageable);
    
    /**
     * 获取攻略详情
     */
    StrategyResponse getStrategy(Long id);
    
    /**
     * 点赞攻略
     */
    void likeStrategy(Long id);
    
    /**
     * 取消点赞
     */
    void unlikeStrategy(Long id);
    
    /**
     * 删除攻略
     */
    void deleteStrategy(Long id);
}
