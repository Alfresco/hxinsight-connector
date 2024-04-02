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
package org.alfresco.hxi_connector.live_ingester.domain.usecase.bulk_ingester;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.alfresco.hxi_connector.live_ingester.adapters.auth.AuthenticationService;
import org.alfresco.hxi_connector.live_ingester.adapters.config.IntegrationProperties;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.BulkIngester;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Ingester;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Repository;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Retry;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Storage;
import org.alfresco.hxi_connector.live_ingester.adapters.config.properties.Transform;
import org.alfresco.hxi_connector.live_ingester.domain.ports.uuid.UUIDProvider;
import org.alfresco.hxi_connector.live_ingester.util.event.CamelTest;

@SpringBootTest(
        properties = {"logging.level.org.alfresco=DEBUG"})
@EnableAutoConfiguration
@SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
public class BulkIngesterEventIntegrationTest
{
    @MockBean
    private UUIDProvider uuidProvider;

    @Autowired
    private IntegrationProperties integrationProperties;

    @Autowired
    private CamelTest camelTest;

    private final String TEST_UUID = "1bde77d8-39c0-4c5d-81c5-7593b3c8e087";

    @BeforeEach
    void setUp()
    {
        camelTest.reset();
        when(uuidProvider.random()).thenReturn(TEST_UUID);
    }

    @Test
    void shouldIngestOnlyMetadataIfThereIsNoContent()
    {
        // given
        String bulkIngesterEvent = """
                {
                  "nodeId": "5018ff83-ec45-4a11-95c4-681761752aa7",
                  "contentInfo": null,
                  "properties": {
                    "cm:name": "Mexican Spanish",
                    "type": "cm:category",
                    "createdAt": 1707153552,
                    "createdBy": "System",
                    "modifiedBy": "admin",
                    "aspectsNames": [
                      "cm:auditable"
                    ]
                  }
                }""";

        // when
        camelTest.BULK_INGESTER_LISTENER.receivesMessage(bulkIngesterEvent);

        // then
        String expectedIngestEvent = """
                [
                  {
                    "objectId" : "5018ff83-ec45-4a11-95c4-681761752aa7",
                    "eventType" : "create",
                    "properties" : {
                      "type": {"value": "cm:category"},
                      "createdAt": {"value": 1707153552},
                      "createdBy": {"value": "System"},
                      "modifiedBy": {"value": "admin"},
                      "aspectsNames": {"value": ["cm:auditable"]},
                      "cm:name": {"value": "Mexican Spanish"}
                    }
                  }
                ]""";

        camelTest.INGESTER_LISTENER.expectExactlyOneMessageReceived(expectedIngestEvent);
    }

    @Test
    void shouldIngestMetadataAndContent()
    {
        // given
        String bulkIngesterEvent = """
                {
                  "nodeId": "37be157c-741c-4e51-b781-20d36e4e335a",
                  "contentInfo": {
                    "contentSize": 330,
                    "encoding": "ISO-8859-1",
                    "mimetype": "text/xml"
                  },
                  "properties": {
                    "cm:name": "dashboard.xml",
                    "cm:isContentIndexed": true,
                    "cm:isIndexed": false,
                    "createdAt": 1308061016,
                    "type": "cm:content",
                    "createdBy": "admin",
                    "modifiedBy": "hr_user",
                    "aspectsNames": [
                      "cm:indexControl",
                      "cm:auditable"
                    ]
                  }
                }""";

        // when
        camelTest.BULK_INGESTER_LISTENER.receivesMessage(bulkIngesterEvent);

        // then
        String expectedIngestEvent = """
                [
                  {
                    "objectId" : "37be157c-741c-4e51-b781-20d36e4e335a",
                    "eventType" : "create",
                    "properties" : {
                      "type": {"value": "cm:content"},
                      "createdBy": {"value": "admin"},
                      "modifiedBy": {"value": "hr_user"},
                      "aspectsNames": {"value": ["cm:indexControl", "cm:auditable"]},
                      "createdAt": {"value": 1308061016},
                      "cm:name": {"value": "dashboard.xml"},
                      "cm:isContentIndexed": {"value": true},
                      "cm:isIndexed": {"value": false},
                      "cm:content": {
                        "file": {
                          "content-metadata": {
                            "name": "dashboard.xml",
                            "size": 330,
                            "content-type": "text/xml"
                          }
                        }
                      }
                    }
                  }
                ]""";
        camelTest.INGESTER_LISTENER.expectExactlyOneMessageReceived(expectedIngestEvent);

        String expectedATSRequest = """
                {
                    "requestId": "%s",
                    "nodeRef": "workspace://SpacesStore/37be157c-741c-4e51-b781-20d36e4e335a",
                    "targetMediaType": "application/pdf",
                    "clientData": "{\\"nodeRef\\":\\"37be157c-741c-4e51-b781-20d36e4e335a\\",\\"targetMimeType\\":\\"application/pdf\\",\\"retryAttempt\\":0}",
                    "transformOptions": { "timeout":"%s" },
                    "replyQueue": "%s"
                }""".formatted(
                TEST_UUID,
                integrationProperties.alfresco().transform().request().timeout(),
                integrationProperties.alfresco().transform().response().queueName());
        camelTest.ATS_REQUEST_LISTENER.expectExactlyOneMessageReceived(expectedATSRequest);
    }

    @TestConfiguration
    public static class IntegrationPropertiesTestConfig
    {

        @Bean
        public IntegrationProperties integrationProperties()
        {
            return new IntegrationProperties(
                    new IntegrationProperties.Alfresco(
                            new Repository("direct:test1"),
                            new BulkIngester("direct:test-bulk-ingester-endpoint"),
                            new Transform(
                                    new Transform.Request(
                                            "direct:test-transform-request-endpoint",
                                            1000),
                                    new Transform.Response(
                                            "direct:test-transform-reply-endpoint",
                                            "test-transform-reply-endpoint",
                                            new Retry(),
                                            new Retry()),
                                    new Transform.SharedFileStore(
                                            "localhost",
                                            3000,
                                            new Retry())),
                            mock()),
                    new IntegrationProperties.HylandExperience(
                            mock(),
                            mock(),
                            new Storage(
                                    new Storage.Location(
                                            "mock:test4",
                                            new Retry()),
                                    new Storage.Upload(
                                            new Retry())),
                            new Ingester(
                                    "direct:hxi-ingester-endpoint",
                                    new Retry())));
        }

        @Bean
        public AuthenticationService authenticationService()
        {
            Authentication authentication = mock();

            doReturn(Set.of((GrantedAuthority) () -> "OAUTH2_USER")).when(authentication).getAuthorities();
            doReturn(true).when(authentication).isAuthenticated();

            SecurityContextHolder.getContext().setAuthentication(authentication);
            return mock();
        }
    }
}
