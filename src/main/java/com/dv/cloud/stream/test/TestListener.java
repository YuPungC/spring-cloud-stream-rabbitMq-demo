package com.dv.cloud.stream.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@EnableBinding(ConsumerQueue.class)
public class TestListener {

    /**
     * 消费演示，延迟消费发演示
     * @param entity
     */
    @StreamListener(ConsumerQueue.DELAY_CONSUMER)
    public void receiveDelay(Message<String> entity) {
        log.info("监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) +"]");
        Date date = new Date();
        System.out.print("订单超时取消 ："+date);
    }

    /**
     * 路由演示
     * @param entity
     */
    @StreamListener(value = ConsumerQueue.ROUTE_CONSUMER,condition = "headers['version']=='1.0'")
    public void receiveRoute1(Message<String> entity, @Header("version") String version) {
        log.info("1.0监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + ", " + version+ "]");
    }
    /**
     * 路由演示
     * @param entity
     */
    @StreamListener(value = ConsumerQueue.ROUTE_CONSUMER,condition = "headers['version']=='2.0'")
    public void receiveRoute2(Message<String> entity, @Header("version") String version) {
        log.info("2.0监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + ", " + version+ "]");
    }

    /**
     * 异常演示
     * @param entity
     */
    @StreamListener(value = ConsumerQueue.ROUTE_CONSUMER,condition = "headers['version']=='exceptionTest'")
    public void receiveException(Message<String> entity, @Header("version") String version) {
        log.info("测试模拟异常 监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + ", " + version+ "]");
        throw new RuntimeException("Message consumer failed!");
    }

    /**
     * 异常，自定义处理演示
     * @param entity
     */
    @StreamListener(value = ConsumerQueue.EXCEPTION_CONSUMER,condition = "headers['version']=='exceptionDealTest'")
    public void receiveExceptionDeal(Message<String> entity, @Header("version") String version) {
        log.info("自定义异常处理 监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + ", " + version+ "]");
        throw new RuntimeException("Message consumer failed!");
    }

    /**
     * 消息消费失败的降级处理逻辑
     *
     * @param message
     */
    @ServiceActivator(inputChannel = "demo.exception.topic.test-group.errors")
    public void error(Message<?> message) {
        log.info("消息消费失败的降级处理逻辑---");
    }

    /**
     * 异常死信队列演示
     * @param entity
     */
    @StreamListener(ConsumerQueue.DLQ_CONSUMER)
    public void receiveDlq(Message<String> entity) {
        int count = 1;

        log.info("死信 监听到消息[" + JsonUtils.writeValueAsString(entity.getPayload()) + "]");
        // 进入DLQ的逻辑
        if (count == 3) {
            count = 1;

            throw new AmqpRejectAndDontRequeueException("tried 3 times failed, send to dlq! 进入死信队列");
        } else {
            count ++;
            throw new RuntimeException("Message consumer failed!");
        }
    }


    /**
     * 再次消费死信队列的处理逻辑
     * @param failedMessage
     * concurrency # 消费端的监听个数(即@RabbitListener开启几个线程去处理数据。)
     */
    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(ConsumerQueue.DLQ)
                    , exchange = @Exchange(ConsumerQueue.DLX)
                    , key = ConsumerQueue.DLQ_QUEUE
            ),
            concurrency = "1-5"
    )
    public void handleDlq(Message failedMessage) throws InterruptedException {
        Thread.sleep(10);
        log.info("重新消费 死信队列. 完整消息: {};", failedMessage);

        log.info("body: {}", new String( (byte[])failedMessage.getPayload() ));
    }
}