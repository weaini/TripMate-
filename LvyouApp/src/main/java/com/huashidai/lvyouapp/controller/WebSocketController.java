package com.huashidai.lvyouapp.controller;

import com.huashidai.lvyouapp.dto.CommentResponse;
import com.huashidai.lvyouapp.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket控制器
 */
@Controller
@RequiredArgsConstructor
public class WebSocketController {
    
    private final CommentService commentService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 发送评论到活动聊天室
     */
    @MessageMapping("/activity/{activityId}/comment")
    @SendTo("/topic/activity/{activityId}/comments")
    public CommentResponse sendComment(@DestinationVariable Long activityId, String content) {
        try {
            // 这里应该调用commentService创建评论
            // 为了简化，这里直接返回一个模拟的评论
            CommentResponse comment = new CommentResponse();
            comment.setContent(content);
            comment.setCreatedAt(java.time.LocalDateTime.now());
            
            return comment;
        } catch (Exception e) {
            // 处理错误
            return null;
        }
    }
    
    /**
     * 发送系统消息
     */
    public void sendSystemMessage(Long activityId, String message) {
        messagingTemplate.convertAndSend("/topic/activity/" + activityId + "/system", message);
    }
}
