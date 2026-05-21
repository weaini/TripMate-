package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.StrategyCreateRequest;
import com.huashidai.lvyouapp.dto.StrategyResponse;
import com.huashidai.lvyouapp.dto.UserResponse;
import com.huashidai.lvyouapp.entity.Strategy;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.StrategyRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import com.huashidai.lvyouapp.service.StrategyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 攻略服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StrategyServiceImpl implements StrategyService {
    
    private final StrategyRepository strategyRepository;
    private final UserRepository userRepository;
    
    @Override
    public StrategyResponse createStrategy(StrategyCreateRequest request) {
        // 创建攻略
        Strategy strategy = new Strategy();
        strategy.setTitle(request.getTitle());
        strategy.setContent(request.getContent());
        strategy.setDestination(request.getDestination());
        strategy.setSummary(request.getSummary());
        strategy.setBudget(request.getBudget());
        strategy.setDuration(request.getDuration());
        strategy.setType(request.getType());
        strategy.setLevel(request.getLevel());
        strategy.setStatus(Strategy.StrategyStatus.APPROVED); // 简化处理，直接通过
        strategy.setCreatedAt(LocalDateTime.now());
        
        // 设置作者（这里简化处理，实际应该从SecurityContext获取）
        User author = userRepository.findById(1L).orElseThrow(() -> new RuntimeException("用户不存在"));
        strategy.setAuthor(author);
        
        Strategy savedStrategy = strategyRepository.save(strategy);
        
        return convertToStrategyResponse(savedStrategy);
    }
    
    @Override
    public void uploadImages(Long strategyId, List<MultipartFile> files) {
        // 这里应该调用MinIO服务上传图片
        log.info("上传攻略图片，攻略ID: {}, 图片数量: {}", strategyId, files.size());
    }
    
    @Override
    public Page<StrategyResponse> getStrategies(Pageable pageable) {
        Page<Strategy> strategies = strategyRepository.findByStatusOrderByCreatedAtDesc(Strategy.StrategyStatus.APPROVED, pageable);
        return strategies.map(this::convertToStrategyResponse);
    }
    
    @Override
    public StrategyResponse getStrategy(Long id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        // 增加浏览量
        strategy.setViewCount(strategy.getViewCount() + 1);
        strategyRepository.save(strategy);
        
        return convertToStrategyResponse(strategy);
    }
    
    @Override
    public void likeStrategy(Long id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        strategy.setLikeCount(strategy.getLikeCount() + 1);
        strategyRepository.save(strategy);
    }
    
    @Override
    public void unlikeStrategy(Long id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        strategy.setLikeCount(Math.max(strategy.getLikeCount() - 1, 0));
        strategyRepository.save(strategy);
    }
    
    @Override
    public void deleteStrategy(Long id) {
        Strategy strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("攻略不存在"));
        
        strategy.setStatus(Strategy.StrategyStatus.REJECTED);
        strategyRepository.save(strategy);
    }
    
    private StrategyResponse convertToStrategyResponse(Strategy strategy) {
        StrategyResponse response = new StrategyResponse();
        response.setId(strategy.getId());
        response.setTitle(strategy.getTitle());
        response.setContent(strategy.getContent());
        response.setDestination(strategy.getDestination());
        response.setSummary(strategy.getSummary());
        response.setBudget(strategy.getBudget());
        response.setDuration(strategy.getDuration());
        response.setType(strategy.getType());
        response.setStatus(strategy.getStatus());
        response.setLevel(strategy.getLevel());
        response.setCoverImage(strategy.getCoverImage());
        response.setViewCount(strategy.getViewCount());
        response.setLikeCount(strategy.getLikeCount());
        response.setCommentCount(strategy.getCommentCount());
        response.setShareCount(strategy.getShareCount());
        response.setRewardPoints(strategy.getRewardPoints());
        response.setCreatedAt(strategy.getCreatedAt());
        response.setUpdatedAt(strategy.getUpdatedAt());
        response.setIsLiked(false); // 简化处理
        
        // 设置作者信息
        if (strategy.getAuthor() != null) {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(strategy.getAuthor().getId());
            userResponse.setUsername(strategy.getAuthor().getUsername());
            userResponse.setNickname(strategy.getAuthor().getNickname());
            userResponse.setAvatar(strategy.getAuthor().getAvatar());
            response.setAuthor(userResponse);
        }
        
        return response;
    }
}
