package org.project.paysystem.repository;

import org.project.paysystem.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoId(Long kakaoId);

    Optional<User> findByEmail(String kakaoEmail);

    Optional<User> findByUsername(String username);
}
