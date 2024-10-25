package com.onion.backend.entity;


import com.onion.backend.repository.AdviewHistoryRepository;
import lombok.Builder;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;

@Document(collection = "adViewHistory")
@Setter
public class AdViewHistory {

    @Id
    private String id;
    private Long adId;
    private String username;
    private String clientIp;
    private Boolean isTrueView = false;
    private LocalDateTime createdDate = LocalDateTime.now();
    @Builder
    public AdViewHistory(Long adId, String username,String clientIp, Boolean isTrueView){
        this.adId = adId;
        this.username = username;
        this.clientIp = clientIp;
        this.isTrueView = isTrueView;
    }

}
