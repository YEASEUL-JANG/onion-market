package com.onion.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onion.backend.dto.*;
import com.onion.backend.service.AdvertisementService;
import com.onion.backend.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
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
    @PostMapping("/create/ad")
    public ResponseEntity<AdvertisementResDto> writeAdvertisement(@RequestBody AdvertisementReqDto advertisementReqDto)
    {
        return ResponseEntity.ok(advertisementService.writeAd(advertisementReqDto));
    }

    /**
     * 광고 전체 조회
     * @param page
     * @return
     */
    @GetMapping("/advertisement/all")
    public ResponseEntity<Page<AdvertisementResDto>> getAdvertisementList(@RequestParam(value = "page" ,defaultValue = "1") int page) {
        Page<AdvertisementResDto> advertisementResDtos = advertisementService.getAdvertisementList(page);
        return ResponseEntity.ok(advertisementResDtos);
    }


    /**
     * 광고 개별 조회
     */

    @GetMapping("/advertisement/{adId}")
    public ResponseEntity<AdvertisementResDto> getAdvertisementById(@PathVariable(value = "adId")Long adId,
                                                                    HttpServletRequest request,
                                                                    @RequestParam(value = "isTrueView", defaultValue = "false") Boolean isTrueView)  {
        String ipAddress = request.getRemoteAddr();
        AdvertisementResDto advertisementResDto = advertisementService.getAdvertisement(adId,ipAddress,isTrueView);
        return ResponseEntity.ok(advertisementResDto);
    }

    /**
     * 광고 클릭
     */

    @PostMapping("/advertisement/{adId}")
    public ResponseEntity<String> clickAdvertisement(@PathVariable(value = "adId")Long adId,
                                                                    HttpServletRequest request)  {
        String ipAddress = request.getRemoteAddr();
        advertisementService.clickAdvertisement(adId,ipAddress);
        return ResponseEntity.ok("ad click");
    }

    /**
     * 광고 히스토리 조회
     */

    @GetMapping("/advertisement/history")
    public ResponseEntity<List<AdViewHistoryResDto>> getAdHistory(){
        List<AdViewHistoryResDto> adViewHistoryResDtoList = advertisementService.getAdViewHistoryGroupedByAdId();
        advertisementService.insertAdViewStat(adViewHistoryResDtoList);
        return ResponseEntity.ok(adViewHistoryResDtoList);
    }

}
