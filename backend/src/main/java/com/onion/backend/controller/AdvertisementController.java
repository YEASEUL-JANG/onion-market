package com.onion.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onion.backend.dto.AdvertisementReqDto;
import com.onion.backend.dto.AdvertisementResDto;
import com.onion.backend.dto.ArticleReqDto;
import com.onion.backend.dto.ArticleResDto;
import com.onion.backend.service.AdvertisementService;
import com.onion.backend.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdvertisementController {
    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }


    /**
     * 광고 발행
     * @return
     */
    @PostMapping("/advertisement")
    public ResponseEntity<AdvertisementResDto> writeAdvertisement(@RequestBody AdvertisementReqDto advertisementReqDto)
    {
        return ResponseEntity.ok(advertisementService.writeAd(advertisementReqDto));
    }

    /**
     * 게시글 조회
     * @param page
     * @return
     */
    @GetMapping("/advertisement/all")
    public ResponseEntity<Page<AdvertisementResDto>> getAdvertisementList(@RequestParam(value = "page" ,defaultValue = "1") int page) {
        Page<AdvertisementResDto> advertisementResDtos = advertisementService.getAdvertisementList(page);
        return ResponseEntity.ok(advertisementResDtos);
    }

    /**
     * 게시글 수정
     * @param boardId
     * @param articleId
     * @param articleReqDto
     * @return
     */
//    @PutMapping("/{boardId}/articles/{articleId}")
//    public ResponseEntity<ArticleResDto> editArticle(@PathVariable(value = "boardId") Long boardId,
//                                                     @PathVariable(value = "articleId") Long articleId,
//                                                     @RequestBody ArticleReqDto articleReqDto) throws JsonProcessingException {
//        return ResponseEntity.ok(articleService.editArticle(boardId,articleId, articleReqDto));
//    }

    /**
     * 게시글 삭제
     * @param boardId
     * @param articleId
     * @return
     */
//    @DeleteMapping("/{boardId}/articles/{articleId}")
//    public ResponseEntity<String> deleteArticle(@PathVariable(value = "boardId") Long boardId,
//                                               @PathVariable(value = "articleId") Long articleId) throws JsonProcessingException {
//        articleService.deleteArticle(boardId,articleId);
//        return ResponseEntity.ok("success");
//    }

    /**
     * 게시글 상세 조회
     */

    @GetMapping("/advertisement/{adId}")
    public ResponseEntity<AdvertisementResDto> getAdvertisementById(@PathVariable(value = "adId")Long adId) throws JsonProcessingException {
        AdvertisementResDto advertisementResDto = advertisementService.getAdvertisement(adId);
        return ResponseEntity.ok(advertisementResDto);
    }

}
