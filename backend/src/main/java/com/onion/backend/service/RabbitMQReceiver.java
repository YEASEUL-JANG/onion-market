package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onion.backend.dto.SendCommentNotificationDto;
import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.dto.WriteCommentDto;
import com.onion.backend.entity.Article;
import com.onion.backend.entity.Comment;
import com.onion.backend.repository.ArticleRepository;
import com.onion.backend.repository.CommentRepository;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RabbitMQReceiver {
    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserNotificationHistoryService userNotificationHistoryService;
    private final RabbitMQSender rabbitMQSender;
    private final RabbitTemplate rabbitTemplate;
    @Autowired
    public RabbitMQReceiver(CommentRepository commentRepository, ArticleRepository articleRepository, UserNotificationHistoryService userNotificationHistoryService, RabbitMQSender rabbitMQSender, RabbitTemplate rabbitTemplate) {
        this.commentRepository = commentRepository;
        this.articleRepository = articleRepository;
        this.userNotificationHistoryService = userNotificationHistoryService;
        this.rabbitMQSender = rabbitMQSender;
        this.rabbitTemplate = rabbitTemplate;
    }
    @RabbitListener(queues = "send_notification.email")
    public void emailReceive(String message){
        log.info("Received Message(email) : "+message);
    }

    @RabbitListener(queues = "send_notification.sms")
    public void smsReceive(String message){
        log.info("Received Message(sms) : "+message);
    }
    @RabbitListener(queues = "onion-notification")
    public void receiveMessage(String message) throws JsonProcessingException {
        if (message.contains(WriteCommentDto.class.getSimpleName())) {
            this.sendCommentNotification(message);
            return;
        }
        if (message.contains(WriteArticleDto.class.getSimpleName())) {
            this.sendArticleNotification(message);
            return;
        }
        log.info("Received Message(onion) : "+message);
    }
    @RabbitListener(queues = "onion-dead-letter", ackMode = "MANUAL")
    public void handleDeadLetter(Message message, Channel channel) throws IOException {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);
        Integer retryCount = (Integer) message.getMessageProperties().getHeaders().getOrDefault("x-retry-count", 0);

        if (retryCount < 3) {
            log.info("[DEAD LETTER] 재시도 지연 큐로 전송... count = " + (retryCount + 1));
            MessageProperties props = message.getMessageProperties();
            props.getHeaders().put("x-retry-count", retryCount + 1);

            Message retryMessage = new Message(message.getBody(), props);

            // 지연 큐로 전송
            rabbitTemplate.send("", "onion-retry-queue", retryMessage);
        } else {
            log.error("[DEAD LETTER] 최대 재시도 초과. 메시지 폐기 or 로깅: " + body);
        }
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    private void sendArticleNotification(String message) {
        //데이터 역직렬화
        message = message.replace("WriteArticle(", "").replace(")", "");
        String[] parts = message.split(", ");
        String type = null;
        Long articleId = null;
        Long userId = null;
        for (String part : parts) {
            String[] keyValue = part.split("=");
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            if (key.equals("type")) {
                type = value;
            } else if (key.equals("articleId")) {
                articleId = Long.parseLong(value);
            }else if (key.equals("userId")) {
                userId = Long.parseLong(value);
            }
        }
        Optional<Article> article = articleRepository.findById(articleId);
        if(article.isPresent()){
            userNotificationHistoryService.insertArticleNotification(article.get(),userId);
        }
    }

    private void sendCommentNotification(String message) throws JsonProcessingException {
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
                userNotificationHistoryService.insertCommentNotification(comment,userId);
            }
    }
}
