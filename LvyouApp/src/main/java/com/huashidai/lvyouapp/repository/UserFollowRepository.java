package com.huashidai.lvyouapp.repository;

import com.huashidai.lvyouapp.entity.UserFollow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFollowRepository extends JpaRepository<UserFollow, Long> {

    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    Optional<UserFollow> findByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    Page<UserFollow> findByFollower_IdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

    Page<UserFollow> findByFollowing_IdOrderByCreatedAtDesc(Long followingId, Pageable pageable);

    long countByFollower_Id(Long followerId);

    long countByFollowing_Id(Long followingId);

    @Query("SELECT uf.following.id FROM UserFollow uf WHERE uf.follower.id = :followerId")
    List<Long> findFollowingUserIdsByFollowerId(@Param("followerId") Long followerId);
}
