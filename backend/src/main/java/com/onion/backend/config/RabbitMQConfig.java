package com.onion.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    /**
     * Dead Letter Queue 설정
     * @return
     */
    @Bean
    public Queue notificationMainQueue() {
        return QueueBuilder.durable("onion-notification")
                .withArgument("x-dead-letter-exchange", "onion-dlx")  // DLX로 설정
                .withArgument("x-dead-letter-routing-key", "onion-dlq")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("onion-dead-letter")
                .build();
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange("onion-dlx");
    }

    @Bean
    public Binding dlqBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("onion-dlq");
    }
    @Bean
    public Binding notificationQueueBinding() {
        return BindingBuilder
                .bind(notificationMainQueue())
                .to(fanoutExchange());
    }



    /**
     * 각각 이메일, SMS용 메시지를 처리할 큐 생성.
     * @return
     */
    @Bean
    public Queue emailQueue(){
        return new Queue("send_notification.email",true);
    }

    @Bean
    public Queue smsQueue(){
        return new Queue("send_notification.sms",true);
    }

    /**
     * fanoutExchange : 라우팅 키를 무시하고, 이 익스체인지에 연결된 모든 큐에 똑같이 메시지를 복사해서 보냄
     * @return
     */
    @Bean
    public FanoutExchange fanoutExchange(){
        return new FanoutExchange("send_notification_exchange");
    }

    /**
     * "send_notification.sms" 와 "send_notification.email" 큐를 FanoutExchange에 연결.
     * => 메시지 하나 보내면 둘 다 수신함.
     * @param smsQueue
     * @param fanoutExchange
     * @return
     */
    @Bean
    public Binding bindingSmsQueue(@Qualifier("smsQueue")Queue smsQueue, FanoutExchange fanoutExchange){
        return BindingBuilder.bind(smsQueue).to(fanoutExchange);
    }
    @Bean
    public Binding bindingEmailQueue(@Qualifier("emailQueue")Queue emailQueue, FanoutExchange fanoutExchange){
        return BindingBuilder.bind(emailQueue).to(fanoutExchange);
    }

    /**
     * retryQueue : 메시지 재전송을 위한 큐
     * @return
     */
    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable("onion-retry-queue")
                .withArgument("x-dead-letter-exchange", "send_notification_exchange")
                .withArgument("x-dead-letter-routing-key", "") // fanout 이라 routing key 무시됨
                .withArgument("x-message-ttl", 5000) // 5초 지연
                .build();
    }

    @Bean
    public Binding retryQueueBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(fanoutExchange());  // 원래 exchange에 연결
    }
    @Bean
    public MessageConverter jsonMessageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
