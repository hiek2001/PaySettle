package org.project.paysystem.repository;

import org.project.paysystem.entity.Ads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface AdsRepository extends JpaRepository<Ads, Integer> {
    // 광고 ID를 해시값으로 변환하여 광고 랜덤 추출
    @Query(value = "SELECT * FROM paysystem.ads WHERE MOD(CONV(SUBSTRING(MD5(ads_id), 1, 15), 16, 10), 100) < 50 LIMIT 1", nativeQuery = true)
    Ads findRandomAdByHash();
}
