package com.fast.rabbitmq.starter.producer;

import com.fast.rabbitmq.starter.constants.FastRabbitMqProducerTableFieldConstans;
import com.fast.rabbitmq.starter.constants.FastRabbitMqTableConstans;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * 消息发送者
 *
 * @Auther: liuxi
 * @Date: 2019/6/1 11:23
 * @Description:
 */
public class FastRabbitMQProducer {

    private RabbitTemplate rabbitTemplate;

    private MongoTemplate fastRabbitMQMongoTemplate;

    public FastRabbitMQProducer(RabbitTemplate rabbitTemplate, MongoTemplate fastRabbitMQMongoTemplate){

        //初始化异步接受消息确认
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback(){

            @Override
            public void confirm(CorrelationData correlationData, boolean b, String s) {

                Long currentTime = System.currentTimeMillis();

                String correlationDataId = correlationData.getId();

                if(b){
                    Query query = Query.query(Criteria.where(FastRabbitMqProducerTableFieldConstans.MESSAGE_ID).is(correlationDataId));

                    Update update = Update.update(FastRabbitMqProducerTableFieldConstans.UPDATE_TIME,currentTime)
                            .addToSet(FastRabbitMqProducerTableFieldConstans.CALL_BACK,1);

                    fastRabbitMQMongoTemplate.updateFirst(query,update,FastRabbitMqTableConstans.PRODUCER_MESSAGE_RECORD);
                }else {
                    Query query = Query.query(Criteria.where(FastRabbitMqProducerTableFieldConstans.MESSAGE_ID).is(correlationDataId));

                    Update update = Update.update(FastRabbitMqProducerTableFieldConstans.UPDATE_TIME,currentTime)
                            .addToSet(FastRabbitMqProducerTableFieldConstans.CALL_BACK,2);

                    fastRabbitMQMongoTemplate.updateFirst(query,update,FastRabbitMqTableConstans.PRODUCER_MESSAGE_RECORD);
                }
            }
        });
        this.rabbitTemplate = rabbitTemplate;
        this.fastRabbitMQMongoTemplate = fastRabbitMQMongoTemplate;
    }

    /**
     * 发送消息
     * @param serverName  服务名称
     * @param messageName 消息名称
     * @param exchange    消息交换机
     * @param routeKey    消息路由key
     * @param message     消息body
     */
    public void send(String serverName, String messageName,String exchange, String routeKey, String message){

        String messageId = DigestUtils.md5Hex(serverName + messageName + exchange + UUID.randomUUID().toString().toLowerCase().replaceAll("-","") + message);

        before(serverName, messageName, messageId, exchange, routeKey, message);

        MessagePostProcessor messagePostProcessor = buildMessagePostProcessor(messageId, serverName, messageName);

        CorrelationData correlationData = new CorrelationData(messageId);

        rabbitTemplate.convertAndSend(exchange, routeKey, message, messagePostProcessor, correlationData);

    }

    /**
     * 发送消息
     * @param serverName  服务名称
     * @param messageName 消息名称
     * @param exchange    消息交换机
     * @param routeKey    消息路由key
     * @param message     消息body
     * @param messageId   消息ID(可用于重新投递消息的情况)
     */
    public void send(String serverName, String messageName,String exchange, String routeKey, String message, String messageId){

        before(serverName, messageName, messageId, exchange, routeKey, message);

        MessagePostProcessor messagePostProcessor = buildMessagePostProcessor(messageId, serverName, messageName);

        CorrelationData correlationData = new CorrelationData(messageId);

        rabbitTemplate.convertAndSend(exchange, routeKey, message, messagePostProcessor, correlationData);

    }

    public void before(String serverName, String messageName, String messageId, String exchange, String routeKey, String message){

        Long currentTime = System.currentTimeMillis();

        Query query = Query.query(Criteria.where(FastRabbitMqProducerTableFieldConstans.SERVER_NAME).is(serverName)
                .and(FastRabbitMqProducerTableFieldConstans.MESSAGE_NAME).is(messageName)
                .and(FastRabbitMqProducerTableFieldConstans.MESSAGE_ID).is(messageId));

        Update update = Update.update(FastRabbitMqProducerTableFieldConstans.SERVER_NAME,serverName)
                .addToSet(FastRabbitMqProducerTableFieldConstans.MESSAGE_NAME, messageName)
                .addToSet(FastRabbitMqProducerTableFieldConstans.MESSAGE_ID, messageId)
                .addToSet(FastRabbitMqProducerTableFieldConstans.EXCHANGE, exchange)
                .addToSet(FastRabbitMqProducerTableFieldConstans.ROUTEKEY, routeKey)
                .addToSet(FastRabbitMqProducerTableFieldConstans.MESSAGE, message)
                .addToSet(FastRabbitMqProducerTableFieldConstans.CALL_BACK, 0)
                .inc(FastRabbitMqProducerTableFieldConstans.SEND_NUM,1)
                .addToSet(FastRabbitMqProducerTableFieldConstans.ADD_TIME, currentTime)
                .addToSet(FastRabbitMqProducerTableFieldConstans.UPDATE_TIME,currentTime);

        fastRabbitMQMongoTemplate.upsert(query, update, FastRabbitMqTableConstans.PRODUCER_MESSAGE_RECORD);
    }

    public MessagePostProcessor buildMessagePostProcessor(String messageId, String serverName, String messageName){

        MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {

            @Override
            public Message postProcessMessage(Message message) throws AmqpException {

                message.getMessageProperties().setCorrelationId(messageId);

                message.getMessageProperties().setHeader(FastRabbitMqProducerTableFieldConstans.SERVER_NAME,serverName);
                message.getMessageProperties().setHeader(FastRabbitMqProducerTableFieldConstans.MESSAGE_NAME,messageName);

                return message;
            }
        };

        return messagePostProcessor;

    }
}
