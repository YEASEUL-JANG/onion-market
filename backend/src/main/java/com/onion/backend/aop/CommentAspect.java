package com.onion.backend.aop;

import com.onion.backend.dto.CommentReqDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Board;
import com.onion.backend.entity.Comment;
import com.onion.backend.entity.User;
import com.onion.backend.exception.ForbiddenException;
import com.onion.backend.exception.ResourceNotFoundException;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.BoardRepository;
import com.onion.backend.repository.CommentRepository;
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
public class CommentAspect {
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public CommentAspect(UserRepository userRepository, BoardRepository boardRepository, ArticleRepository articleRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
    }

    // 모든 서비스 메서드에 대한 사용자 인증 확인
    @Before("execution(* com.onion.backend.service.CommentService.*(..))")
    public void checkUserAuthorization() throws ResourceNotFoundException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        if (author.isEmpty()) {
            throw new ResourceNotFoundException("가입 유저를 찾을 수 없습니다.");
        }
    }

    // 게시판이 존재하는지 확인
    @Before("execution(* com.onion.backend.service.CommentService.*(..)) && args(boardId,..)")
    public void checkBoardExists(Long boardId) {
        Optional<Board> board = boardRepository.findById(boardId);
        if (board.isEmpty()) {
            throw new ResourceNotFoundException("게시판을 찾을 수 없습니다. ID: " + boardId);
        }
    }

    // 게시글이 존재하는지 확인
    @Before(value = "execution(* com.onion.backend.service.CommentService.*(..)) && args(boardId, articleId,..)", argNames = "boardId,articleId")
    public void checkArticleExists(Long boardId, Long articleId) {
        Optional<Article> article = articleRepository.findById(articleId);
        if (article.isEmpty()) {
            throw new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + articleId);
        }
        if (article.get().getIsDeleted()) {
            throw new ResourceNotFoundException("게시글이 삭제되었습니다: " + articleId);
        }
    }

    // 댓글이 존재하는지 확인
    @Before(value = "execution(* com.onion.backend.service.CommentService.*(..)) && " +
            "args(boardId, articleId, commentId,..)", argNames = "boardId,articleId,commentId")
    public void checkCommentExists(Long boardId, Long articleId, Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        if (comment.isEmpty()) {
            throw new ResourceNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId);
        }
    }

    // 댓글 수정 시 권한 확인
    @Before(value = "execution(* com.onion.backend.service.CommentService.editComment(..)) && " +
            "args(boardId, articleId, commentId, commentReqDto,..)", argNames = "boardId,articleId,commentId,commentDto")
    public void checkUserAuthorizationForEdit(Long boardId, Long articleId, Long commentId, CommentReqDto commentReqDto) {
        checkAuthorization(boardId, articleId, commentId);
    }

    // 댓글 삭제 시 권한 확인
    @Before(value = "execution(* com.onion.backend.service.CommentService.deleteComment(..)) && " +
            "args(boardId, articleId, commentId,..)", argNames = "boardId,articleId,commentId")
    public void checkUserAuthorizationForDelete(Long boardId, Long articleId, Long commentId) {
        checkAuthorization(boardId, articleId, commentId);
    }

    // 공통 권한 확인 로직
    private void checkAuthorization(Long boardId, Long articleId, Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Article> article = articleRepository.findById(articleId);
        Optional<Comment> comment = commentRepository.findById(commentId);

        if (author.isEmpty() || article.isEmpty() || comment.isEmpty() || !comment.get().getAuthor().equals(author.get())) {
            throw new ForbiddenException("작성 권한이 없습니다.");
        }
    }
}