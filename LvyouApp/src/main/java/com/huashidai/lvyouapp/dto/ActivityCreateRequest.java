package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.Activity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 活动创建请求DTO
 */
@Data
public class ActivityCreateRequest {
    
    @NotBlank(message = "活动标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;
    
    @NotBlank(message = "活动描述不能为空")
    @Size(max = 2000, message = "描述长度不能超过2000个字符")
    private String description;
    
    @NotBlank(message = "目的地不能为空")
    @Size(max = 200, message = "目的地长度不能超过200个字符")
    private String destination;
    
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;
    
    @NotNull(message = "报名截止时间不能为空")
    private LocalDateTime registrationDeadline;
    
    @NotNull(message = "最大参与人数不能为空")
    @Positive(message = "最大参与人数必须大于0")
    private Integer maxParticipants;
    
    private BigDecimal cost;
    
    @NotBlank(message = "活动类型不能为空")
    private String type;
    
    @NotBlank(message = "难度等级不能为空")
    private String level;
    
    @Size(max = 500, message = "要求说明长度不能超过500个字符")
    private String requirements;
    
    @Size(max = 200, message = "联系方式长度不能超过200个字符")
    private String contactInfo;
}
