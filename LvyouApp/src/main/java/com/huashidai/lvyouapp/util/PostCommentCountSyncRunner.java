package com.huashidai.lvyouapp.util;

import com.huashidai.lvyouapp.entity.Comment;
import com.huashidai.lvyouapp.entity.Post;
import com.huashidai.lvyouapp.repository.CommentRepository;
import com.huashidai.lvyouapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动时按 comments 表重算每条动态的评论数，修复历史数据未维护 commentCount 的问题。
 */
@Component
@Order(110)
@RequiredArgsConstructor
@Slf4j
public class PostCommentCountSyncRunner implements CommandLineRunner {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        int updated = 0;
        for (Post p : postRepository.findAll()) {
            int real = commentRepository.countByPostAndStatus(p, Comment.CommentStatus.ACTIVE).intValue();
            Integer current = p.getCommentCount();
            if (current == null || current != real) {
                p.setCommentCount(real);
                postRepository.save(p);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("已按评论记录同步 {} 条动态的评论数", updated);
        }
    }
}

