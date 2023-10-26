package com.unicloud.messagehandler.emqx;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.stream.CharacterStreamReadingMessageSource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.MessageHandler;

@Slf4j
@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class MqttConfig {
    private final MqttProperties mqttProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttProperties.getHost()});
        options.setUserName(mqttProperties.getUsername());
        options.setPassword(mqttProperties.getPassword().toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageProducerSupport mqttInbound() {
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(MqttAsyncClient.generateClientId(), mqttClientFactory(), mqttProperties.getSubTopic());
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        return adapter;
    }

    @Bean
    @SneakyThrows
    public IntegrationFlow mqttInboundFlow() {
        return IntegrationFlow.from(mqttInbound())
            .transform(p -> p + " received from MQTT")
            .handle(m -> {
                log.info("[MQTT][SUB] topic: {}", m.getHeaders().get("mqtt_receivedTopic"));
                log.info("[MQTT][SUB] value: {}", m.getPayload());
                String kafkaTopic = String.valueOf(m.getHeaders().get("mqtt_receivedTopic")).replace("/", ".");
                Object kafkaPayload = m.getPayload();
                log.info("[KAFKA][PUB] topic: {}, ", kafkaTopic);
                log.info("[KAFKA][PUB] payload: {}, ", kafkaPayload);
                kafkaTemplate.send(kafkaTopic, kafkaPayload);
            })
            .get();
    }

    @Bean
    public IntegrationFlow mqttOutFlow() {
        return IntegrationFlow.from(CharacterStreamReadingMessageSource.stdin(),
                e -> e.poller(Pollers.fixedDelay(1000)))
            .handle(mqttOutbound())
            .get();
    }

    @Bean
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler(MqttAsyncClient.generateClientId(), mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("defaultTopic");
        return messageHandler;
    }
}
