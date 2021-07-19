package com.dv.cloud.stream.test;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.binder.rabbit.properties.RabbitCommonProperties;
import org.springframework.messaging.SubscribableChannel;

/**
 * 消费者队列定义
 */
public interface ConsumerQueue {

    /**
     *  延迟演示队列
     */
    String DELAY_CONSUMER = "delay-consumer";

    /**
     *  路由演示队列
     */
    String ROUTE_CONSUMER = "route-consumer";

    /**
     *  异常演示队列
     */
    String EXCEPTION_CONSUMER = "exception-consumer";

    /**
     *  死信演示队列
     */
    String DLQ_CONSUMER = "dlq-consumer";
    String DLQ_QUEUE = "demo.dlq.topic.test-group";
    String DLQ = DLQ_QUEUE + ".dlq";
    /**
     * 死信队列交换机. 默认为: {@link RabbitCommonProperties#DEAD_LETTER_EXCHANGE}, 值为 "DLX".
     */
    String DLX = RabbitCommonProperties.DEAD_LETTER_EXCHANGE;

    @Input(DELAY_CONSUMER)
    SubscribableChannel delayConsumer();

    @Input(ROUTE_CONSUMER)
    SubscribableChannel routeConsumer();

    @Input(EXCEPTION_CONSUMER)
    SubscribableChannel exceConsumer();

    @Input(DLQ_CONSUMER)
    SubscribableChannel dlqConsumer();

//    @Input(DLQ_CONSUMER)
//    SubscribableChannel dlqDealConsumer();

}
