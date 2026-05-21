package com.huashidai.lvyouapp.util;

import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.UserFollowRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 启动时按 user_follows 表重算每位用户的粉丝数、关注数，避免历史种子数据（如 50/20）与真实关系不一致。
 */
@Component
@Order(100)
@RequiredArgsConstructor
@Slf4j
public class FollowCountSyncRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;

    @Override
    @Transactional
    public void run(String... args) {
        int updated = 0;
        for (User u : userRepository.findAll()) {
            int followers = (int) userFollowRepository.countByFollowing_Id(u.getId());
            int following = (int) userFollowRepository.countByFollower_Id(u.getId());
            if (!java.util.Objects.equals(u.getFollowersCount(), followers)
                    || !java.util.Objects.equals(u.getFollowingCount(), following)) {
                u.setFollowersCount(followers);
                u.setFollowingCount(following);
                userRepository.save(u);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("已按关注关系同步 {} 个用户的粉丝/关注数", updated);
        }
    }
}
