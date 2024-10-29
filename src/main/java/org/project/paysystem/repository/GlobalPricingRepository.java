package org.project.paysystem.repository;

import org.project.paysystem.entity.GlobalPricing;
import org.project.paysystem.entity.MediaTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GlobalPricingRepository extends JpaRepository<GlobalPricing, Long> {

    // batch
    List<GlobalPricing> findAllByType(MediaTypeEnum type);
}
