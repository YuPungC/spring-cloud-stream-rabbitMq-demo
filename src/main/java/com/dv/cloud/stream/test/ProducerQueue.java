package com.dv.cloud.stream.test;


import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 *
 */
public interface ProducerQueue {

    /**
     *  延迟演示队列
     */
    String DELAY_PRODUCER= "delay-producer";

    /**
     *  路由演示队列
     */
    String ROUTE_PRODUCER = "route-producer";

    /**
     *  异常演示队列
     */
    String EXCEPTION_PRODUCER = "exception-producer";

    /**
     *  死信演示队列
     */
    String DLQ_PRODUCER = "dlq-producer";

    @Output(DELAY_PRODUCER)
    MessageChannel delayProducer();

    @Output(ROUTE_PRODUCER)
    MessageChannel routeProducer();

    @Output(EXCEPTION_PRODUCER)
    MessageChannel excepProducer();

    @Output(DLQ_PRODUCER)
    MessageChannel dlqProducer();

}
