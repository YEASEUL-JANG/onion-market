package com.onion.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentResDto {
    private Long id;
    private String content;
    private String authorName;

    @Builder
    public CommentResDto(Long id, String content, String authorName){
        this.id = id;
        this.content = content;
        this.authorName = authorName;
    }
}
