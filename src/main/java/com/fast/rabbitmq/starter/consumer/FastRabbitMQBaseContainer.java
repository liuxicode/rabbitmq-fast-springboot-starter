package com.fast.rabbitmq.starter.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.ImmediateAcknowledgeAmqpException;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;

/**
 * 使用 @RabbitListener 需继承该接口
 * @Auther: liuxi
 * @Date: 2019/6/3 13:50
 * @Description:
 */
public interface FastRabbitMQBaseContainer {

    public void before(Message message);

    public void after(String messageId, int result);

    public void processMessage(String message);

    public void process(String message);

    void after(String consumerQueue, String messageId, int result);
}
