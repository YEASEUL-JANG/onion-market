package com.onion.backend.service;

import com.onion.backend.dto.CommentReqDto;
import com.onion.backend.dto.CommentResDto;
import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Comment;
import com.onion.backend.entity.User;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.CommentRepository;
import com.onion.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Service
public class CommentService {
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final Integer pageSize = 10;
    private final RabbitMQSender rabbitMQSender;


    @Autowired
    public CommentService(ArticleRepository articleRepository, CommentRepository commentRepository, UserRepository userRepository, RabbitMQSender rabbitMQSender) {
        this.articleRepository = articleRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.rabbitMQSender = rabbitMQSender;
    }

    /**
     * 댓글 작성
     *
     * @param commentReqDto
     * @param boardId
     * @param articleId
     * @return
     */

    @Transactional
    public CommentResDto writeComment(@RequestBody CommentReqDto commentReqDto, Long boardId, Long articleId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Optional<User> author = userRepository.findByUsername(userDetails.getUsername());
        Optional<Article> article = articleRepository.findById(articleId);

        Comment comment = Comment.builder()
                .content(commentReqDto.getContent())
                .author(author.get())
                .authorName(author.get().getUsername())
                .article(article.get())
                .build();

        Comment savedComment = commentRepository.save(comment);
        //rabbitmq(댓글작성 알림 발송)
        WriteCommentDto writeCommentDto = new WriteCommentDto();
        writeCommentDto.setCommentId(savedComment.getId());
        rabbitMQSender.sendMessage(writeCommentDto);


        return CommentResDto.builder()
                .id(savedComment.getId())
                .content(savedComment.getContent())
                .authorName(savedComment.getAuthorName()).build();
    }

    /**
     * 댓글 수정
     * @param articleId
     * @param commentId
     * @param dto
     * @return
     */
    @Transactional
    public CommentResDto editComment(Long articleId, Long commentId, CommentReqDto dto) {
        Optional<Comment> comment = commentRepository.findById(articleId);
        // 엔티티 필드 변경
        Comment existingComment = comment.get();  // Optional에서 엔티티 꺼내기
        existingComment.setContent(dto.getContent());

        //* 변경감지로 DB데이터 업데이트
        return CommentResDto.builder()
                .id(existingComment.getId())
                .content(existingComment.getContent())
                .authorName(existingComment.getAuthorName()).build();
    }

    /**
     * 댓글 삭제
     * @param articleId
     * @param commentId
     */
    @Transactional
    public void deleteComment(Long boardId, Long articleId, Long commentId) {
        Optional<Comment> comment = commentRepository.findById(commentId);
        comment.get().setIsDeleted(true);
    }


}
