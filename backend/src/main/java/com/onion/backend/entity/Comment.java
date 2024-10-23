package com.onion.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Auditing 기능을 사용하기 위한 설정
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)) //실제로 외래키를 세팅하지 않음.
    private User author;

    @Column(nullable = false)
    private String authorName;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Article article;

    // 댓글이 처음 생성된 날짜 (자동 설정)
    @CreatedDate
    @Column(insertable = true)
    private LocalDateTime createdDate;

    // 글 정보가 갱신될 때마다 자동으로 설정
    @LastModifiedDate //코드레벨에서만 적용
    private LocalDateTime updatedDate;

    @Column(nullable = false)
    private Boolean isDeleted = false;


    @Builder
    public Comment(String content, User author, Article article, String authorName) {
        this.content = content;
        this.author = author;
        this.article = article;
        this.authorName = authorName;
    }
}