package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.dto.ArticleNotificationDto;
import com.onion.backend.dto.SendCommentNotificationDto;
import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Comment;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.CommentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class RabbitMQReceiver {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final RabbitMQSender rabbitMQSender;
    @Autowired
    public RabbitMQReceiver(CommentRepository commentRepository, ArticleRepository articleRepository, RabbitMQSender rabbitMQSender) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.rabbitMQSender = rabbitMQSender;
    }

    @RabbitListener(queues = "onion-notification")
    public void receiveMessage(String message){
        if(message.contains(WriteCommentDto.class.getSimpleName())){
            this.sendCommentNotification(message);
            return;
        }

        System.out.println("Received Message : "+message);
    }

    private void sendCommentNotification(String message) {
            //데이터 역직렬화
            message = message.replace("WriteComment(", "").replace(")", "");
            String[] parts = message.split(", ");
            String type = null;
            Long commentId = null;
            for (String part : parts) {
                String[] keyValue = part.split("=");
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                if (key.equals("type")) {
                    type = value;
                } else if (key.equals("commentId")) {
                    commentId = Long.parseLong(value);
                }
            }
            WriteCommentDto writeCommentDto = new WriteCommentDto();
            writeCommentDto.setType(type);
            writeCommentDto.setCommentId(commentId);

            //알림전송
            SendCommentNotificationDto sendCommentNotificationDto = new SendCommentNotificationDto();
            sendCommentNotificationDto.setCommentId(writeCommentDto.getCommentId());
            Optional<Comment> commentOptional = commentRepository.findById(writeCommentDto.getCommentId());
            if(commentOptional.isEmpty()){return;}

            Comment comment = commentOptional.get();
            HashSet<Long> userSet = new HashSet<>();
            //댓글 작성한 본인
            userSet.add(comment.getAuthor().getId());
            //글 작성자
            userSet.add(comment.getArticle().getAuthor().getId());
            //글에 댓글을 달은 모든 댓작성자들에게 알림을 보냄
        List<Comment> comments = commentRepository.findByArticleId(comment.getArticle().getId());
        for(Comment article_comment: comments){
                userSet.add(article_comment.getAuthor().getId());
            }
            for(Long userId: userSet){
                sendCommentNotificationDto.setUserId(userId);
                rabbitMQSender.sendMessage(sendCommentNotificationDto);
                }
    }
}