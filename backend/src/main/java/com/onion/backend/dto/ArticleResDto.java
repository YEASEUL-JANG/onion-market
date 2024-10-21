package com.onion.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
public class ArticleResDto {
    private Long id;
    private String title;
    private String content;
    private String authorName;
    private Long viewCount;
    private String createdDate;
    private List<CommentResDto> comments;

    private ArticleResDto(){}

    @Builder
    public ArticleResDto(Long id, String title, String content, String authorName, List<CommentResDto> comments, String createdDate, Long viewCount) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.authorName = authorName;
        this.viewCount = viewCount;
        this.comments = comments;
        this.createdDate = createdDate;
    }
}
