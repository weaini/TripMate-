package com.huashidai.lvyouapp.dto;

import lombok.Data;
import java.util.List;

/**
 * 评论列表响应DTO，包含评论数据和统计信息
 */
@Data
public class CommentListResponse {
    
    // 评论列表
    private List<CommentResponse> comments;
    
    // 总评论数（包括所有层级）
    private Long totalComments;
    
    // 一级评论数
    private Long topLevelComments;
    
    // 二级评论数
    private Long secondLevelComments;
    
    // 当前页
    private int currentPage;
    
    // 每页大小
    private int pageSize;
    
    // 总页数
    private int totalPages;
    
    // 是否有更多数据
    private boolean hasMore;
}
