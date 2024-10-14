package com.onion.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleReqDto {
    private String title;
    private String content;
}
