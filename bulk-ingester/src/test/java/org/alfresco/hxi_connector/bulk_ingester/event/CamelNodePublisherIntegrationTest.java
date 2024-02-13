/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2024 Alfresco Software Limited
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

package org.alfresco.hxi_connector.bulk_ingester.event;

import static org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.bulk_ingester.processor.mapper.AlfrescoNodeMapper.TYPE_PROPERTY;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import org.alfresco.hxi_connector.bulk_ingester.processor.model.ContentInfo;
import org.alfresco.hxi_connector.bulk_ingester.processor.model.Node;
import org.alfresco.hxi_connector.bulk_ingester.util.ActiveMqIntegrationTestBase;
import org.alfresco.hxi_connector.bulk_ingester.util.TestCamelConsumer;

@SpringBootTest(
        classes = {CamelNodePublisher.class, TestCamelConsumer.class},
        properties = "logging.level.org.alfresco=DEBUG")
@EnableAutoConfiguration
@ImportAutoConfiguration(CamelAutoConfiguration.class)
@EnableConfigurationProperties(NodePublisherConfig.class)
public class CamelNodePublisherIntegrationTest extends ActiveMqIntegrationTestBase
{
    @Autowired
    CamelNodePublisher nodePublisher;

    @Autowired
    TestCamelConsumer testCamelConsumer;

    @Test
    void shouldPublishNode()
    {
        // given
        Node node = new Node(
                "66326096-3bd6-412e-abbe-a07fbabf2fcc",
                "admin",
                "admin",
                Set.of("ownable", "taggable"),
                new ContentInfo(1000, "UTF-8", "application/pdf"),
                Map.of(TYPE_PROPERTY, "file",
                        "cm:categories", (Serializable) List.of("33cd7d4c-ba12-4006-9642-f9fb2d3bd406"),
                        CREATED_AT_PROPERTY, 2000));

        // when
        nodePublisher.publish(node);

        // then
        String expectedEvent = """
                {
                  "nodeId" : "66326096-3bd6-412e-abbe-a07fbabf2fcc",
                  "creatorId" : "admin",
                  "modifierId" : "admin",
                  "aspectNames" : [ "ownable", "taggable" ],
                  "contentInfo" : {
                    "contentSize" : 1000,
                    "encoding" : "UTF-8",
                    "mimetype" : "application/pdf"
                  },
                  "properties" : {
                    "type" : "file",
                    "cm:categories" : [ "33cd7d4c-ba12-4006-9642-f9fb2d3bd406" ],
                    "createdAt" : 2000
                  }
                }""";

        testCamelConsumer.assertNMessagesReceived(1);
        testCamelConsumer.assertMessageReceived(expectedEvent);
    }

    @AfterEach
    void tearDown()
    {
        testCamelConsumer.cleanUp();
    }
}
