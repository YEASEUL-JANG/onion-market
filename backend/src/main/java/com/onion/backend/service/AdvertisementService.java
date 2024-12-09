package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.onion.backend.dto.*;
import com.onion.backend.entity.*;
import com.onion.backend.repository.AdclickHistoryRepository;
import com.onion.backend.repository.AdvertisementRepository;
import com.onion.backend.repository.AdviewHistoryRepository;
import com.onion.backend.repository.AdviewStatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdvertisementService {
    private final AdvertisementRepository advertisementRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AdviewHistoryRepository adviewHistoryRepository;
    private final AdclickHistoryRepository adclickHistoryRepository;
    private static final String REDIS_KEY = "ad:";
    private final MongoTemplate mongoTemplate;

    private final AdviewStatRepository adviewStatRepository;

    @Autowired
    public AdvertisementService(AdvertisementRepository advertisementRepository, RedisTemplate<String, Object> redisTemplate, AdviewHistoryRepository adviewHistoryRepository, AdclickHistoryRepository adclickHistoryRepository, MongoTemplate mongoTemplate, AdviewStatRepository adviewStatRepository) {
        this.advertisementRepository = advertisementRepository;
        this.redisTemplate = redisTemplate;
        this.adviewHistoryRepository = adviewHistoryRepository;
        this.adclickHistoryRepository = adclickHistoryRepository;
        this.mongoTemplate = mongoTemplate;
        this.adviewStatRepository = adviewStatRepository;
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
        redisTemplate.opsForHash().put(REDIS_KEY,advertisement.getId(), advertisement);

        return getAdvertisementDto(advertisement);
    }
    @Transactional(readOnly = true)
    public List<AdvertisementResDto> getAdvertisementList(){
        //redis 에서 먼저 조회
        List<Object> redisData = redisTemplate.opsForHash().values(REDIS_KEY);

        List<Advertisement> advertisementList = redisData.stream()
                .map(object -> (Advertisement) object)
                .toList();
        // Redis에 없으면 DB에서 조회하고 캐싱
        if (advertisementList.isEmpty()) {
            advertisementList = advertisementRepository.findAllByIsDeletedIsFalse();
            //redis 에 다시 캐싱
            advertisementList.forEach(ad -> redisTemplate.opsForHash().put(REDIS_KEY, ad.getId(), ad));
        }
        return advertisementList.stream().map(AdvertisementService::getAdvertisementDto).toList();
    }
    @Transactional(readOnly = true)
    public AdvertisementResDto getAdvertisement(Long adId, String clientIp, Boolean isTrueView){
        insertAdViewHistory(adId,clientIp,isTrueView);
        // Redis에서 조회
        Advertisement advertisement = (Advertisement) redisTemplate.opsForHash().get(REDIS_KEY, adId);

        // Redis에 없으면 DB에서 조회하고 Redis에 캐싱
        if (advertisement == null) {
            advertisement = advertisementRepository.findById(adId)
                    .orElseThrow(() -> new IllegalArgumentException("Advertisement not found for ID: " + adId));
            // Redis에 저장
            redisTemplate.opsForHash().put(REDIS_KEY, adId, advertisement);
        }
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
                .id(advertisement.getId())
                .title(advertisement.getTitle())
                .content(advertisement.getContent())
                .startDate(advertisement.getStartDate())
                .endDate(advertisement.getEndDate())
                .build();
    }

    public List<AdViewHistoryResDto> countUniqueFieldByAdIdForYesterday(String fieldName, boolean fieldExists) {
        // 어제의 시작 시간과 끝 시간
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDateTime.now(); // 어제부터 현재 시간까지

        // 필터 조건: fieldName이 존재하지 않거나(null) 특정 조건에 따라 다르게 설정
        Criteria criteria = Criteria.where("createdDate").gte(yesterdayStart).lt(yesterdayEnd)
                .and("adId").ne(null);

        // 필드 존재 여부에 따른 조건 설정
        if (fieldExists) {
            criteria.and(fieldName).ne(null); // 필드가 존재하며 null이 아닌 경우
        } else {
            criteria.orOperator(
                    Criteria.where(fieldName).exists(false),  // 필드가 존재하지 않는 경우
                    Criteria.where(fieldName).is(null)        // 필드가 null인 경우
            );
        }

        String groupingField = fieldExists ? fieldName : "clientIp";

        // Aggregation 단계 정의
        MatchOperation matchStage = Aggregation.match(criteria);

        GroupOperation groupStage = Aggregation.group("adId")
                .addToSet(groupingField).as("uniqueValues");


        ProjectionOperation projectStage = Aggregation.project("_id")
                .andExpression("size(uniqueValues)").as("count");
        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage, projectStage);


        // Aggregation 파이프라인 생성
        AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "adViewHistory", Map.class);

        return results.getMappedResults().stream()
                .map(result -> {
                    Long adId = result.get("_id") != null ? Long.valueOf(result.get("_id").toString()) : null;
                    Integer count = result.get("count") != null ? (Integer) result.get("count") : 0;
                    return new AdViewHistoryResDto(adId, count);
                })
                .collect(Collectors.toList());
    }
    public List<AdViewHistoryResDto> getAdViewHistoryGroupedByAdId(){
        List<AdViewHistoryResDto> usernameResult = countUniqueFieldByAdIdForYesterday("username", true);
        List<AdViewHistoryResDto> clientIpResult = countUniqueFieldByAdIdForYesterday("username", false);
        Map<Long, AdViewHistoryResDto> resultMap = usernameResult.stream()
                .collect(Collectors.toMap(AdViewHistoryResDto::getAdId, dto -> dto));

        for (AdViewHistoryResDto clientIpDto : clientIpResult) {
            Long adId = clientIpDto.getAdId();

            // 이미 존재하는 adId이면 count 값을 더하고, 없으면 새로 추가
            resultMap.merge(adId, clientIpDto, (existing, newDto) -> {
                existing.setCount(existing.getCount() + newDto.getCount());
                return existing;
            });
        }

        return new ArrayList<>(resultMap.values());
    }

    public void insertAdViewStat(List<AdViewHistoryResDto> results){
        ArrayList<AdViewStat> arrayList = new ArrayList<>();
        for (AdViewHistoryResDto item : results){
            AdViewStat adViewStat = new AdViewStat();
            adViewStat.setAdId(item.getAdId());
            adViewStat.setCount(item.getCount());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = LocalDateTime.now().minusDays(1).format(formatter);
            adViewStat.setDt(formattedDate);
            arrayList.add(adViewStat);
        }
        adviewStatRepository.saveAll(arrayList);
    }
}
