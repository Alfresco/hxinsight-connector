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

package org.alfresco.hxi_connector.bulk_ingester.processor;

import static java.lang.String.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import org.alfresco.hxi_connector.bulk_ingester.BulkIngesterApplication;
import org.alfresco.hxi_connector.bulk_ingester.spring.ApplicationManager;
import org.alfresco.hxi_connector.bulk_ingester.util.integration.PostgresIntegrationTestBase;
import org.alfresco.hxi_connector.common.model.IngestEvent;

@EnableAutoConfiguration
@SpringBootTest(properties = "logging.level.org.alfresco=DEBUG", classes = BulkIngesterApplication.class)
@Import(BulkIngestionProcessorIntegrationTest.MockEventPublisherConfiguration.class)
@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.SimplifyBooleanReturns", "PMD.LooseCoupling"})
class BulkIngestionProcessorIntegrationTest extends PostgresIntegrationTestBase
{

    @Autowired
    private BulkIngestionProcessor bulkIngestionProcessor;

    @Autowired
    private IngestEventPublisher ingestEventPublisher;

    @Test
    void shouldPublishAllNodesFromDb()
    {
        // given
        List<IngestEvent> categories = List.of(
                new IngestEvent("94e0b276-6447-4dbc-b32a-1d37836a8066", null, parseProperties("type=cm:category", "cm:name=Animal Species", "createdAt=1708329410", "aspectsNames=[cm:auditable]", "createdByUserWithId=admin", "modifiedByUserWithId=admin")),
                new IngestEvent("11dedf84-4ebb-431e-adbf-7e92b2792674", null, parseProperties("type=cm:category", "cm:name=Mammal", "createdAt=1708329430", "aspectsNames=[cm:auditable]", "createdByUserWithId=admin", "modifiedByUserWithId=admin")),
                new IngestEvent("fa6b38cd-442a-4f77-9d3e-dc212a6b809e", null, parseProperties("type=cm:category", "cm:name=Fish", "createdAt=1708329464", "aspectsNames=[cm:auditable]", "createdByUserWithId=admin", "modifiedByUserWithId=admin")));

        IngestEvent folder = new IngestEvent("dad275aa-affc-487d-a7ed-92cf8e6ce351", null, parseProperties("type=cm:folder", "cm:name=Animals", "cm:title=Animals overview", "createdAt=1708329323", "aspectsNames=[cm:titled, cm:auditable]", "createdByUserWithId=admin", "cm:description=This folder contains overview of the animals in our zoo", "modifiedByUserWithId=admin"));
        IngestEvent textFile = new IngestEvent("44545a62-0f64-4d3e-838a-9f8ba23df0c7", new IngestEvent.ContentInfo(28, "UTF-8", "text/plain"), parseProperties("cm:name=Animals list", "cm:title=List of animals", "type=cm:content", "createdAt=1708329628", "app:editInline=true", "aspectsNames=[cm:generalclassifiable, app:inlineeditable, cm:thumbnailModification, cm:titled, cm:taggable, rn:renditioned, cm:auditable]", "cm:categories=25805c3b-dc41-4a3d-9b03-a44ac7963c70", "createdByUserWithId=admin", "cm:lastThumbnailModification=[doclib:1708329633941, pdf:1708329629285]", "cm:description=List of animals in our zoo", "modifiedByUserWithId=admin"));

        List<IngestEvent> pdfFiles = List.of(
                new IngestEvent("02acf462-533d-4e1b-9825-05fa934140da", new IngestEvent.ContentInfo(119625, "UTF-8", "application/pdf"), parseProperties("cm:name=carp.pdf", "type=cm:content", "cm:autoVersion=true", "cm:title=", "cm:versionType=MAJOR", "cm:versionLabel=1.0", "cm:autoVersionOnUpdateProps=false", "aspectsNames=[cm:generalclassifiable, cm:versionable, cm:author, cm:thumbnailModification, cm:titled, cm:taggable, rn:renditioned, cm:auditable]", "cm:categories=fa6b38cd-442a-4f77-9d3e-dc212a6b809e", "cm:lastThumbnailModification=doclib:1708330172467", "cm:description=", "createdAt=1708330172", "cm:initialVersion=true", "createdByUserWithId=admin", "modifiedByUserWithId=admin")),
                new IngestEvent("f9d6264e-426b-41cd-9f4b-b660dc582311", new IngestEvent.ContentInfo(2431571, "UTF-8", "application/pdf"), parseProperties("cm:name=giraffe.pdf", "cm:autoVersion=true", "cm:title=", "cm:versionType=MAJOR", "cm:autoVersionOnUpdateProps=false", "cm:versionLabel=1.0", "aspectsNames=[cm:generalclassifiable, cm:versionable, cm:author, cm:thumbnailModification, cm:titled, cm:taggable, rn:renditioned, cm:auditable]", "cm:categories=11dedf84-4ebb-431e-adbf-7e92b2792674", "type=cm:content", "cm:lastThumbnailModification=doclib:1708330181376", "cm:description=", "createdAt=1708330180", "cm:initialVersion=true", "createdByUserWithId=admin", "modifiedByUserWithId=admin")),
                new IngestEvent("71b5b65b-d92a-4944-9403-48b7ebf8664c", new IngestEvent.ContentInfo(1496650, "UTF-8", "application/pdf"), parseProperties("cm:name=porcupine.pdf", "cm:title=", "cm:autoVersion=true", "cm:versionType=MAJOR", "cm:versionLabel=1.0", "cm:autoVersionOnUpdateProps=false", "aspectsNames=[cm:generalclassifiable, cm:versionable, cm:author, cm:thumbnailModification, cm:titled, cm:taggable, rn:renditioned, cm:auditable]", "cm:categories=11dedf84-4ebb-431e-adbf-7e92b2792674", "type=cm:content", "cm:lastThumbnailModification=doclib:1708330181340", "cm:description=", "createdAt=1708330180", "cm:initialVersion=true", "createdByUserWithId=admin", "modifiedByUserWithId=admin")));

        // when
        bulkIngestionProcessor.process();

        // then
        ingestEventPublisher.assertPublishedNodes(categories);
        ingestEventPublisher.assertPublishedNode(folder);
        ingestEventPublisher.assertPublishedNode(textFile);
        ingestEventPublisher.assertPublishedNodes(pdfFiles);
    }

    @TestConfiguration
    public static class MockEventPublisherConfiguration
    {
        @Bean
        @Primary
        public IngestEventPublisher ingestEventPublisher()
        {
            return new IngestEventPublisher();
        }

        @Bean
        @Primary
        public ApplicationManager applicationManager()
        {
            return mock();
        }
    }

    @Slf4j
    public static class IngestEventPublisher implements org.alfresco.hxi_connector.bulk_ingester.event.IngestEventPublisher
    {
        private final Map<String, IngestEvent> ingestEvents = new HashMap<>();

        @Override
        public void publish(IngestEvent ingestEvent)
        {
            log.info("Publishing node {}", ingestEvent.toString());

            ingestEvents.put(ingestEvent.nodeId(), ingestEvent);
        }

        public void assertPublishedNodes(List<IngestEvent> ingestEvents)
        {
            ingestEvents.forEach(this::assertPublishedNode);
        }

        public void assertPublishedNode(IngestEvent ingestEvent)
        {
            IngestEvent publishedIngestEvent = ingestEvents.get(ingestEvent.nodeId());

            assertNotNull(publishedIngestEvent, format("Node %s not published", ingestEvent.nodeId()));

            assertEquals(ingestEvent, publishedIngestEvent);
        }
    }

    private static Map<String, Serializable> parseProperties(String... properties)
    {
        return Arrays.stream(properties)
                .map(BulkIngestionProcessorIntegrationTest::parseProperty)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map.Entry<String, Serializable> parseProperty(String property)
    {
        String[] propertySplit = property.split("=");

        String key = propertySplit[0];

        if (propertySplit.length == 1)
        {
            return Map.entry(key, "");
        }

        String value = propertySplit[1];

        return Map.entry(key, parsePropertyValue(value));
    }

    private static Serializable parsePropertyValue(String value)
    {
        if (value.startsWith("["))
        {
            value = value.replace("[", "");
            value = value.replace("]", "");
            String[] valueSplit = value.split(", ");

            return (Serializable) Set.of(valueSplit);
        }

        if (StringUtils.isNumeric(value))
        {
            return Long.parseLong(value);
        }

        if (value.equals("true"))
        {
            return true;
        }

        if (value.equals("false"))
        {
            return false;
        }

        return value;
    }
}
