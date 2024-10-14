package com.onion.backend.aop;

import com.onion.backend.dto.ArticleReqDto;
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
import org.aspectj.lang.annotation.Pointcut;
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

    // 게시글 발행, 수정, 삭제에 대한 Pointcut (권한 체크 필요)
    @Pointcut("execution(* com.onion.backend.service.ArticleService.writeArticle(..)) || " +
            "execution(* com.onion.backend.service.ArticleService.editArticle(..)) || " +
            "execution(* com.onion.backend.service.ArticleService.deleteArticle(..))")
    public void articleModificationMethods() {}

    // ArticleService의 모든 메서드에 적용되는 Pointcut (리소스 확인)
    @Pointcut("execution(* com.onion.backend.service.ArticleService.*(..))")
    public void allArticleServiceMethods() {}

    // boardId가 들어오는 메서드
    @Pointcut("args(boardId,..)")
    public void methodsWithBoardId(Long boardId) {}

    // articleId가 들어오는 메서드
    @Pointcut(value = "args(boardId, articleId,..)", argNames = "boardId,articleId")
    public void methodsWithArticleId(Long boardId, Long articleId) {}

    // 게시판 존재 확인 (모든 메서드에 대해)
    @Before(value = "allArticleServiceMethods() && methodsWithBoardId(boardId)", argNames = "boardId")
    public void checkBoardExists(Long boardId) {
        boardRepository.findById(boardId).orElseThrow(() -> new ResourceNotFoundException("게시판을 찾을 수 없습니다. ID: " + boardId));
    }

    // 게시글 존재 확인 (모든 메서드에 대해)
    @Before(value = "allArticleServiceMethods() && methodsWithArticleId(boardId, articleId)", argNames = "boardId,articleId")
    public void checkArticleExists(Long boardId, Long articleId) {
        articleRepository.findById(articleId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + articleId));
    }

    // 게시글 발행, 수정, 삭제 시 권한 확인
    @Before(value = "articleModificationMethods() && methodsWithArticleId(boardId, articleId)", argNames = "boardId,articleId")
    public void checkUserAuthorizationForModification(Long boardId, Long articleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Article> article = articleRepository.findById(articleId);

        if (author.isEmpty() || article.isEmpty() || !article.get().getAuthor().equals(author.get())) {
            throw new ForbiddenException("작성 권한이 없습니다.");
        }
    }
}