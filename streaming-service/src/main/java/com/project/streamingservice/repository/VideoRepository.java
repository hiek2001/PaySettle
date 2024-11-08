package com.project.streamingservice.repository;

import com.project.streamingservice.dto.VideoDto;
import com.project.streamingservice.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v.id FROM Video v")
    List<Long> findAllIds();

    @Query("SELECT v FROM Video v WHERE v.id = :id")
    Video batchFindById(@Param("id") long id);

    @Query("SELECT new com.project.streamingservice.dto.VideoDto(v.id, v.videoViews) FROM Video v")
    List<VideoDto> batchFindAll();

    @Query("SELECT new com.project.streamingservice.dto.VideoDto(v.id, v.videoViews) FROM Video v WHERE v.id > :lastId ORDER BY v.id ASC LIMIT :pageSize")
    List<VideoDto> batchFindAfterId(@Param("lastId") Long lastId,@Param("pageSize") int pageSize);
}
