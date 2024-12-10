package com.onion.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onion.backend.config.BatchConfig;
import com.onion.backend.dto.ArticleReqDto;
import com.onion.backend.dto.ArticleResDto;
import com.onion.backend.dto.HotArticleDto;
import com.onion.backend.service.ArticleService;
import com.onion.backend.service.ElasticsearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/boards")
public class ArticleController {
    private final ArticleService articleService;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String,Object> redisTemplate;

    private final ElasticsearchService elasticsearchService;


    public ArticleController(ArticleService articleService, AuthenticationManager authenticationManager, RedisTemplate<String, Object> redisTemplate, ElasticsearchService elasticsearchService) {
        this.articleService = articleService;
        this.authenticationManager = authenticationManager;
        this.redisTemplate = redisTemplate;
        this.elasticsearchService = elasticsearchService;
    }
    /**
     * 게시글 발행
     * @param boardId
     * @param articleReqDto
     * @return
     */
    @PostMapping("/{boardId}/articles")
    public ResponseEntity<ArticleResDto> writeArticle(@PathVariable(value = "boardId") Long boardId, @RequestBody ArticleReqDto articleReqDto)
            throws JsonProcessingException {
        return ResponseEntity.ok(articleService.writeArticle(articleReqDto, boardId));
    }
    /**
     * 전체게시글 조회(페이징 x)
     * @param boardId
     * @return
     */
    @GetMapping("/{boardId}/all-articles")
    public ResponseEntity<List<ArticleResDto>> getAllArticlesByBoard(@PathVariable(value = "boardId") Long boardId) {
        List<ArticleResDto> articleDtos = articleService.getAllArticlesByBoardId(boardId);
        return ResponseEntity.ok(articleDtos);
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
                                                     @RequestBody ArticleReqDto articleReqDto) throws JsonProcessingException {
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
                                               @PathVariable(value = "articleId") Long articleId) throws JsonProcessingException {
        articleService.deleteArticle(boardId,articleId);
        return ResponseEntity.ok("success");
    }

    /**
     * 게시글 상세 조회
     */

    @GetMapping("/{boardId}/articles/{articleId}")
    public ResponseEntity<ArticleResDto> getArticleWithComment(@PathVariable(value = "boardId")Long boardId,
                                                         @PathVariable(value = "articleId")Long articleId) throws JsonProcessingException {
        //redis cash 에서 먼저 조회
        //Object object =  redisTemplate.opsForHash().get(BatchConfig.YEST_REDIS_KEY+articleId,articleId);

        //elasticSearch 에서 조회
        ArticleResDto articleResDto = elasticsearchService.getArticleById(articleId).block();
        if (articleResDto != null) {
            // Elasticsearch에서 조회 성공 시 바로 반환
            return ResponseEntity.ok(articleResDto);
        }

//        if(object!=null){
//            HotArticleDto hotArticleDto = (HotArticleDto)object;
//            ArticleResDto articleResDto = ArticleResDto.builder()
//                    .id(hotArticleDto.getId())
//                    .title(hotArticleDto.getTitle())
//                    .content(hotArticleDto.getContent())
//                    .authorName(hotArticleDto.getAuthorName())
//                    .createdDate(String.valueOf(hotArticleDto.getCreatedDate()))
//                    .viewCount(hotArticleDto.getViewCount())
//                    .build();
//            return ResponseEntity.ok(articleResDto);
//        }
        //redis 에 없을 경우 DB조회
        ArticleResDto articleDBResDto = articleService.getArticleWithComments(boardId,articleId);
        return ResponseEntity.ok(articleDBResDto);
    }

    /**
     * 게시글 키워드 검색
     */

    @PostMapping("/{boardId}/articles/search")
    public ResponseEntity<List<ArticleResDto>> searchArticle(@PathVariable(value = "boardId")Long boardId,
                                                               @RequestParam(value = "keyword",required = true) String keyword) {
            List<ArticleResDto> articleResDtos = articleService.searchArticle(boardId,keyword);
            return ResponseEntity.ok(articleResDtos);
    }
}
