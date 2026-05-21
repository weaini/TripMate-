package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Strategy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 攻略创建请求DTO
 */
@Data
public class StrategyCreateRequest {
    
    @NotBlank(message = "攻略标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "攻略内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过10000个字符")
    private String content;
    
    @NotBlank(message = "目的地不能为空")
    @Size(max = 200, message = "目的地长度不能超过200个字符")
    private String destination;
    
    @Size(max = 1000, message = "摘要长度不能超过1000个字符")
    private String summary;
    
    private BigDecimal budget;
    
    @Positive(message = "天数必须大于0")
    private Integer duration;
    
    @NotNull(message = "攻略类型不能为空")
    private Strategy.StrategyType type;
    
    private Strategy.StrategyLevel level = Strategy.StrategyLevel.BEGINNER;
}
