package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.Post;
import com.huashidai.lvyouapp.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 动态图片数据访问层
 */
@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    
    /**
     * 根据动态ID查找所有图片
     */
    List<PostImage> findByPostIdOrderBySortOrder(Long postId);
    
    /**
     * 根据动态查找所有图片
     */
    List<PostImage> findByPostOrderBySortOrder(Post post);
    
    /**
     * 根据动态ID删除所有图片
     */
    void deleteByPostId(Long postId);
    
    /**
     * 根据动态删除所有图片
     */
    void deleteByPost(Post post);
}
