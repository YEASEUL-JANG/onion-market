package com.onion.backend.config;

import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import com.onion.backend.aop.ArticleAspect;

@Configuration
@EnableAspectJAutoProxy // AOP 프록시 지원 활성화
@EnableTransactionManagement // 트랜잭션 관리 활성화
public class AopConfig {

    // Aspect 클래스 등록
        @Bean
        public ArticleAspect articleAspect(UserRepository userRepository,
                                           BoardRepository boardRepository,
                                           ArticleRepository articleRepository) {
            return new ArticleAspect(userRepository, boardRepository, articleRepository);
        }

}
