package org.project.paysystem.repository;

import org.project.paysystem.entity.SocialUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialUserRepository extends JpaRepository<SocialUser, Long> {
    Optional<SocialUser> findByKakaoId(Long kakaoId);
}
