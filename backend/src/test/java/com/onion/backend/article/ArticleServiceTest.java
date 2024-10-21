package com.onion.backend.article;

import com.onion.backend.dto.ArticleReqDto;
import com.onion.backend.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @BeforeEach
    public void setup() {
        // 사용자 정보 설정 (UsernamePasswordAuthenticationToken을 사용하여 인증 객체 생성)
        UserDetails user = User.withUsername("yeaseul")
                .password("dptmfl12")
                .roles("USER")
                .build();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // SecurityContext에 인증 정보 설정
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }


    @Test
    public void create10MillionArticles() throws Exception {
        Long boardId = 1L;

        // 10만 개 이상의 게시글 발행
        for (int i = 1; i <= 100000; i++) {
            ArticleReqDto articleReqDto = new ArticleReqDto();
            articleReqDto.setTitle("테스트 게시글 제목 " + i);
            articleReqDto.setContent("테스트 게시글 내용 " + i);

            // ArticleService의 메소드 호출
            articleService.writeArticle(articleReqDto, boardId);

            // 결과 로그 출력
            System.out.println("Created article #" + i);
        }
    }

}
