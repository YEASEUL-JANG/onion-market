package com.onion.backend.controller;

import com.onion.backend.dto.ArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.User;
import com.onion.backend.service.ArticleService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
public class ArticleController {
    private final ArticleService articleService;
    private final AuthenticationManager authenticationManager;

    public ArticleController(ArticleService articleService, AuthenticationManager authenticationManager) {
        this.articleService = articleService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * 게시글 발행
     * @param boardId
     * @param articleDto
     * @return
     */
    @PostMapping("/{boardId}/articles")
    public ResponseEntity<Article> writeArticle(@PathVariable(value = "boardId") Long boardId, @RequestBody ArticleDto articleDto) {
        return ResponseEntity.ok(articleService.writeArticle(articleDto, boardId));
    }

    /**
     * 게시글 조회
     * @param boardId
     * @param page
     * @return
     */
    @GetMapping("/{boardId}/articles")
    public ResponseEntity<Page<Article>> getArticlesByBoard(@PathVariable(value = "boardId") Long boardId,
                                                            @RequestParam(value = "page" ,defaultValue = "1") int page) {
        Page<Article> articles = articleService.getArticlesBtBoardId(boardId, page);
        return ResponseEntity.ok(articles);
    }

    /**
     * 게시글 수정
     * @param boardId
     * @param articleId
     * @param articleDto
     * @return
     */
    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<Article> editArticle(@PathVariable(value = "boardId") Long boardId,
                                                     @PathVariable(value = "articleId") Long articleId,
                                                     @RequestBody ArticleDto articleDto) {
        return ResponseEntity.ok(articleService.editArticle(boardId,articleId,articleDto));
    }
}
