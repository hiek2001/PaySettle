package com.project.streamingservice.repository;


import com.project.streamingservice.entity.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


public interface AdRepository extends JpaRepository<Ad, Integer> {
    // 광고 랜덤 추출
    @Query(value = "SELECT * FROM paysystem.ad ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Ad findRandomAdByHash();
}
