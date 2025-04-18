package com.onion.backend.repository;


import com.onion.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByArticleIdAndIsDeletedFalseOrderByCreatedDateDesc(Long articleId, Pageable pageable);

    List<Comment> findByArticleId(Long id);
}
