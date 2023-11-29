package org.alfresco.hxi_connector.live_ingester.routes.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@ToString
@Validated
@ConfigurationProperties(prefix = "alfresco.ingester.messaging")
public class ActiveMQProperties {

    @NotBlank
    private String channel;

}
