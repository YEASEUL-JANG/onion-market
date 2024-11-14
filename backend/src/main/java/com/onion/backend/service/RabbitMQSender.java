package com.onion.backend.service;

import com.onion.backend.dto.WriteArticleDto;
import com.onion.backend.dto.SendCommentNotificationDto;
import com.onion.backend.dto.WriteCommentDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    public void sendMessage(WriteArticleDto notiDto){
        rabbitTemplate.convertAndSend("onion-notification",notiDto.toString());
    }

    public void sendMessage(WriteCommentDto commentDto){
        rabbitTemplate.convertAndSend("onion-notification",commentDto.toString());
    }

    public void sendMessage(SendCommentNotificationDto sendDto){
        rabbitTemplate.convertAndSend("onion-notification",sendDto.toString());
    }

}
