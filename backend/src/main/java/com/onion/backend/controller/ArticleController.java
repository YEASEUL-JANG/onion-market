package com.onion.backend.controller;

import com.onion.backend.dto.ArticleReqDto;
import com.onion.backend.dto.ArticleResDto;
import com.onion.backend.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

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
     * @param articleReqDto
     * @return
     */
    @PostMapping("/{boardId}/articles")
    public ResponseEntity<ArticleResDto> writeArticle(@PathVariable(value = "boardId") Long boardId, @RequestBody ArticleReqDto articleReqDto) {
        return ResponseEntity.ok(articleService.writeArticle(articleReqDto, boardId));
    }

    /**
     * 게시글 조회
     * @param boardId
     * @param page
     * @return
     */
    @GetMapping("/{boardId}/articles")
    public ResponseEntity<Page<ArticleResDto>> getArticlesByBoard(@PathVariable(value = "boardId") Long boardId,
                                                            @RequestParam(value = "page" ,defaultValue = "1") int page) {
        Page<ArticleResDto> articleDtos = articleService.getArticlesByBoardId(boardId, page);
        return ResponseEntity.ok(articleDtos);
    }

    /**
     * 게시글 수정
     * @param boardId
     * @param articleId
     * @param articleReqDto
     * @return
     */
    @PutMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<ArticleResDto> editArticle(@PathVariable(value = "boardId") Long boardId,
                                                     @PathVariable(value = "articleId") Long articleId,
                                                     @RequestBody ArticleReqDto articleReqDto) {
        return ResponseEntity.ok(articleService.editArticle(boardId,articleId, articleReqDto));
    }

    /**
     * 게시글 삭제
     * @param boardId
     * @param articleId
     * @return
     */
    @DeleteMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<String> deleteArticle(@PathVariable(value = "boardId") Long boardId,
                                               @PathVariable(value = "articleId") Long articleId) {
        articleService.deleteArticle(boardId,articleId);
        return ResponseEntity.ok("success");
    }

    /**
     * 게시글 상세 조회
     */

    @GetMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<ArticleResDto> getArticleWithComment(@PathVariable(value = "boardId")Long boardId,
                                                         @PathVariable(value = "articleId")Long articleId){
        ArticleResDto articleResDto = articleService.getArticleWithComments(boardId,articleId);
        return ResponseEntity.ok(articleResDto);
    }


}
