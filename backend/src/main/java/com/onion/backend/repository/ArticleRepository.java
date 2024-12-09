package com.onion.backend.repository;


import com.onion.backend.entity.Article;
import com.onion.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByBoardIdAndIsDeletedFalseOrderByCreatedDateDesc(Long boardId, Pageable pageable);

    @Query("SELECT a FROM Article a WHERE a.createdDate >= :startDate AND a.createdDate < :endDate ORDER BY a.viewCount DESC LIMIT 1")
    Article findHotArticle(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    List<Article> findTop15ByBoardIdAndIsDeletedFalseOrderByCreatedDateDesc(Long boardId);

}
