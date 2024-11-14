package com.onion.backend.dto;

import com.onion.backend.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class HotArticleDto implements Serializable {

    private Long id;

    private String title;

    private String content;

    private String authorName;


    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Long viewCount=0L;
}
