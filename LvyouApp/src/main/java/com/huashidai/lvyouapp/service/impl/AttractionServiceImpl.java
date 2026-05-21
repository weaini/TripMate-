package com.huashidai.lvyouapp.service.impl;

import com.huashidai.lvyouapp.dto.AttractionCreateRequest;
import com.huashidai.lvyouapp.dto.AttractionResponse;
import com.huashidai.lvyouapp.dto.AttractionUpdateRequest;
import com.huashidai.lvyouapp.entity.Attraction;
import com.huashidai.lvyouapp.entity.AttractionFavorite;
import com.huashidai.lvyouapp.entity.AttractionImage;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.AttractionFavoriteRepository;
import com.huashidai.lvyouapp.repository.AttractionImageRepository;
import com.huashidai.lvyouapp.repository.AttractionRepository;
import com.huashidai.lvyouapp.service.AttractionService;
import com.huashidai.lvyouapp.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.huashidai.lvyouapp.util.FileStorageUtil;
import com.huashidai.lvyouapp.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 景点服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AttractionServiceImpl implements AttractionService {
    
    private final AttractionRepository attractionRepository;
    private final AttractionImageRepository attractionImageRepository;
    private final AttractionFavoriteRepository attractionFavoriteRepository;
    
    @Override
    @Transactional
    public AttractionResponse createAttraction(AttractionCreateRequest request) {
        // 检查管理员权限
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有管理员才能创建景点");
        }
        
        // 创建景点
        Attraction attraction = new Attraction();
        attraction.setName(request.getName());
        attraction.setDescription(request.getDescription());
        attraction.setIntroduction(request.getIntroduction());
        attraction.setCountry(request.getCountry());
        attraction.setProvince(request.getProvince());
        attraction.setCity(request.getCity());
        attraction.setAddress(request.getAddress());
        attraction.setLatitude(request.getLatitude());
        attraction.setLongitude(request.getLongitude());
        attraction.setCoverImage(request.getCoverImage());
        attraction.setType(request.getType());
        attraction.setLevel(request.getLevel() != null ? request.getLevel() : Attraction.AttractionLevel.UNRATED);
        attraction.setTicketPrice(request.getTicketPrice());
        attraction.setOpeningHours(request.getOpeningHours());
        attraction.setContactPhone(request.getContactPhone());
        attraction.setWebsite(request.getWebsite());
        attraction.setTips(request.getTips());
        attraction.setRating(request.getRating() != null ? request.getRating() : 0);
        attraction.setStatus(Attraction.AttractionStatus.ACTIVE);
        attraction.setCreatedBy(currentUser);
        
        attraction = attractionRepository.save(attraction);
        
        // 保存图片
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            List<AttractionImage> images = new ArrayList<>();
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                AttractionImage image = new AttractionImage();
                image.setAttraction(attraction);
                image.setImageUrl(request.getImageUrls().get(i));
                image.setSortOrder(i);
                images.add(image);
            }
            attractionImageRepository.saveAll(images);
        }
        
        log.info("管理员 {} 创建了景点: {}", currentUser.getUsername(), attraction.getName());
        return convertToAttractionResponse(attraction);
    }
    
    @Override
    @Transactional
    public AttractionResponse updateAttraction(Long id, AttractionUpdateRequest request) {
        // 检查管理员权限
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有管理员才能更新景点");
        }
        
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("景点不存在"));
        
        // 更新字段
        if (request.getName() != null) {
            attraction.setName(request.getName());
        }
        if (request.getDescription() != null) {
            attraction.setDescription(request.getDescription());
        }
        if (request.getIntroduction() != null) {
            attraction.setIntroduction(request.getIntroduction());
        }
        if (request.getCountry() != null) {
            attraction.setCountry(request.getCountry());
        }
        if (request.getProvince() != null) {
            attraction.setProvince(request.getProvince());
        }
        if (request.getCity() != null) {
            attraction.setCity(request.getCity());
        }
        if (request.getAddress() != null) {
            attraction.setAddress(request.getAddress());
        }
        if (request.getLatitude() != null) {
            attraction.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            attraction.setLongitude(request.getLongitude());
        }
        if (request.getCoverImage() != null) {
            attraction.setCoverImage(request.getCoverImage());
        }
        if (request.getType() != null) {
            attraction.setType(request.getType());
        }
        if (request.getLevel() != null) {
            attraction.setLevel(request.getLevel());
        }
        if (request.getTicketPrice() != null) {
            attraction.setTicketPrice(request.getTicketPrice());
        }
        if (request.getOpeningHours() != null) {
            attraction.setOpeningHours(request.getOpeningHours());
        }
        if (request.getContactPhone() != null) {
            attraction.setContactPhone(request.getContactPhone());
        }
        if (request.getWebsite() != null) {
            attraction.setWebsite(request.getWebsite());
        }
        if (request.getTips() != null) {
            attraction.setTips(request.getTips());
        }
        if (request.getRating() != null) {
            attraction.setRating(request.getRating());
        }
        if (request.getStatus() != null) {
            attraction.setStatus(request.getStatus());
        }
        
        attraction = attractionRepository.save(attraction);
        
        // 更新图片
        if (request.getImageUrls() != null) {
            // 删除旧图片
            attractionImageRepository.deleteByAttraction(attraction);
            
            // 添加新图片
            if (!request.getImageUrls().isEmpty()) {
                List<AttractionImage> images = new ArrayList<>();
                for (int i = 0; i < request.getImageUrls().size(); i++) {
                    AttractionImage image = new AttractionImage();
                    image.setAttraction(attraction);
                    image.setImageUrl(request.getImageUrls().get(i));
                    image.setSortOrder(i);
                    images.add(image);
                }
                attractionImageRepository.saveAll(images);
            }
        }
        
        log.info("管理员 {} 更新了景点: {}", currentUser.getUsername(), attraction.getName());
        return convertToAttractionResponse(attraction);
    }
    
    @Override
    @Transactional
    public void deleteAttraction(Long id) {
        // 检查管理员权限
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有管理员才能删除景点");
        }
        
        Attraction attraction = attractionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("景点不存在"));
        
        // 软删除
        attraction.setStatus(Attraction.AttractionStatus.DELETED);
        attractionRepository.save(attraction);
        
        log.info("管理员 {} 删除了景点: {}", currentUser.getUsername(), attraction.getName());
    }
    
    @Override
    @Transactional
    public AttractionResponse getAttraction(Long id) {
        Attraction attraction = attractionRepository.findByIdAndStatus(
                id, Attraction.AttractionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("景点不存在或已删除"));
        
        // 确保图片列表被加载（触发懒加载）
        if (attraction.getImages() != null) {
            attraction.getImages().size(); // 触发懒加载
        }
        
        // 增加浏览量
        attraction.setViewCount((attraction.getViewCount() != null ? attraction.getViewCount() : 0) + 1);
        attractionRepository.save(attraction);
        
        return convertToAttractionResponse(attraction);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttractionResponse> getAttractions(
            String keyword,
            String type,
            String city,
            String province,
            String level,
            String sort,
            Pageable pageable) {
        
        // 根据sort参数创建正确的Pageable对象（移除原有的sort，使用我们自己的排序逻辑）
        Sort sortObj = getSortFromString(sort);
        Pageable finalPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortObj
        );
        
        Page<Attraction> attractions;
        
        // 搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            attractions = attractionRepository.searchAttractions(
                    keyword, Attraction.AttractionStatus.ACTIVE, finalPageable);
        }
        // 按类型筛选
        else if (type != null && !type.trim().isEmpty()) {
            try {
                Attraction.AttractionType attractionType = Attraction.AttractionType.valueOf(type.toUpperCase());
                attractions = getAttractionsByTypeAndSort(attractionType, sort, finalPageable);
            } catch (IllegalArgumentException e) {
                attractions = getAttractionsBySort(sort, finalPageable);
            }
        }
        // 按城市筛选
        else if (city != null && !city.trim().isEmpty()) {
            attractions = getAttractionsByCityAndSort(city, sort, finalPageable);
        }
        // 按省份筛选
        else if (province != null && !province.trim().isEmpty()) {
            attractions = getAttractionsByProvinceAndSort(province, sort, finalPageable);
        }
        // 按等级筛选
        else if (level != null && !level.trim().isEmpty()) {
            try {
                Attraction.AttractionLevel attractionLevel = Attraction.AttractionLevel.valueOf(level.toUpperCase());
                attractions = getAttractionsByLevelAndSort(attractionLevel, sort, finalPageable);
            } catch (IllegalArgumentException e) {
                attractions = getAttractionsBySort(sort, finalPageable);
            }
        }
        // 仅排序
        else {
            attractions = getAttractionsBySort(sort, finalPageable);
        }
        
        return attractions.map(this::convertToAttractionResponse);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttractionResponse> getAllAttractionsForAdmin(
            String keyword,
            String type,
            String city,
            String province,
            String level,
            String sort,
            Pageable pageable) {
        
        // 根据sort参数创建正确的Pageable对象
        Sort sortObj = getSortFromString(sort);
        Pageable finalPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortObj
        );
        
        Page<Attraction> attractions;
        
        // 搜索（不限制状态）
        if (keyword != null && !keyword.trim().isEmpty()) {
            attractions = attractionRepository.searchAllAttractions(keyword, finalPageable);
        }
        // 按类型筛选（不限制状态）
        else if (type != null && !type.trim().isEmpty()) {
            try {
                Attraction.AttractionType attractionType = Attraction.AttractionType.valueOf(type.toUpperCase());
                attractions = attractionRepository.findByType(attractionType, finalPageable);
            } catch (IllegalArgumentException e) {
                attractions = attractionRepository.findAll(finalPageable);
            }
        }
        // 按城市筛选（不限制状态）
        else if (city != null && !city.trim().isEmpty()) {
            attractions = attractionRepository.findByCity(city, finalPageable);
        }
        // 其他情况查询所有
        else {
            attractions = attractionRepository.findAll(finalPageable);
        }
        
        return attractions.map(this::convertToAttractionResponse);
    }
    
    /**
     * 根据排序字符串创建Sort对象
     */
    private Sort getSortFromString(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        switch (sort.toLowerCase()) {
            case "popular":
                return Sort.by(Sort.Direction.DESC, "viewCount");
            case "favorite":
                return Sort.by(Sort.Direction.DESC, "favoriteCount");
            case "latest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
    
    /**
     * 根据排序获取景点（无筛选条件）
     */
    private Page<Attraction> getAttractionsBySort(String sort, Pageable pageable) {
        if ("popular".equalsIgnoreCase(sort)) {
            return attractionRepository.findByStatusOrderByViewCountDesc(
                    Attraction.AttractionStatus.ACTIVE, pageable);
        } else if ("favorite".equalsIgnoreCase(sort)) {
            return attractionRepository.findByStatusOrderByFavoriteCountDesc(
                    Attraction.AttractionStatus.ACTIVE, pageable);
        } else {
            return attractionRepository.findByStatusOrderByCreatedAtDesc(
                    Attraction.AttractionStatus.ACTIVE, pageable);
        }
    }
    
    /**
     * 根据类型和排序获取景点
     */
    private Page<Attraction> getAttractionsByTypeAndSort(
            Attraction.AttractionType type, String sort, Pageable pageable) {
        // 由于Repository方法已经固定了排序，我们需要使用通用查询
        // 这里先使用默认排序，后续可以优化为使用@Query支持动态排序
        return attractionRepository.findByTypeAndStatusOrderByCreatedAtDesc(
                type, Attraction.AttractionStatus.ACTIVE, pageable);
    }
    
    /**
     * 根据城市和排序获取景点
     */
    private Page<Attraction> getAttractionsByCityAndSort(String city, String sort, Pageable pageable) {
        return attractionRepository.findByCityAndStatusOrderByCreatedAtDesc(
                city, Attraction.AttractionStatus.ACTIVE, pageable);
    }
    
    /**
     * 根据省份和排序获取景点
     */
    private Page<Attraction> getAttractionsByProvinceAndSort(String province, String sort, Pageable pageable) {
        return attractionRepository.findByProvinceAndStatusOrderByCreatedAtDesc(
                province, Attraction.AttractionStatus.ACTIVE, pageable);
    }
    
    /**
     * 根据等级和排序获取景点
     */
    private Page<Attraction> getAttractionsByLevelAndSort(
            Attraction.AttractionLevel level, String sort, Pageable pageable) {
        return attractionRepository.findByLevelAndStatusOrderByCreatedAtDesc(
                level, Attraction.AttractionStatus.ACTIVE, pageable);
    }
    
    @Override
    @Transactional
    public void favoriteAttraction(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Attraction attraction = attractionRepository.findByIdAndStatus(
                id, Attraction.AttractionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("景点不存在或已删除"));
        
        // 检查是否已收藏
        if (attractionFavoriteRepository.existsByAttractionAndUser(attraction, currentUser)) {
            throw new RuntimeException("已经收藏过该景点");
        }
        
        // 创建收藏记录
        AttractionFavorite favorite = new AttractionFavorite();
        favorite.setAttraction(attraction);
        favorite.setUser(currentUser);
        attractionFavoriteRepository.save(favorite);
        
        // 更新收藏数
        attraction.setFavoriteCount(attraction.getFavoriteCount() + 1);
        attractionRepository.save(attraction);
        
        log.info("用户 {} 收藏了景点: {}", currentUser.getUsername(), attraction.getName());
    }
    
    @Override
    @Transactional
    public void unfavoriteAttraction(Long id) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Attraction attraction = attractionRepository.findByIdAndStatus(
                id, Attraction.AttractionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("景点不存在或已删除"));
        
        // 删除收藏记录
        attractionFavoriteRepository.deleteByAttractionAndUser(attraction, currentUser);
        
        // 更新收藏数
        attraction.setFavoriteCount(Math.max(0, attraction.getFavoriteCount() - 1));
        attractionRepository.save(attraction);
        
        log.info("用户 {} 取消收藏了景点: {}", currentUser.getUsername(), attraction.getName());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<AttractionResponse> getUserFavorites(Pageable pageable) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        
        Page<AttractionFavorite> favorites = attractionFavoriteRepository.findByUserOrderByCreatedAtDesc(
                currentUser, pageable);
        
        return favorites.map(favorite -> convertToAttractionResponse(favorite.getAttraction()));
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(Long attractionId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElse(null);
        if (attraction == null) {
            return false;
        }
        
        return attractionFavoriteRepository.existsByAttractionAndUser(attraction, currentUser);
    }
    
    @Override
    @Transactional
    public void uploadImages(Long attractionId, List<MultipartFile> files) {
        // 检查管理员权限
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null || currentUser.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("只有管理员才能上传景点图片");
        }
        
        // 验证景点是否存在
        Attraction attraction = attractionRepository.findById(attractionId)
                .orElseThrow(() -> new RuntimeException("景点不存在"));
        
        // 验证文件
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("请选择要上传的图片");
        }
        
        // 检查文件数量限制（最多20张）
        if (files.size() > 20) {
            throw new RuntimeException("最多只能上传20张图片");
        }
        
        List<AttractionImage> attractionImages = new ArrayList<>();
        
        // 处理每个文件
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) {
                continue;
            }
            
            // 验证文件类型
            if (!FileStorageUtil.isValidImageFile(file)) {
                throw new RuntimeException("只能上传图片文件，支持JPG、PNG格式");
            }
            
            // 验证文件大小（10MB限制）
            if (!FileStorageUtil.isValidFileSize(file, 10 * 1024 * 1024)) {
                throw new RuntimeException("图片大小不能超过10MB");
            }
            
            try {
                // 保存图片文件
                String imageUrl = FileStorageUtil.saveFile(file, "attractions");
                log.info("景点图片上传成功: {}", imageUrl);
                
                // 创建AttractionImage实体
                AttractionImage attractionImage = new AttractionImage();
                attractionImage.setAttraction(attraction);
                attractionImage.setImageUrl(imageUrl);
                attractionImage.setSortOrder(i);
                attractionImage.setCaption(""); // 可以后续添加图片说明功能
                
                attractionImages.add(attractionImage);
                
            } catch (Exception e) {
                log.error("图片上传失败: {}", e.getMessage());
                throw new RuntimeException("图片上传失败: " + e.getMessage());
            }
        }
        
        // 批量保存图片信息到数据库
        if (!attractionImages.isEmpty()) {
            try {
                // 先删除该景点的旧图片记录（可选，根据需求决定是否保留旧图片）
                // attractionImageRepository.deleteByAttraction(attraction);
                
                // 批量保存新的图片记录
                attractionImageRepository.saveAll(attractionImages);
                
                log.info("景点 {} 的 {} 张图片信息已保存到数据库", attractionId, attractionImages.size());
            } catch (Exception e) {
                log.error("保存图片信息到数据库失败: {}", e.getMessage());
                // 如果数据库保存失败，删除已上传的文件
                for (AttractionImage image : attractionImages) {
                    try {
                        FileStorageUtil.deleteFile(image.getImageUrl());
                    } catch (Exception deleteException) {
                        log.error("删除文件失败: {}", deleteException.getMessage());
                    }
                }
                throw new RuntimeException("保存图片信息失败，请重试");
            }
        }
        
        log.info("景点 {} 的 {} 张图片上传完成", attractionId, files.size());
    }
    
    /**
     * 转换为响应DTO
     */
    private AttractionResponse convertToAttractionResponse(Attraction attraction) {
        AttractionResponse response = new AttractionResponse();
        response.setId(attraction.getId());
        response.setName(attraction.getName());
        response.setDescription(attraction.getDescription());
        response.setIntroduction(attraction.getIntroduction());
        response.setCountry(attraction.getCountry());
        response.setProvince(attraction.getProvince());
        response.setCity(attraction.getCity());
        response.setAddress(attraction.getAddress());
        response.setLatitude(attraction.getLatitude());
        response.setLongitude(attraction.getLongitude());
        response.setCoverImage(attraction.getCoverImage());
        response.setType(attraction.getType());
        response.setTypeName(getTypeName(attraction.getType()));
        response.setLevel(attraction.getLevel());
        response.setLevelName(getLevelName(attraction.getLevel()));
        response.setTicketPrice(attraction.getTicketPrice());
        response.setOpeningHours(attraction.getOpeningHours());
        response.setContactPhone(attraction.getContactPhone());
        response.setWebsite(attraction.getWebsite());
        response.setTips(attraction.getTips());
        response.setViewCount(attraction.getViewCount());
        response.setFavoriteCount(attraction.getFavoriteCount());
        response.setRating(attraction.getRating());
        response.setStatus(attraction.getStatus());
        response.setCreatedAt(attraction.getCreatedAt());
        response.setUpdatedAt(attraction.getUpdatedAt());
        
        // 设置收藏状态（仅在用户已登录时检查）
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            response.setIsFavorited(attractionFavoriteRepository.existsByAttractionAndUser(attraction, currentUser));
        } else {
            response.setIsFavorited(false);
        }
        
        // 设置图片列表
        if (attraction.getImages() != null) {
            response.setImages(attraction.getImages().stream()
                    .map(img -> {
                        AttractionResponse.AttractionImageResponse imgResponse = 
                                new AttractionResponse.AttractionImageResponse();
                        imgResponse.setId(img.getId());
                        imgResponse.setImageUrl(img.getImageUrl());
                        imgResponse.setCaption(img.getCaption());
                        imgResponse.setSortOrder(img.getSortOrder());
                        return imgResponse;
                    })
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
    
    /**
     * 获取类型中文名称
     */
    private String getTypeName(Attraction.AttractionType type) {
        if (type == null) {
            return "";
        }
        switch (type) {
            case NATURAL: return "自然景观";
            case CULTURAL: return "文化古迹";
            case ENTERTAINMENT: return "娱乐休闲";
            case MUSEUM: return "博物馆";
            case PARK: return "公园";
            case TEMPLE: return "寺庙";
            case MOUNTAIN: return "山岳";
            case WATER: return "水域";
            case ARCHITECTURE: return "建筑";
            case OTHER: return "其他";
            default: return "";
        }
    }
    
    /**
     * 获取等级中文名称
     */
    private String getLevelName(Attraction.AttractionLevel level) {
        if (level == null) {
            return "";
        }
        switch (level) {
            case LEVEL_5A: return "5A级";
            case LEVEL_4A: return "4A级";
            case LEVEL_3A: return "3A级";
            case LEVEL_2A: return "2A级";
            case LEVEL_1A: return "1A级";
            case UNRATED: return "未评级";
            default: return "";
        }
    }
}

