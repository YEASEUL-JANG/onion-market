package com.onion.backend.dto;

import com.onion.backend.entity.User;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDto {
    private String title;
    private String content;
}
