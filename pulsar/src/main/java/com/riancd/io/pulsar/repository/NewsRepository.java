package com.riancd.io.pulsar.repository;

import com.riancd.io.pulsar.model.News;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    @Query(value = "SELECT * FROM tb_news ORDER BY embedding <-> cast(:vector as vector) LIMIT :limit", nativeQuery = true)
    List<News> findSimilarNews(@Param("vector") String vector, @Param("limit") int limit);
}
