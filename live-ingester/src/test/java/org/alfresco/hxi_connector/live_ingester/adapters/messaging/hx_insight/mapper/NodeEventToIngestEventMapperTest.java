/*
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2026 Alfresco Software Limited
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
package org.alfresco.hxi_connector.live_ingester.adapters.messaging.hx_insight.mapper;

import static org.alfresco.hxi_connector.common.constant.NodeProperties.CONTENT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.CREATED_BY_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_AT_PROPERTY;
import static org.alfresco.hxi_connector.common.constant.NodeProperties.MODIFIED_BY_PROPERTY;
import static org.alfresco.hxi_connector.live_ingester.domain.usecase.metadata.model.EventType.CREATE_OR_UPDATE;

import java.util.List;
import java.util.Map;

import org.hyland.sdk.cic.http.client.mapper.MapperService;
import org.hyland.sdk.cic.ingest.object.IngestEvent;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityInfo;
import org.alfresco.hxi_connector.live_ingester.adapters.messaging.repository.util.AuthorityTypeResolver;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.ContentProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.DeleteNodeEvent;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.NodeProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.PermissionsProperty;
import org.alfresco.hxi_connector.live_ingester.domain.ports.ingestion_engine.UpdateNodeEvent;

class NodeEventToIngestEventMapperTest
{
    private static final String NODE_ID = "node-id";
    private static final String SOURCE_ID = "dummy-source-id";
    private static final long TIMESTAMP = 1_724_225_729_830L;

    private final NodeEventToIngestEventMapper mapper = new NodeEventToIngestEventMapper();

    @Test
    void shouldMapEmptyUpdateEvent() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP);

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {}
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapDeleteEvent() throws JSONException
    {
        DeleteNodeEvent event = new DeleteNodeEvent(NODE_ID, SOURCE_ID, TIMESTAMP);

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "delete",
                  "sourceTimestamp": 1724225729830,
                  "properties": {}
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapAnnotatedMetadataProperties() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(CREATED_AT_PROPERTY, "2024-02-19T07:56:50.034Z"))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_BY_PROPERTY, "000-000-000"))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_AT_PROPERTY, "2025-02-19T07:56:50.034Z"));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "createdAt": {"value": "2024-02-19T07:56:50.034Z", "annotation": "dateCreated"},
                    "modifiedAt": {"value": "2025-02-19T07:56:50.034Z", "annotation": "dateModified"},
                    "modifiedBy": {"value": "000-000-000", "annotation": "modifiedBy"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapTypedProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("colours", List.of("red", "yellow", "green", "blue")));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "colours": {
                      "value": ["red", "yellow", "green", "blue"],
                      "type": "string"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapContentProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addContentInstruction(new ContentProperty(CONTENT_PROPERTY, "content-id", "application/pdf",
                        "application/msword", 123L, "something.doc"));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "cm:content": {
                      "file": {
                        "id": "content-id",
                        "content-type": "application/pdf",
                        "content-metadata": {
                          "content-type": "application/msword",
                          "size": 123,
                          "name": "something.doc"
                        }
                      }
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapPermissionsProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(CREATED_BY_PROPERTY, "admin"))
                .addPermissionsInstruction(new PermissionsProperty(
                        "PERMISSIONS",
                        List.of(new AuthorityInfo("user1", AuthorityTypeResolver.AuthorityType.USER)),
                        List.of(new AuthorityInfo("group1", AuthorityTypeResolver.AuthorityType.GROUP))))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_BY_PROPERTY, "user2"));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "createdBy": {"value": "admin", "annotation": "createdBy"},
                    "modifiedBy": {"value": "user2", "annotation": "modifiedBy"},
                    "PERMISSIONS": {
                      "value": {
                        "read": [{"id": "user1", "type": "USER"}],
                        "deny": [{"id": "group1", "type": "GROUP"}],
                        "principalsType": "effective"
                      },
                      "annotation": "principals"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapNullPropertyValues() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>(CREATED_BY_PROPERTY, null))
                .addMetadataInstruction(new NodeProperty<>(MODIFIED_BY_PROPERTY, null));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "createdBy": {"value": null, "annotation": "createdBy"},
                    "modifiedBy": {"value": null, "annotation": "modifiedBy"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapObjectProperty() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("grandparent", Map.of("parent", Map.of("child", "some-data"))));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "grandparent": {
                      "value": {
                        "parent": {
                          "child": "some-data"
                        }
                      },
                      "type": "object"
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldIgnoreEmptyCollection() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addMetadataInstruction(new NodeProperty<>("emptyList", List.of()))
                .addMetadataInstruction(new NodeProperty<>("nonEmptyList", List.of("something")));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "nonEmptyList": {"value": ["something"], "type": "string"}
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }

    @Test
    void shouldMapContentPropertyWithPartialMetadata() throws JSONException
    {
        UpdateNodeEvent event = new UpdateNodeEvent(NODE_ID, CREATE_OR_UPDATE, SOURCE_ID, TIMESTAMP)
                .addContentInstruction(new ContentProperty(CONTENT_PROPERTY, null, null,
                        "application/msword", null, null));

        IngestEvent result = mapper.map(event);
        String json = MapperService.writeAsString(result);

        String expected = """
                {
                  "objectId": "node-id",
                  "sourceId": "dummy-source-id",
                  "eventType": "createOrUpdate",
                  "sourceTimestamp": 1724225729830,
                  "properties": {
                    "cm:content": {
                      "file": {
                        "content-metadata": {
                          "content-type": "application/msword"
                        }
                      }
                    }
                  }
                }""";
        JSONAssert.assertEquals(expected, json, true);
    }
}
