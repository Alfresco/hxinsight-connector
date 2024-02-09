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

package org.alfresco.hxi_connector.bulk_ingester.processor;

import lombok.extern.slf4j.Slf4j;
import org.alfresco.hxi_connector.bulk_ingester.BulkIngesterApplication;
import org.alfresco.hxi_connector.bulk_ingester.event.NodePublisher;
import org.alfresco.hxi_connector.bulk_ingester.spring.ApplicationManager;
import org.alfresco.hxi_connector.bulk_ingester.util.IntegrationTest;
import org.alfresco.hxi_connector.bulk_ingester.util.integration.PostgresIntegrationTestBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.mockito.Mockito.mock;

@IntegrationTest
@EnableAutoConfiguration
@SpringBootTest(properties = "logging.level.org.alfresco=DEBUG", classes = BulkIngesterApplication.class)
@Import(BulkIngestionProcessorIntegrationTest.MockEventPublisherConfiguration.class)
class BulkIngestionProcessorIntegrationTest extends PostgresIntegrationTestBase
{

    @Autowired
    private BulkIngestionProcessor bulkIngestionProcessor;

    @Test
    void name()
    {
        bulkIngestionProcessor.process();
    }

    @Slf4j
    @TestConfiguration
    public static class MockEventPublisherConfiguration
    {
        @Bean
        @Primary
        public NodePublisher nodePublisher()
        {
            return node -> log.info(node.toString());
        }

        @Bean
        @Primary
        public ApplicationManager applicationManager() {
            return mock();
        }
    }
}
