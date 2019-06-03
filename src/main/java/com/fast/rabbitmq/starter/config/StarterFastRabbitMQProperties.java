package com.fast.rabbitmq.starter.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Auther: liuxi
 * @Date: 2019/6/1 10:57
 * @Description:
 */
@ConfigurationProperties("easycode.fast.rabbitmq")
public class StarterFastRabbitMQProperties {

    private boolean enable;
}
