package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.onion.backend.dto.*;
import com.onion.backend.entity.Advertisement;
import com.onion.backend.entity.Article;
import com.onion.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }


    @Transactional
    public AdvertisementResDto writeAd(@RequestBody AdvertisementReqDto advertisementReqDto) {
        Advertisement advertisement = Advertisement.builder()
                .title(advertisementReqDto.getTitle())
                .content(advertisementReqDto.getContent())
                .startDate(advertisementReqDto.getStartDate())
                .endDate(advertisementReqDto.getEndDate())
                .build();
        advertisementRepository.save(advertisement);

        return getAdvertisementDto(advertisement);
    }
    @Transactional(readOnly = true)
    public Page<AdvertisementResDto> getAdvertisementList(int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber - 1, 10); // 페이지 번호는 0부터 시작
        Page<Advertisement> advertisementPage =  advertisementRepository.findAllByIsDeletedIsFalse(pageable);
        return advertisementPage.map(AdvertisementService::getAdvertisementDto);
    }
//    @Transactional
//    public ArticleResDto editArticle(Long boardId, Long articleId, ArticleReqDto dto) throws JsonProcessingException {
//        Optional<Article> optionalArticle = articleRepository.findById(articleId);
//
//        // 변경감지 엔티티 필드 변경
//        Article article = optionalArticle.get();  // Optional에서 엔티티 꺼내기
//        article.setTitle(dto.getTitle());
//        article.setContent(dto.getContent());
//        //elasticSearch 반영
//        indexArticle(article);
//        return getAdvertisementDto(article);
//    }
//    @Transactional
//    public void deleteArticle(Long boardId, Long articleId) throws JsonProcessingException {
//        Optional<Article> article = articleRepository.findById(articleId);
//        article.get().setIsDeleted(true);
//        //elasticSearch 반영
//        indexArticle(article.get());
//    }
    @Transactional
    public AdvertisementResDto getAdvertisement(Long adId){
        Optional<Advertisement> optionalAdvertisement = advertisementRepository.findById(adId);
        Advertisement advertisement = optionalAdvertisement.get();  // Optional에서 엔티티 꺼내기
        // 조회수 증가
        advertisement.setViewCount(advertisement.getViewCount() + 1);
        advertisementRepository.save(advertisement);
        return getAdvertisementDto(advertisement);
    }
    private static AdvertisementResDto getAdvertisementDto(Advertisement advertisement) {
        // Advertisement DTO로 변환
        return AdvertisementResDto.builder()
                .title(advertisement.getTitle())
                .content(advertisement.getContent())
                .startDate(advertisement.getStartDate())
                .endDate(advertisement.getEndDate())
                .build();
    }





}
