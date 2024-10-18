package org.project.paysystem.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name="ads")
public class Ads {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adsId; // 동영상에 삽입된 광고 ID
    private Long adsViews; // 광고 조회수
    private Long watchedTime; // 광고가 삽입된 지점
    private String adsUrl; // 광고 URL
}
