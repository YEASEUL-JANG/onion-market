package com.onion.backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Auditing 기능을 사용하기 위한 설정
public class Advertisement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    // 글이 처음 생성된 날짜 (자동 설정)
    @CreatedDate
    @Column(insertable = true)
    private LocalDateTime createdDate;

    // 글 정보가 갱신될 때마다 자동으로 설정
    @LastModifiedDate //코드레벨에서만 적용
    private LocalDateTime updatedDate;

    // 광고의 시작일
    @Column(insertable = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startDate;

    // 광고의 종료일
    @Column(insertable = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endDate;


    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Column(nullable = false)
    private Boolean isVisible = true;

    @Column(nullable = false)
    private Integer viewCount=0;

    @Column(nullable = false)
    private Integer clickCount=0;


    @Builder
    public Advertisement(String title,String content,LocalDateTime startDate,LocalDateTime endDate){
        this.title = title;
        this.content = content;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
