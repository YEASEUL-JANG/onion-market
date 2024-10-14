package com.onion.backend.service;

import com.onion.backend.dto.ArticleReqDto;
import com.onion.backend.dto.ArticleResDto;
import com.onion.backend.dto.CommentResDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.User;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    public ArticleResDto writeArticle(@RequestBody ArticleReqDto articleReqDto, Long boardId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Board> board = boardRepository.findById(boardId);
        Article article = Article.builder()
                .title(articleReqDto.getTitle())
                .content(articleReqDto.getContent())
                .author(author.get())
                .authorName(author.get().getUsername())
                .board(board.get())
                .build();

        articleRepository.save(article);
        return getArticleResDto(article);
    }

    public Page<ArticleResDto> getArticlesBtBoardId(Long boardId, int pageNumber){
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize); // 페이지 번호는 0부터 시작
        Page<Article> articlePage =  articleRepository.findByBoardIdAndIsDeletedFalseOrderByCreatedDateDesc(boardId, pageable);

        return articlePage.map(ArticleService::getArticleResDto);
    }

    public ArticleResDto editArticle(Long boardId, Long articleId, ArticleReqDto dto) {
        Optional<Article> optionalArticle = articleRepository.findById(articleId);

        // 변경감지 엔티티 필드 변경
        Article article = optionalArticle.get();  // Optional에서 엔티티 꺼내기
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());

        return getArticleResDto(article);
    }

    public void deleteArticle(Long boardId, Long articleId) {
        Optional<Article> article = articleRepository.findById(articleId);
        article.get().setIsDeleted(true);
    }

    public ArticleResDto getArticleWithComments(Long boardId, Long articleId){
        Optional<Article> optionalArticle = articleRepository.findById(articleId);
        Article article = optionalArticle.get();  // Optional에서 엔티티 꺼내기
        // Comment 리스트를 DTO로 변환
        return getArticleResDto(article);
    }

    private static ArticleResDto getArticleResDto(Article article) {
        List<CommentResDto> commentResDtoList = article.getComments().stream()
                .map(comment -> new CommentResDto(comment.getId(), comment.getContent(), comment.getAuthorName()))
                .toList();

        // Article을 DTO로 변환
        return ArticleResDto.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .authorName(article.getAuthorName())
                .comments(commentResDtoList)
                .createdDate(article.getFormattedCreatedDate())
                .build();
    }
}
