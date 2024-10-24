package com.onion.backend.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdvertisementResDto {

    private String title;
    private String content;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer viewCount=0;
    private Integer clickCount=0;
    @Builder
    public AdvertisementResDto(String title,String content,LocalDateTime startDate,LocalDateTime endDate){
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;

    }
}
