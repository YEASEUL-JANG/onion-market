package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.onion.backend.dto.*;
import com.onion.backend.entity.AdClickHistory;
import com.onion.backend.entity.AdViewHistory;
import com.onion.backend.entity.Advertisement;
import com.onion.backend.entity.Article;
import com.onion.backend.repository.AdclickHistoryRepository;
import com.onion.backend.repository.AdvertisementRepository;
import com.onion.backend.repository.AdviewHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AdviewHistoryRepository adviewHistoryRepository;
    private final AdclickHistoryRepository adclickHistoryRepository;
    private static final String REDIS_KEY = "ad:";

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository, RedisTemplate<String, Object> redisTemplate, AdviewHistoryRepository adviewHistoryRepository, AdclickHistoryRepository adclickHistoryRepository) {
        this.advertisementRepository = advertisementRepository;
        this.redisTemplate = redisTemplate;
        this.adviewHistoryRepository = adviewHistoryRepository;
        this.adclickHistoryRepository = adclickHistoryRepository;
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

        //redis 저장
        redisTemplate.opsForHash().put(REDIS_KEY+advertisement.getId(),advertisement.getId(), advertisement);

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
    public AdvertisementResDto getAdvertisement(Long adId, String clientIp, Boolean isTrueView){
        insertAdViewHistory(adId,clientIp,isTrueView);
        Object object =  redisTemplate.opsForHash().get(REDIS_KEY,adId);
        Advertisement advertisement;
        if (object!=null){
            advertisement = (Advertisement) redisTemplate.opsForHash().get(REDIS_KEY,adId);
        }else{
            Optional<Advertisement> optionalAdvertisement = advertisementRepository.findById(adId);
            advertisement = optionalAdvertisement.get();  // Optional에서 엔티티 꺼내기
        }

        assert advertisement != null;
        return getAdvertisementDto(advertisement);
    }

    public void clickAdvertisement(Long adId, String ipAddress) {
        AdClickHistory adClickHistory = AdClickHistory.builder()
                .adId(adId)
                .clientIp(ipAddress)
                .build();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!principal.equals("anonymousUser")){
            UserDetails userDetails = (UserDetails) principal;
            adClickHistory.setUsername(userDetails.getUsername());
        }
        adclickHistoryRepository.save(adClickHistory);

    }

    private void insertAdViewHistory(Long adId, String clientIp, Boolean isTrueView){

        AdViewHistory adViewHistory = AdViewHistory.builder()
                .adId(adId)
                .clientIp(clientIp)
                .isTrueView(isTrueView)
                .build();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (!principal.equals("anonymousUser")){
            UserDetails userDetails = (UserDetails) principal;
            adViewHistory.setUsername(userDetails.getUsername());
        }
        adviewHistoryRepository.save(adViewHistory);

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
