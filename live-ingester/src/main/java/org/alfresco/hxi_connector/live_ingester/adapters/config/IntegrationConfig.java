package org.alfresco.hxi_connector.live_ingester.adapters.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IntegrationConfig.Properties.class)
public class IntegrationConfig
{

    @ConfigurationProperties(prefix = "alfresco.integration")
    public record Properties(Storage storage)
    {}

    public record Storage(String endpoint, Retry retry)
    {}

    public record Retry(int attempts, int initialDelay, double delayMultiplier, List<Class<? extends Throwable>> reasons)
    {}
}
