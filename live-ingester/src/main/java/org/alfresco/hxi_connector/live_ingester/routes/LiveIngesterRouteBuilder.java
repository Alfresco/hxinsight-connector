/*-
 * #%L
 * %%
 * Copyright (C) 2020 Alfresco Software Limited
 * %%
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 * #L%
 */
package org.alfresco.hxi_connector.live_ingester.routes;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.live_ingester.routes.config.ActiveMQProperties;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static org.apache.camel.LoggingLevel.DEBUG;

@Slf4j
@Component
public class LiveIngesterRouteBuilder extends RouteBuilder {

  private final ActiveMQProperties properties;

  public LiveIngesterRouteBuilder(
          CamelContext context,
          ActiveMQProperties activeMQProperties
  ) {
    super(context);
    this.properties = activeMQProperties;
  }

  public void configure() {
    System.out.println(properties);
    from(properties.getChannel())
            .transacted()
            .routeId("ingester-events-consumer")
            .log(DEBUG, "Received path event : ${header.JMSMessageID}")
            .end();
  }
}
