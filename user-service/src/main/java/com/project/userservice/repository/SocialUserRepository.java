package com.project.userservice.repository;

import com.project.userservice.entity.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialUserRepository extends JpaRepository<SocialUser, Long> {
    Optional<SocialUser> findByKakaoId(Long kakaoId);
}
