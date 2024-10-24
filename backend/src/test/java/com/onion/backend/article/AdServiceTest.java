package com.onion.backend.article;

import com.onion.backend.dto.AdvertisementReqDto;
import com.onion.backend.dto.ArticleReqDto;
import com.onion.backend.service.AdvertisementService;
import com.onion.backend.service.ArticleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;

@SpringBootTest
public class AdServiceTest {

    @Autowired
    private AdvertisementService advertisementService;

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
    public void create10MillionAdvertisement() throws Exception {
        Long boardId = 1L;

        // 1만 개의 게시글 발행
        for (int i = 3; i <= 10000; i++) {
            AdvertisementReqDto advertisementReqDto = new AdvertisementReqDto();
            advertisementReqDto.setTitle("테스트 광고 제목 " + i);
            advertisementReqDto.setContent("테스트 광고 내용 " + i);
            advertisementReqDto.setStartDate(LocalDateTime.now());
            advertisementReqDto.setEndDate(LocalDateTime.now().plusDays(7)); // 7일 후로 종료일 설정
            advertisementReqDto.setIsDeleted(false);
            advertisementReqDto.setIsVisible(true);
            // ArticleService의 메소드 호출
            advertisementService.writeAd(advertisementReqDto);

            // 결과 로그 출력
            System.out.println("Created article #" + i);
        }
    }

}
