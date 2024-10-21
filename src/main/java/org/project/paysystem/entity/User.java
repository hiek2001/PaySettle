package org.project.paysystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRoleEnum role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="social_id")
    private SocialUser socialUser;

   // private Long kakaoId;

    @Builder
    public User(String username, UserRoleEnum role, String email, SocialUser socialUser) {
        this.username = username;
        this.role = role;
        this.email = email;
        this.socialUser = socialUser;
    }

//    public User kakaoIdUpdate(Long kakaoId) {
//        this.kakaoId = kakaoId;
//        return this;
//    }
}
