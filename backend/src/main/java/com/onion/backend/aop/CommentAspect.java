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
import org.aspectj.lang.annotation.Pointcut;
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

    // 댓글 작성, 수정, 삭제에 대한 Pointcut (권한 체크 필요)
    @Pointcut("execution(* com.onion.backend.service.CommentService.writeComment(..)) || " +
            "execution(* com.onion.backend.service.CommentService.editComment(..)) || " +
            "execution(* com.onion.backend.service.CommentService.deleteComment(..))")
    public void commentModificationMethods() {}

    // CommentService의 모든 메서드에 적용되는 Pointcut (리소스 확인)
    @Pointcut("execution(* com.onion.backend.service.CommentService.*(..))")
    public void allCommentServiceMethods() {}

    // boardId가 들어오는 메서드
    @Pointcut("args(boardId,..)")
    public void methodsWithBoardId(Long boardId) {}

    // articleId가 들어오는 메서드
    @Pointcut(value = "args(boardId, articleId,..)", argNames = "boardId,articleId")
    public void methodsWithArticleId(Long boardId, Long articleId) {}

    // commentId가 들어오는 메서드
    @Pointcut(value = "args(boardId, articleId, commentId,..)", argNames = "boardId,articleId,commentId")
    public void methodsWithCommentId(Long boardId, Long articleId, Long commentId) {}

    // 게시판 존재 확인 (모든 메서드에 대해)
    @Before(value = "allCommentServiceMethods() && methodsWithBoardId(boardId)", argNames = "boardId")
    public void checkBoardExists(Long boardId) {
        boardRepository.findById(boardId).orElseThrow(() -> new ResourceNotFoundException("게시판을 찾을 수 없습니다. ID: " + boardId));
    }

    // 게시글 존재 확인 (모든 메서드에 대해)
    @Before(value = "allCommentServiceMethods() && methodsWithArticleId(boardId, articleId)", argNames = "boardId,articleId")
    public void checkArticleExists(Long boardId, Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다. ID: " + articleId));

        if (article.getIsDeleted()) {
            throw new ResourceNotFoundException("게시글이 삭제되었습니다. ID: " + articleId);
        }
    }

    // 댓글 존재 확인 (모든 메서드에 대해)
    @Before(value = "allCommentServiceMethods() && methodsWithCommentId(boardId, articleId, commentId)", argNames = "boardId,articleId,commentId")
    public void checkCommentExists(Long boardId, Long articleId, Long commentId) {
        commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다. ID: " + commentId));
    }

    // 댓글 작성, 수정, 삭제 시 권한 확인
    @Before(value = "commentModificationMethods() && methodsWithCommentId(boardId, articleId, commentId)", argNames = "boardId,articleId,commentId")
    public void checkUserAuthorizationForModification(Long boardId, Long articleId, Long commentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Comment> comment = commentRepository.findById(commentId);

        if (author.isEmpty() || comment.isEmpty() || !comment.get().getAuthor().equals(author.get())) {
            throw new ForbiddenException("작성 권한이 없습니다.");
        }
    }
}
