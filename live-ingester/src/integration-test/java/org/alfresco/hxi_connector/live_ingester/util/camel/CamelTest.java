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

package org.alfresco.hxi_connector.live_ingester.util.camel;

import static org.apache.camel.Exchange.HTTP_RESPONSE_CODE;

import java.util.Map;

import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Component;

import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;

@Component
@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.FieldNamingConventions"})
public class CamelTest
{
    public final TestEventListener INGESTER_LISTENER;
    public final TestEventListener ATS_REQUEST_LISTENER;
    public final TestEventProducer BULK_INGESTER_LISTENER;

    public CamelTest(ProducerTemplate producerTemplate, IntegrationProperties integrationProperties)
    {

        INGESTER_LISTENER = TestEventListener.builder(
                producerTemplate.getCamelContext(),
                integrationProperties.hylandExperience().ingester().endpoint())
                .postProcessor(exchange -> exchange.getMessage().setHeaders(Map.of(HTTP_RESPONSE_CODE, 202)))
                .build();

        ATS_REQUEST_LISTENER = TestEventListener.builder(
                producerTemplate.getCamelContext(),
                integrationProperties.alfresco().transform().request().endpoint())
                .build();

        BULK_INGESTER_LISTENER = new TestEventProducer(
                producerTemplate,
                integrationProperties.alfresco().bulkIngester().endpoint());
    }

    public void reset()
    {
        INGESTER_LISTENER.reset();
        ATS_REQUEST_LISTENER.reset();
    }
}
