package com.unicloud.messagehandler.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerListener {

    private final TaskExecutor taskExecutor;
    private final IntegrationFlow mqttOutFlow;

    @KafkaListener(
        id = "${spring.kafka.consumer.id}", groupId = "${spring.kafka.consumer.group-id}",
        topicPattern = "${spring.kafka.topics.sub-message}", containerFactory = "customKafkaListenerContainerFactory"
    )
    public void listener(@Payload ConsumerRecord<Object, Object> messages) {
        taskExecutor.execute(() -> {
            this.handleMessage(messages);
        });
    }

    @SneakyThrows
    private void handleMessage(ConsumerRecord<Object, Object> message) {
        String topic = message.topic().replace(".", "/").replace("server", "client");
        log.info("[MQTT][PUB] topic: {}", topic);
        log.info("[MQTT][PUB] value: {}", message.value());
        mqttOutFlow.getInputChannel().send(new GenericMessage<>(message.value()));
    }
}
