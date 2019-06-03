package com.fast.rabbitmq.starter.autoconfig;

import com.fast.rabbitmq.starter.config.StarterFastRabbitMQProperties;
import com.fast.rabbitmq.starter.producer.FastRabbitMQProducer;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

/**
 * 注入 message producer 和 consumer
 *
 * @Auther: liuxi
 * @Date: 2019/6/1 11:22
 * @Description:
 */
@Configuration
@ConditionalOnClass(FastRabbitMQProducer.class)
@EnableConfigurationProperties(StarterFastRabbitMQProperties.class)
public class StarterFastRabbitMQAutoConfigure {

    @Bean
    public RabbitTemplate rabbitTemplate(){

        return new RabbitTemplate();
    }

    @Bean(name="fastRabbitMQMongoProperties")
    @Primary
    @ConfigurationProperties(prefix="easycode.fast.rabbitmq.mongodb")
    public MongoProperties fastRabbitMQMongoProperties() {
        return new MongoProperties();
    }

    @Bean(name="fastRabbitMQMongoFactory")
    @Primary
    public MongoDbFactory fastRabbitMQMongoFactory(MongoProperties mongoProperties) throws Exception {

        ServerAddress serverAdress = new ServerAddress(mongoProperties.getUri());

        return new SimpleMongoDbFactory(new MongoClient(serverAdress), mongoProperties.getDatabase());

    }

    @Bean(name="fastRabbitMQMongoTemplate")
    @Primary
    public MongoTemplate fastRabbitMQMongoTemplate(MongoDbFactory fastRabbitMQMongoFactory) throws Exception {
        return new MongoTemplate(fastRabbitMQMongoFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "easycode.fast.rabbitmq", value = "enalbe", havingValue = "true")
    public FastRabbitMQProducer fastRabbitMQProducer(RabbitTemplate rabbitTemplate, MongoTemplate fastRabbitMQMongoTemplate){

        return new FastRabbitMQProducer(rabbitTemplate,fastRabbitMQMongoTemplate);
    }
}
