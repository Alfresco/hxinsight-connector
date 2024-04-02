/// *
// * #%L
// * Alfresco HX Insight Connector
// * %%
// * Copyright (C) 2023 - 2024 Alfresco Software Limited
// * %%
// * This file is part of the Alfresco software.
// * If the software was purchased under a paid Alfresco license, the terms of
// * the paid license agreement will prevail. Otherwise, the software is
// * provided under the following open source license terms:
// *
// * Alfresco is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * Alfresco is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
// * #L%
// */
//
// package org.alfresco.hxi_connector.live_ingester.util.event.external;
//
// import lombok.SneakyThrows;
// import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
// import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.model.ClientData;
// import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.request.model.ATSTransformRequest;
// import org.alfresco.hxi_connector.live_ingester.adapters.messaging.transform.response.TransformResponse;
// import org.apache.camel.CamelContext;
// import org.apache.camel.Exchange;
// import org.apache.camel.Message;
// import org.apache.camel.builder.RouteBuilder;
// import org.apache.camel.model.dataformat.JsonLibrary;
// import org.springframework.stereotype.Component;
// import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
//
// @Component
// public class ATSDummyService
// {
//
// @SneakyThrows
// public ATSDummyService(CamelContext camelContext, IntegrationProperties integrationProperties)
// {
// camelContext.addRoutes(new RouteBuilder()
// {
// @Override
// public void configure()
// {
// from(integrationProperties.alfresco().transform().request().endpoint())
// .unmarshal()
// .json(JsonLibrary.Jackson, ATSTransformRequest.class)
// .process(e -> toATSResponse(e))
// .to(integrationProperties.alfresco().transform().response().endpoint());
// }
// });
// }
//
// @SneakyThrows
// private void toATSResponse(Exchange exchange)
// {
// ATSTransformRequest request = exchange.getIn().getBody(ATSTransformRequest.class);
//
// TransformResponse transformResponse = new TransformResponse(
// "123",
// new ClientData("aaa", "bbb", 0),
// 200,
// null
// );
//
// exchange.getIn().setBody(new ObjectMapper().writeValueAsString(transformResponse));
// }
// }
