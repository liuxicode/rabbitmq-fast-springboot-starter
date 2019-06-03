package com.fast.rabbitmq.starter.constants;

import org.springframework.data.mongodb.core.query.Update;

/**
 * 消息生产者记录表字段名称
 *
 * @Auther: liuxi
 * @Date: 2019/6/3 09:52
 * @Description:
 */
public class FastRabbitMqProducerTableFieldConstans {

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
    //消息体
    public final static String MESSAGE = "message";
    //消息反馈结果(0:无反馈 1:消息到broker发送成功 2:消息发送到broker失败)
    public final static String CALL_BACK = "call_back";
    //消息发送次数
    public final static String SEND_NUM = "send_num";
    //消息第一次发送时间
    public final static String ADD_TIME = "add_time";
    //消息表更新时间
    public final static String UPDATE_TIME = "update_time";

}
