package com.fast.rabbitmq.starter.consumer;

import com.fast.rabbitmq.starter.constants.FastRabbitMqConsumerTableFieldConstans;
import com.fast.rabbitmq.starter.constants.FastRabbitMqProducerTableFieldConstans;
import com.fast.rabbitmq.starter.constants.FastRabbitMqTableConstans;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @Auther: liuxi
 * @Date: 2019/6/3 15:48
 * @Description:
 */
public abstract class AbstractFastRabbitMQBaseContainer implements FastRabbitMQBaseContainer {

    public String queueName;

    @Autowired
    private MongoTemplate fastRabbitMQMongoTemplate;

    @Bean
    public SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory){
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
            container.setConnectionFactory(connectionFactory);
            container.setQueueNames(queueName);              // 监听的队列
            container.setAcknowledgeMode(AcknowledgeMode.AUTO);     // 根据情况确认消息
            container.setMessageListener(new MessageListener(){
                    @Override
                    public void onMessage(Message message) {
                        MessageProperties messageProperties = message.getMessageProperties();

                        String correlationId = messageProperties.getCorrelationId();

                        String messageBody = String.valueOf(message.getBody());

                        String cumsumerQueue = messageProperties.getConsumerQueue();

                        before(message);
                        try {
                            process(messageBody);

                            after(cumsumerQueue, correlationId,1);
                        }catch (Exception e){
                            after(cumsumerQueue, correlationId,2);
                            throw e;
                        }
                    }
            });
        return container;
    }

    @Override
    public void before(Message message) {

        Long currentTime = System.currentTimeMillis();

        MessageProperties messageProperties = message.getMessageProperties();

        String receiveExchange = messageProperties.getReceivedExchange();

        String receivedRoutingKey = messageProperties.getReceivedRoutingKey();

        String messageId = messageProperties.getCorrelationId();

        String consumerQueue = messageProperties.getConsumerQueue();

        Map<String,Object> headers = messageProperties.getHeaders();

        String serverName = String.valueOf(headers.get(FastRabbitMqProducerTableFieldConstans.SERVER_NAME));
        String messageName = String.valueOf(headers.get(FastRabbitMqProducerTableFieldConstans.MESSAGE_NAME));

        Query query = Query.query(Criteria.where(FastRabbitMqConsumerTableFieldConstans.CONSUMER_QUEUE).is(consumerQueue)
                .and(FastRabbitMqConsumerTableFieldConstans.MESSAGE_ID).is(messageId));

        Update update = Update.update(FastRabbitMqConsumerTableFieldConstans.SERVER_NAME,serverName)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.MESSAGE_NAME, messageName)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.MESSAGE_ID, messageId)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.EXCHANGE, receiveExchange)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.ROUTEKEY, receivedRoutingKey)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.CONSUMER_QUEUE, consumerQueue)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.MESSAGE, message)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.RESULT, 0)
                .inc(FastRabbitMqConsumerTableFieldConstans.CONSUMER_NUM,1)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.ADD_TIME, currentTime)
                .addToSet(FastRabbitMqConsumerTableFieldConstans.UPDATE_TIME,currentTime);

        fastRabbitMQMongoTemplate.upsert(query, update, FastRabbitMqTableConstans.PRODUCER_MESSAGE_RECORD);
    }

    @Override
    public void after(String consumerQueue, String messageId, int result) {

        Long currentTime = System.currentTimeMillis();

        Query query = Query.query(Criteria.where(String.valueOf(Criteria.where(FastRabbitMqConsumerTableFieldConstans.CONSUMER_QUEUE).is(consumerQueue)
                .and(FastRabbitMqConsumerTableFieldConstans.MESSAGE_ID).is(messageId))));

        Update update = Update.update(FastRabbitMqProducerTableFieldConstans.UPDATE_TIME,currentTime)
                .addToSet(FastRabbitMqProducerTableFieldConstans.CALL_BACK,result);

        fastRabbitMQMongoTemplate.updateFirst(query, update, FastRabbitMqTableConstans.PRODUCER_MESSAGE_RECORD);
    }
}
