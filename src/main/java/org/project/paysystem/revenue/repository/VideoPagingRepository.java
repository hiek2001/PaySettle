package org.project.paysystem.revenue.repository;

import org.project.paysystem.streaming.entity.Video;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface VideoPagingRepository extends PagingAndSortingRepository<Video, Long> {

    // batch
    Video findById(Long videoId);
}
