package org.project.paysystem.repository;

import org.project.paysystem.entity.Video;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface VideoPagingRepository extends PagingAndSortingRepository<Video, Long> {
}
