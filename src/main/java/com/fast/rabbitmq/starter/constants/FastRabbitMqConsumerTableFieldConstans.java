package com.fast.rabbitmq.starter.constants;

/**
 * 消息消费者记录表字段名称
 *
 * @Auther: liuxi
 * @Date: 2019/6/3 09:52
 * @Description:
 */
public class FastRabbitMqConsumerTableFieldConstans {

    //发送消息服务名称
    public final static String SERVER_NAME = "server_name";
    //发送消息业务名称
    public final static String MESSAGE_NAME = "message_name";
    //消息的唯一ID
    public final static String MESSAGE_ID = "message_id";
    //消息交换机
    public final static String EXCHANGE = "exchange";
    //消息路由 routeKey
    public final static String ROUTEKEY = "route_key";
    //消费队列
    public final static String CONSUMER_QUEUE = "consumer_queue";
    //消息体
    public final static String MESSAGE = "message";
    //消息消费结果(0:收到消息,未确认 1:收到消息，确认消费成功 2:收到消息，确认消费失败)
    public final static String RESULT = "result";
    //消息消费次数
    public final static String CONSUMER_NUM = "consumer_num";
    //消息第一次发送时间
    public final static String ADD_TIME = "add_time";
    //消息表更新时间
    public final static String UPDATE_TIME = "update_time";

}
