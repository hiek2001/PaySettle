package com.project.revenueservice.repository;

import com.project.revenueservice.entity.GlobalPricing;
import com.project.revenueservice.entity.MediaTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlobalPricingRepository extends JpaRepository<GlobalPricing, Long> {

    // batch
    List<GlobalPricing> findAllByType(MediaTypeEnum type);
}
