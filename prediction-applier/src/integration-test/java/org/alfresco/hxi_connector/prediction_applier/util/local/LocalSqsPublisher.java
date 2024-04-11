/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.hxi_connector.prediction_applier.util.local;

import lombok.SneakyThrows;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.common.model.prediction.Prediction;

@Component
@Profile("test")
public class LocalSqsPublisher extends RouteBuilder
{
    private static final String LOCAL_ENDPOINT = "direct:local-aws-sqs";
    private static final String ROUTE_ID = "local-sqs-publisher";
    private static final String ENDPOINT_HEADER = "endpoint";

    @Autowired
    private CamelContext camelContext;

    @Override
    public void configure()
    {
        from(LOCAL_ENDPOINT)
                .routeId(ROUTE_ID)
                .marshal()
                .json()
                .toD("${headers." + ENDPOINT_HEADER + "}")
                .end();
    }

    @SneakyThrows
    public void publish(String endpoint, Prediction prediction)
    {
        camelContext.createFluentProducerTemplate()
                .withHeader(ENDPOINT_HEADER, endpoint)
                .withBody(prediction)
                .to(LOCAL_ENDPOINT)
                .send();
    }
}
