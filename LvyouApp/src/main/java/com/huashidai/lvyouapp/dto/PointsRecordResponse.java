package com.huashidai.lvyouapp.dto;

import com.huashidai.lvyouapp.entity.PointsRecord;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分记录响应 DTO
 */
@Data
public class PointsRecordResponse {

    private Long id;
    private Integer points;  // 正数为获得，负数为消费
    private String type;     // EARN, SPEND, REFUND, ADMIN_ADJUST
    private String description;
    private String relatedType;
    private Long relatedId;
    private LocalDateTime createdAt;

    public static PointsRecordResponse fromEntity(PointsRecord record) {
        PointsRecordResponse dto = new PointsRecordResponse();
        dto.setId(record.getId());
        dto.setPoints(record.getPoints());
        dto.setType(record.getType() != null ? record.getType().name() : null);
        dto.setDescription(record.getDescription());
        dto.setRelatedType(record.getRelatedType());
        dto.setRelatedId(record.getRelatedId());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}
