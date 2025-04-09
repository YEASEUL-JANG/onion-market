package com.onion.backend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.dto.SendCommentNotificationDto;
import com.onion.backend.dto.WriteCommentDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public RabbitMQSender(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }
    public void sendMessage(WriteArticleDto notiDto) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(notiDto);
        rabbitTemplate.convertAndSend("onion-notification",json);
    }

    public void sendMessage(WriteCommentDto commentDto) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(commentDto);
        rabbitTemplate.convertAndSend("onion-notification",json);
    }

    public void sendMessage(SendCommentNotificationDto sendDto) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(sendDto);
        rabbitTemplate.convertAndSend("send_notification_exchange","",json);
    }

}
