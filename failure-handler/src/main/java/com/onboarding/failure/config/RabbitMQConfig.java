package com.onboarding.failure.config;

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

    @Bean
    public TopicExchange onboardingExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue kycFailedQueue() {
        return QueueBuilder.durable("kyc.failed.queue").build();
    }

    @Bean
    public Queue identityFailedQueue() {
        return QueueBuilder.durable("identity.failed.queue").build();
    }

    @Bean
    public Queue provisioningFailedQueue() {
        return QueueBuilder.durable("provisioning.failed.queue").build();
    }

    @Bean
    public Queue notificationFailedQueue() {
        return QueueBuilder.durable("notification.failed.queue").build();
    }

    @Bean
    public Binding kycFailedBinding() {
        return BindingBuilder
                .bind(kycFailedQueue())
                .to(onboardingExchange())
                .with(EventRoutingKeys.KYC_FAILED);
    }

    @Bean
    public Binding identityFailedBinding() {
        return BindingBuilder
                .bind(identityFailedQueue())
                .to(onboardingExchange())
                .with(EventRoutingKeys.IDENTITY_FAILED);
    }

    @Bean
    public Binding provisioningFailedBinding() {
        return BindingBuilder
                .bind(provisioningFailedQueue())
                .to(onboardingExchange())
                .with(EventRoutingKeys.PROVISIONING_FAILED);
    }

    @Bean
    public Binding notificationFailedBinding() {
        return BindingBuilder
                .bind(notificationFailedQueue())
                .to(onboardingExchange())
                .with(EventRoutingKeys.NOTIFICATION_FAILED);
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
