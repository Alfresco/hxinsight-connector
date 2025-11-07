/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2025 Alfresco Software Limited
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

import static org.mockito.BDDMockito.given;

import static org.alfresco.hxi_connector.bulk_ingester.util.IngestEventPropertyParser.parseProperties;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import org.alfresco.hxi_connector.bulk_ingester.BulkIngesterApplication;
import org.alfresco.hxi_connector.bulk_ingester.processor.mapper.TimeProvider;
import org.alfresco.hxi_connector.bulk_ingester.util.integration.PostgresIntegrationTestBase;
import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableAutoConfiguration
@SpringBootTest(properties = {"logging.level.org.alfresco=DEBUG",
        "alfresco.filter.aspect.allow[0]=cm:titled",
        "alfresco.filter.aspect.deny[0]=cm:author",
        "alfresco.filter.type.allow[0]=cm:category", "alfresco.filter.type.allow[1]=cm:content",
        "alfresco.filter.type.deny[0]=cm:folder",
        "alfresco.filter.path.allow[0]=6d7c466b-efd0-4b88-b77f-a941f3a2f025", // company home
        "alfresco.filter.path.deny[0]=811e21ac-7d5a-469b-ab6e-ec3c8cd8a864"}, // data dictionary
        classes = BulkIngesterApplication.class)
@Import(MockEventPublisherConfiguration.class)
@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.SimplifyBooleanReturns", "PMD.LooseCoupling"})
class BulkIngestionFilterIntegrationTest extends PostgresIntegrationTestBase
{
    private static final long TIMESTAMP = 1_708_329_410L;

    @MockBean
    private TimeProvider timeProvider;

    @Autowired
    private BulkIngestionProcessor bulkIngestionProcessor;

    @Autowired
    private DummyIngestEventPublisher ingestEventPublisher;

    @BeforeEach
    void setUp()
    {
        given(timeProvider.getCurrentTimestamp()).willReturn(TIMESTAMP);
    }

    @AfterEach
    void cleanUp()
    {
        ingestEventPublisher.cleanUpEvents();
    }

    @Test
    void shouldFilterOutNodesWithoutAllowedAspectOrWithDeniedAspect()
    {
        // given
        Map<String, Serializable> categoryProperties = new HashMap<>();
        categoryProperties.put("type", "cm:category");
        categoryProperties.put("cm:name", "Animal Species");
        categoryProperties.put("createdAt", "2024-02-19T08:00:28.040Z");
        categoryProperties.put("modifiedAt", "2024-02-19T09:31:46.154Z");
        categoryProperties.put("aspectsNames", (Serializable) List.of("cm:auditable"));
        categoryProperties.put("createdBy", "admin");
        categoryProperties.put("modifiedBy", "admin");
        categoryProperties.put("ancestors", (Serializable) Map.of(
                "primaryParentId", "",
                "primaryAncestorIds", (Serializable) List.of()));

        IngestEvent category = IngestEvent.builder()
                .nodeId("94e0b276-6447-4dbc-b32a-1d37836a8066")
                .properties(categoryProperties)
                .timestamp(TIMESTAMP)
                .build();

        Map<String, Serializable> folderProperties = new HashMap<>();
        folderProperties.put("type", "cm:folder");
        folderProperties.put("cm:name", "Animals");
        folderProperties.put("cm:title", "Animals overview");
        folderProperties.put("createdAt", "2024-02-19T08:00:28.040Z");
        folderProperties.put("modifiedAt", "2024-02-19T09:31:46.154Z");
        folderProperties.put("aspectsNames", (Serializable) List.of("cm:titled", "cm:auditable"));
        folderProperties.put("createdBy", "admin");
        folderProperties.put("cm:description", "This folder contains overview of the animals in our zoo");
        folderProperties.put("modifiedBy", "admin");
        folderProperties.put("ancestors", (Serializable) Map.of(
                "primaryParentId", "",
                "primaryAncestorIds", (Serializable) List.of()));

        IngestEvent folder = IngestEvent.builder()
                .nodeId("dad275aa-affc-487d-a7ed-92cf8e6ce351")
                .properties(folderProperties)
                .timestamp(TIMESTAMP)
                .build();

        Map<String, Serializable> textFileProperties = new HashMap<>();
        textFileProperties.put("cm:name", "Animals list");
        textFileProperties.put("cm:title", "List of animals");
        textFileProperties.put("type", "cm:content");
        textFileProperties.put("createdAt", "2024-02-19T08:00:28.040Z");
        textFileProperties.put("modifiedAt", "2024-02-19T09:31:46.154Z");
        textFileProperties.put("app:editInline", true);
        textFileProperties.put("aspectsNames", (Serializable) List.of("cm:generalclassifiable", "app:inlineeditable", "cm:thumbnailModification", "cm:titled", "cm:taggable", "rn:renditioned", "cm:auditable"));
        textFileProperties.put("cm:categories", "25805c3b-dc41-4a3d-9b03-a44ac7963c70");
        textFileProperties.put("createdBy", "admin");
        textFileProperties.put("cm:lastThumbnailModification", (Serializable) List.of("doclib:1708329633941", "pdf:1708329629285"));
        textFileProperties.put("cm:description", "List of animals in our zoo");
        textFileProperties.put("modifiedBy", "admin");
        textFileProperties.put("ALLOW_ACCESS", (Serializable) List.of("GROUP_EVERYONE"));
        textFileProperties.put("ancestors", (Serializable) Map.of(
                "primaryParentId", "dad275aa-affc-487d-a7ed-92cf8e6ce351",
                "primaryAncestorIds", (Serializable) List.of("6d7c466b-efd0-4b88-b77f-a941f3a2f025", "e7a273da-2974-4581-a219-5e897342844a","dad275aa-affc-487d-a7ed-92cf8e6ce351")));

        IngestEvent textFile = IngestEvent.builder()
                .nodeId("44545a62-0f64-4d3e-838a-9f8ba23df0c7")
                .contentInfo(new IngestEvent.ContentInfo(28, "UTF-8", "text/plain"))
                .properties(textFileProperties)
                .timestamp(TIMESTAMP)
                .build();

        Map<String, Serializable> pdfFileProperties = new HashMap<>();
        pdfFileProperties.put("cm:name", "carp.pdf");
        pdfFileProperties.put("type", "cm:content");
        pdfFileProperties.put("cm:autoVersion", true);
        pdfFileProperties.put("cm:title", "");
        pdfFileProperties.put("cm:versionType", "MAJOR");
        pdfFileProperties.put("cm:versionLabel", "1.0");
        pdfFileProperties.put("cm:autoVersionOnUpdateProps", false);
        pdfFileProperties.put("aspectsNames", (Serializable) List.of("cm:generalclassifiable", "cm:versionable", "cm:author", "cm:thumbnailModification", "cm:titled", "cm:taggable", "rn:renditioned", "cm:auditable"));
        pdfFileProperties.put("cm:categories", "fa6b38cd-442a-4f77-9d3e-dc212a6b809e");
        pdfFileProperties.put("cm:lastThumbnailModification", "doclib:1708330172467");
        pdfFileProperties.put("cm:description", "");
        pdfFileProperties.put("createdAt", "2024-02-19T08:00:28.040Z");
        pdfFileProperties.put("modifiedAt", "2024-02-19T09:31:46.154Z");
        pdfFileProperties.put("cm:initialVersion", true);
        pdfFileProperties.put("createdBy", "admin");
        pdfFileProperties.put("modifiedBy", "admin");
        pdfFileProperties.put("ancestors", (Serializable) Map.of(
                "primaryParentId", "",
                "primaryAncestorIds", (Serializable) List.of()));

        IngestEvent pdfFile = IngestEvent.builder()
                .nodeId("02acf462-533d-4e1b-9825-05fa934140da")
                .contentInfo(new IngestEvent.ContentInfo(119625, "UTF-8", "application/pdf"))
                .properties(pdfFileProperties)
                .timestamp(TIMESTAMP)
                .build();

        IngestEvent emailTemplate = IngestEvent.builder()
                .nodeId("3d022f89-1ee0-49af-ac54-55c16702b188")
                .contentInfo(new IngestEvent.ContentInfo(6156, "UTF-8", "text/plain"))
                .properties(parsePropertiesWithAncestors(
                        createAncestorsMap("d37ab7e8-f181-41ed-822a-8f22f6626429",
                                List.of("811e21ac-7d5a-469b-ab6e-ec3c8cd8a864", "e7a273da-2974-4581-a219-5e897342844a",
                                        "832b097a-c6ab-4e37-a0e3-a1c52d0e79bf", "6d7c466b-efd0-4b88-b77f-a941f3a2f025")),
                        "cm:title=invite-email_it.html.ftl", "createdAt=2024-02-19T07:51:13.391Z", "modifiedAt=2024-02-19T07:51:13.391Z",
                        "createdBy=System", "cm:name=invite-email_it.html.ftl", "app:editInline=true",
                        "aspectsNames=[app:inlineeditable, cm:titled, cm:auditable]", "modifiedBy=System", "type=cm:content",
                        "cm:description=Email template used to generate the invite email for Alfresco Share - Italian version",
                        "ALLOW_ACCESS=[GROUP_EVERYONE]"))
                .timestamp(TIMESTAMP)
                .build();

        // when
        bulkIngestionProcessor.process();

        // then
        ingestEventPublisher.assertNodeNotPublished(category);
        ingestEventPublisher.assertNodeNotPublished(folder);
        ingestEventPublisher.assertPublishedNode(textFile);
        ingestEventPublisher.assertNodeNotPublished(pdfFile);
        ingestEventPublisher.assertNodeNotPublished(emailTemplate);
    }
    private Map<String, Serializable> parsePropertiesWithAncestors(Map<String, Serializable> ancestors, String... properties) {
        Map<String, Serializable> props = parseProperties(properties);
        props.put("ancestors", (Serializable) ancestors);
        return props;
    }

    private Map<String, Serializable> createAncestorsMap(String primaryParentId, List<String> primaryAncestorIds) {
        Map<String, Serializable> ancestors = new HashMap<>();
        ancestors.put("primaryParentId", primaryParentId);
        ancestors.put("primaryAncestorIds", (Serializable) primaryAncestorIds);
        return ancestors;
    }
}
