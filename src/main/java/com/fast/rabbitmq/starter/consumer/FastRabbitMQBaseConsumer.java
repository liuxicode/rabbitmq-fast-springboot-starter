package com.fast.rabbitmq.starter.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

/**
 * 使用 @RabbitListener 需继承该接口
 * @Auther: liuxi
 * @Date: 2019/6/3 13:50
 * @Description:
 */
public interface FastRabbitMQBaseConsumer {

    public void before(String messageId);

    public void after(String consumerQueue, String messageId, int result);

    @RabbitHandler
    public default void process(String message){
        return;
    }
}
