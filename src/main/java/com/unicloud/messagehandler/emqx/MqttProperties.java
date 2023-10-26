package com.unicloud.messagehandler.emqx;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("mqtt")
@Data
public class MqttProperties {
    @JsonProperty("host")
    private String host;
    @JsonProperty("client-id")
    private String clientId;
    @JsonProperty("sub-topic")
    private String subTopic;
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
}
