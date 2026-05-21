package com.huashidai.lvyouapp.util;

import com.huashidai.lvyouapp.entity.Post;
import com.huashidai.lvyouapp.entity.User;
import com.huashidai.lvyouapp.repository.PostRepository;
import com.huashidai.lvyouapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 动态数据初始化器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PostDataInitializer implements CommandLineRunner {
    
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有动态数据
        if (postRepository.count() > 0) {
            log.info("动态数据已存在，跳过初始化");
            return;
        }
        
        log.info("开始初始化动态数据...");
        
        // 获取用户
        User traveler = userRepository.findByUsername("traveler").orElse(null);
        User hiker = userRepository.findByUsername("hiker").orElse(null);
        User testuser = userRepository.findByUsername("testuser").orElse(null);
        
        if (traveler == null || hiker == null || testuser == null) {
            log.warn("用户数据不存在，跳过动态数据初始化");
            return;
        }
        
        // 创建测试动态
        List<Post> posts = Arrays.asList(
            createPost("今天去了美丽的西湖，风景如画！推荐大家一定要来看看，特别是夕阳西下的时候，真的太美了！", 
                     "杭州西湖", "30.2741", "120.1551", Post.PostType.DYNAMIC, traveler, 15, 3, 2, 128),
            createPost("分享一个超棒的徒步路线！从黄山脚下到山顶，全程约8公里，风景绝美，体力好的朋友可以挑战一下！", 
                     "安徽黄山", "30.1324", "118.1674", Post.PostType.STRATEGY, hiker, 28, 7, 5, 256),
            createPost("刚参加完一个摄影活动，学到了很多拍照技巧！和大家分享几张作品，欢迎点评～", 
                     "北京798艺术区", "39.9042", "116.4074", Post.PostType.ACTIVITY, traveler, 42, 12, 8, 389),
            createPost("周末去了故宫，人山人海但是很值得！建议早上8点就去，避开人流高峰。", 
                     "北京故宫", "39.9163", "116.3972", Post.PostType.DYNAMIC, testuser, 23, 5, 3, 156),
            createPost("推荐一个超棒的民宿，环境优美，老板人很好，价格也很实惠！", 
                     "丽江古城", "26.8721", "100.2331", Post.PostType.STRATEGY, traveler, 35, 8, 6, 234),
            createPost("今天天气真好，适合出去走走。去了附近的公园，看到了很多美丽的花朵。", 
                     "上海人民公园", "31.2304", "121.4737", Post.PostType.DYNAMIC, hiker, 8, 2, 1, 67),
            createPost("分享一个美食攻略，这家餐厅的菜品真的很棒，环境也很好，强烈推荐！", 
                     "成都宽窄巷子", "30.6624", "104.0633", Post.PostType.STRATEGY, testuser, 19, 4, 2, 98),
            createPost("参加了一个户外活动，认识了很多志同道合的朋友，收获满满！", 
                     "深圳大鹏半岛", "22.5431", "114.0579", Post.PostType.ACTIVITY, hiker, 31, 9, 4, 187),
            createPost("今天去了博物馆，学到了很多历史知识，推荐大家也去看看。", 
                     "西安兵马俑", "34.3847", "109.2785", Post.PostType.DYNAMIC, traveler, 12, 3, 1, 89),
            createPost("分享一个省钱攻略，如何在旅行中节省开支，希望对大家有帮助！", 
                     "厦门鼓浪屿", "24.4473", "118.0644", Post.PostType.STRATEGY, testuser, 27, 6, 3, 145)
        );
        
        // 保存动态
        postRepository.saveAll(posts);
        
        log.info("动态数据初始化完成，共插入 {} 条动态", posts.size());
    }
    
    private Post createPost(String content, String location, String latitude, String longitude, 
                           Post.PostType type, User user, int likeCount, int commentCount, 
                           int shareCount, int viewCount) {
        Post post = new Post();
        post.setContent(content);
        post.setLocation(location);
        post.setLatitude(latitude);
        post.setLongitude(longitude);
        post.setType(type);
        post.setStatus(Post.PostStatus.APPROVED);
        post.setUser(user);
        post.setLikeCount(likeCount);
        post.setCommentCount(commentCount);
        post.setShareCount(shareCount);
        post.setViewCount(viewCount);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        return post;
    }
}
