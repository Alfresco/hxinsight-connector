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

import static org.alfresco.hxi_connector.bulk_ingester.util.IngestEventPropertyParser.parseProperties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.alfresco.hxi_connector.bulk_ingester.BulkIngesterApplication;
import org.alfresco.hxi_connector.bulk_ingester.util.integration.PostgresIntegrationTestBase;
import org.alfresco.hxi_connector.common.model.ingest.IngestEvent;

@EnableAutoConfiguration
@SpringBootTest(properties = {"logging.level.org.alfresco=DEBUG",
        "alfresco.filter.aspect.allow[0]=http://www.alfresco.org/model/content/1.0:auditable",
        "alfresco.filter.aspect.deny[0]=http://www.alfresco.org/model/content/1.0:author"},
        classes = BulkIngesterApplication.class)
@Import(MockEventPublisherConfiguration.class)
@SuppressWarnings({"PMD.TestClassWithoutTestCases", "PMD.SimplifyBooleanReturns", "PMD.LooseCoupling"})
class BulkIngestionFilterIntegrationTest extends PostgresIntegrationTestBase
{

    @Autowired
    private BulkIngestionProcessor bulkIngestionProcessor;

    @Autowired
    private TestIngestEventPublisher ingestEventPublisher;

    @Test
    void shouldFilterOutNodeWithDeniedAspect()
    {
        // given
        IngestEvent category = IngestEvent.builder()
                .nodeId("94e0b276-6447-4dbc-b32a-1d37836a8066")
                .properties(parseProperties("type=cm:category", "cm:name=Animal Species", "createdAt=1708329410", "aspectsNames=[cm:auditable]", "createdBy=admin", "modifiedBy=admin"))
                .build();
        IngestEvent folder = IngestEvent.builder()
                .nodeId("dad275aa-affc-487d-a7ed-92cf8e6ce351")
                .properties(parseProperties("type=cm:folder", "cm:name=Animals", "cm:title=Animals overview", "createdAt=1708329323", "aspectsNames=[cm:titled, cm:auditable]", "createdBy=admin", "cm:description=This folder contains overview of the animals in our zoo", "modifiedBy=admin"))
                .build();
        IngestEvent textFile = IngestEvent.builder()
                .nodeId("44545a62-0f64-4d3e-838a-9f8ba23df0c7")
                .contentInfo(new IngestEvent.ContentInfo(28, "UTF-8", "text/plain"))
                .properties(parseProperties("cm:name=Animals list", "cm:title=List of animals", "type=cm:content", "createdAt=1708329628", "app:editInline=true", "aspectsNames=[cm:generalclassifiable, app:inlineeditable, cm:thumbnailModification, cm:titled, cm:taggable, rn:renditioned, cm:auditable]", "cm:categories=25805c3b-dc41-4a3d-9b03-a44ac7963c70", "createdBy=admin", "cm:lastThumbnailModification=[doclib:1708329633941, pdf:1708329629285]", "cm:description=List of animals in our zoo", "modifiedBy=admin"))
                .build();
        IngestEvent pdfFile = IngestEvent.builder()
                .nodeId("02acf462-533d-4e1b-9825-05fa934140da")
                .contentInfo(new IngestEvent.ContentInfo(119625, "UTF-8", "application/pdf"))
                .properties(parseProperties("cm:name=carp.pdf", "type=cm:content", "cm:autoVersion=true", "cm:title=", "cm:versionType=MAJOR", "cm:versionLabel=1.0", "cm:autoVersionOnUpdateProps=false", "aspectsNames=[cm:generalclassifiable, cm:versionable, cm:author, cm:thumbnailModification, cm:titled, cm:taggable, rn:renditioned, cm:auditable]", "cm:categories=fa6b38cd-442a-4f77-9d3e-dc212a6b809e", "cm:lastThumbnailModification=doclib:1708330172467", "cm:description=", "createdAt=1708330172", "cm:initialVersion=true", "createdBy=admin", "modifiedBy=admin"))
                .build();

        // when
        bulkIngestionProcessor.process();

        // then
        ingestEventPublisher.assertPublishedNode(category);
        ingestEventPublisher.assertPublishedNode(folder);
        ingestEventPublisher.assertPublishedNode(textFile);
        ingestEventPublisher.assertNodeNotPublished(pdfFile);
    }

}
