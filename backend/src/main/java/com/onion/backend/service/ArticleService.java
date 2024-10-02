package com.onion.backend.service;

import com.onion.backend.dto.ArticleDto;
import com.onion.backend.dto.SignUpUser;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final Integer pageSize = 10;
    @Autowired
    public ArticleService(ArticleRepository articleRepository, BoardRepository boardRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }


    public Article writeArticle(@RequestBody ArticleDto articleDto, Long boardId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);
        if(author.isEmpty()){
            throw new ResourceNotFoundException("가입 유저를 찾을 수 없습니다.");
        }
        if(board.isEmpty()){
            throw new ResourceNotFoundException("게시판을 찾을 수 없습니다."+boardId);
        }

        Article article = Article.builder()
                .title(articleDto.getTitle())
                .content(articleDto.getContent())
                .author(author.get())
                .authorName(author.get().getUsername())
                .board(board.get())
                .build();

        articleRepository.save(article);
        return article;
    }

    public Page<Article> getArticlesBtBoardId(Long boardId, int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize); // 페이지 번호는 0부터 시작
        return articleRepository.findByBoardIdOrderByCreatedDateDesc(boardId, pageable);
    }

    public Article editArticle(Long boardId, Long articleId, ArticleDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 1. 작성자 조회
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        if(author.isEmpty()){
            throw new ResourceNotFoundException("가입 유저를 찾을 수 없습니다.");
        }

        // 2. 게시판 조회
        Optional<Board> board = boardRepository.findById(boardId);
        if(board.isEmpty()){
            throw new ResourceNotFoundException("게시판을 찾을 수 없습니다."+boardId);
        }

        // 3. 게시글 조회
        Optional<Article> article = articleRepository.findById(articleId);
        if(article.isEmpty()){
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다."+articleId);
        }

        // 4. 엔티티 필드 변경
        Article existingArticle = article.get();  // Optional에서 엔티티 꺼내기
        existingArticle.setTitle(dto.getTitle());
        existingArticle.setContent(dto.getContent());
        //* 변경감지로 DB데이터 업데이트
        return existingArticle;
    }
}
