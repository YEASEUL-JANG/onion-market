package com.onion.backend.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQReceiver {
    @RabbitListener(queues = "onion-notification")
    public void receiveMessage(String message){

        System.out.println("Received Message : "+message);
    }
}
