package com.onion.backend.service;

import com.onion.backend.dto.ArticleNotificationDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQSender {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    public void sendMessage(ArticleNotificationDto notiDto){
        rabbitTemplate.convertAndSend("onion-notification",notiDto.toString());
    }

}
