package com.onion.backend.aop;

import com.onion.backend.dto.ArticleDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ForbiddenException;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.UserRepository;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class ArticleAspect {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public ArticleAspect(UserRepository userRepository, BoardRepository boardRepository, ArticleRepository articleRepository) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
    }

    // 모든 서비스 메서드에 대한 사용자 인증 확인
    @Before("execution(* com.onion.backend.service.ArticleService.*(..))")
    public void checkUserAuthorization() throws ResourceNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("가입 유저를 찾을 수 없습니다.");
        }
    }

    // 게시판이 존재하는지 확인
    @Before("execution(* com.onion.backend.service.ArticleService.*(..)) && args(boardId,..)")
    public void checkBoardExists(Long boardId) {
        Optional<Board> board = boardRepository.findById(boardId);
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("게시판을 찾을 수 없습니다. ID: " + boardId);
        }
    }

    // 게시글이 존재하는지 확인
    @Before(value = "execution(* com.onion.backend.service.ArticleService.*(..)) && args(boardId, articleId,..)", argNames = "boardId,articleId")
    public void checkArticleExists(Long boardId, Long articleId) {
        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + articleId);
        }
    }

    // 게시글 수정 시 권한 확인
    @Before(value = "execution(* com.onion.backend.service.ArticleService.editArticle(..)) && args(boardId, articleId, articleDto,..)", argNames = "boardId,articleId,articleDto")
    public void checkUserAuthorizationForEdit(Long boardId, Long articleId, ArticleDto articleDto) {
        checkAuthorization(boardId, articleId);
    }

    // 게시글 삭제 시 권한 확인
    @Before(value = "execution(* com.onion.backend.service.ArticleService.deleteArticle(..)) && args(boardId, articleId,..)", argNames = "boardId,articleId")
    public void checkUserAuthorizationForDelete(Long boardId, Long articleId) {
        checkAuthorization(boardId, articleId);
    }

    // 공통 권한 확인 로직
    private void checkAuthorization(Long boardId, Long articleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Article> article = articleRepository.findById(articleId);

        if (author.isEmpty() || article.isEmpty() || !article.get().getAuthor().equals(author.get())) {
            throw new ForbiddenException("작성 권한이 없습니다.");
        }
    }
}
