package com.unicloud.messagehandler.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
//    private final KafkaTemplate<String, String> kafkaTemplate;
//
//    @PostMapping("/send")
//    public void send() {
//        String message = "hello";
//        String topic = "com.message.push";
//        kafkaTemplate.send(topic, message);
//    }
}
