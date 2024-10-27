package org.project.paysystem.repository;

import org.project.paysystem.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v.id FROM Video v")
    List<Long> findAllIds();

    @Query("SELECT v FROM Video v WHERE v.id = :id")
    Video batchFindById(@Param("id") long id);
}
