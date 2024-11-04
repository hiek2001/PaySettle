package org.project.paysystem.revenue.repository;

import org.project.paysystem.revenue.entity.GlobalPricing;
import org.project.paysystem.revenue.entity.MediaTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlobalPricingRepository extends JpaRepository<GlobalPricing, Long> {

    // batch
    List<GlobalPricing> findAllByType(MediaTypeEnum type);
}
