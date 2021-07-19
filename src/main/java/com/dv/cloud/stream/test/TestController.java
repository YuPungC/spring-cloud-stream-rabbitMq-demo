package com.dv.cloud.stream.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
@EnableBinding(ProducerQueue.class)
public class TestController {

    @Autowired
    private ProducerQueue producerQueue;

    /**
     * 发送消息
     * @param message
     * @return
     */
    @GetMapping("/sendMessage")
    public String messageWithMQ(@RequestParam String message) {
        producerQueue.delayProducer().send(MessageBuilder.withPayload(message).build());
        return "ok";
    }

    /**
     * 发送延迟消息
     * @param message
     * @param delay
     * @return
     */
    @GetMapping("/delay/sendMessage")
    public String sendDelay(@RequestParam String message,@RequestParam Long delay) {
        Message<String> messageVo = MessageBuilder
                .withPayload(message).setHeader("x-delay", delay).build();
        log.info("delay-message"+ messageVo);
        producerQueue.delayProducer().send(messageVo);
        Date date = new Date();
        System.out.print("订单创建成功"+date);
        return "ok";
    }

    /**
     * 发送路由
     * @param message
     * @param version
     * @return
     */
    @GetMapping("/route/sendMessage")
    public String sendRoute(@RequestParam String message,@RequestParam String version) {
        producerQueue.routeProducer().send(MessageBuilder.withPayload(message).setHeader("version", version).build());
        return "ok";
    }

    /**
     * 发送错误信息路由
     * @param message
     * @param version
     * @return
     */
    @GetMapping("/exception/sendMessage")
    public String sendExce(@RequestParam String message,@RequestParam String version) {
        producerQueue.excepProducer().send(MessageBuilder.withPayload(message).setHeader("version", version).build());
        return "ok";
    }

    /**
     * 发送死信
     * @param message
     * @return
     */
    @GetMapping("/dlq/sendMessage")
    public String sendDeath(@RequestParam String message) {
        producerQueue.dlqProducer().send(MessageBuilder.withPayload(message).build());
        return "ok";
    }
}
