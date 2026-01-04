package com.onboarding.kyc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.onboarding.events.EventRoutingKeys;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "onboarding.exchange";
    public static final String QUEUE_NAME = "kyc.queue";

    @Bean
    public TopicExchange onboardingExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange("onboarding.dlx", true, false);
    }

    @Bean
    public Queue kycQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "onboarding.dlx")
                .withArgument("x-dead-letter-routing-key", "kyc.dlq")
                .build();
    }

    @Bean
    public Queue kycDlq() {
        return QueueBuilder.durable("kyc.dlq").build();
    }

    @Bean
    public Binding kycDlqBinding() {
        return BindingBuilder
                .bind(kycDlq())
                .to(dlxExchange())
                .with("kyc.dlq");
    }

    @Bean
    public Binding kycBinding() {
        return BindingBuilder
                .bind(kycQueue())
                .to(onboardingExchange())
                .with(EventRoutingKeys.ONBOARDING_REQUESTED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
