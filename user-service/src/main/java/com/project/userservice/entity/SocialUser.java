package com.project.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="socialUser")
public class SocialUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long kakaoId;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private PlatformEnum platformType;

    @Builder
    public SocialUser(Long kakaoId, PlatformEnum platformType) {
        this.kakaoId = kakaoId;
        this.platformType = platformType;
    }
}
