package com.onion.backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ArticleNotificationDto {
    private String type = "write_article";
    private Long articleId;
    private Long userId;
}
